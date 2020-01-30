;; Copyright (C) 2020 SURFnet B.V.
;;
;; This program is free software: you can redistribute it and/or modify it
;; under the terms of the GNU General Public License as published by the Free
;; Software Foundation, either version 3 of the License, or (at your option)
;; any later version.
;;
;; This program is distributed in the hope that it will be useful, but WITHOUT
;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
;; FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
;; more details.
;;
;; You should have received a copy of the GNU General Public License along
;; with this program. If not, see http://www.gnu.org/licenses/.

(ns nl.surf.demo-data-ooapi.config
  (:require [cheshire.core :as json]
            [clojure.data.generators :as dgen]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [nl.surf.demo-data.config :as config]
            [nl.surf.demo-data.export :as export]
            [nl.surf.demo-data.generators :as gen]
            [nl.surf.demo-data.world :as world]
            [remworks.markov-chain :as mc]))

(def text-spaces (->> "data.edn"
                      io/resource
                      slurp
                      read-string
                      (map #(dissoc % :id :field-of-study))
                      (reduce (fn [m x]
                                (merge-with (fn [a b]
                                              (let [b (s/replace (str b) #"<[^>]+>" "")]
                                                (if (s/ends-with? a ".")
                                                  (str a " " b)
                                                  (str a ". " b))))
                                            m x))
                              {})
                      (map (fn [[k v]]
                             [(name k) (mc/analyse-text v)]))
                      (into {})))

(defmethod config/generator "lorum-surf" [_]
  (fn surf-lorem [world & [scope lines]]
    (let [space (get text-spaces scope (get text-spaces "description"))]
      (->> #(mc/generate-text space)
           (repeatedly (or lines 3))
           (s/join " ")))))

(defmethod config/generator "programme-ects" [_]
  (fn programme-ects [world level]
    (if (= level "Bachelor")
      (* ((gen/int 2 8) world) 30)
      (* ((gen/int-cubic 2 8) world) 30))))

(defmethod config/generator "course-ects" [_]
  (fn course-ects [world]
    (int (- 60 (* 2.5 ((gen/int-cubic 1 24) world))))))

(defmethod config/generator "id" [_]
  (fn id [world]
    (str ((gen/int 1 Integer/MAX_VALUE) world))))

(defmethod config/generator "sanitize" [_]
  (fn sanitize [world x]
    (when x
      (-> x
          s/lower-case
          s/trim
          (s/replace #"[^a-z0-9]+" "-")
          (str (when (> world/*retry-attempt-nr* 0) world/*retry-attempt-nr*))))))

(defmethod config/generator "course-requirements" [_]
  (defn course-requirements [{{:keys [course]} :world :as world} name]
    (when (= 0 ((gen/int 0 2) world))
      (when-let [courses (->> course
                              (map :course/name)
                              (filter (fn [v] (not= name v)))
                              seq)]
        ((gen/one-of courses) world)))))

(defn lecturers-for-offering
  [world course-offering-id]
  (keep (fn [{[_ courseOfferingId] :lecturer/courseOffering
              person               :lecturer/person}]
          (when (= course-offering-id courseOfferingId)
            (world/get-entity world person)))
        (:lecturer world)))

(defn offerings-for-course
  [world course-id]
  (->> world
       :course-offering
       (filter #(= (second (:course-offering/course %)) course-id))))

(defn lecturers-for-course
  [world course-id]
  (->> course-id
       (offerings-for-course world)
       (map :course-offering/courseOfferingId)
       (mapcat #(lecturers-for-offering world %))
       set))

(defn programmes-for-course
  [world course-id]
  (keep (fn [{[_ courseId] :course-programme/course
              programme    :course-programme/educational-programme}]
          (when (= course-id courseId)
            (world/get-entity world programme)))
        (:course-programme world)))

(defn person-link
  [{:person/keys [displayName personId]}]
  {:href  (str "/persons/" personId)
   :title displayName})

(def export-conf
  {"/"                       {:type       :service
                              :singleton? true
                              :attributes {:service/institution {:hidden? true}}
                              :pre        (fn [e _]
                                            (assoc e :_links {:self      {:href "/"}
                                                              :endpoints [{:href "/institution"}
                                                                          {:href "/educational-programmes"}
                                                                          {:href "/course-offerings"}
                                                                          {:href "/persons"}
                                                                          {:href "/courses"}]}))}
   "/institution"            {:type       :institution
                              :singleton? true
                              :attributes {:institution/addressCity {:hidden? true}
                                           :institution/domain      {:hidden? true}}
                              :pre        (fn [e _]
                                            (assoc e :_links {:self                   {:href "/institution"}
                                                              :educational-programmes {:href "/educational-programmes"}}))}
   "/educational-programmes" {:type       :educational-programme
                              :attributes {:educational-programme/service {:hidden? true}}
                              :pre        (fn [{:educational-programme/keys [educationalProgrammeId] :as e} _]
                                            (assoc e :_links {:self    {:href (str "/educational-programmes/" educationalProgrammeId)}
                                                              :courses {:href (str "/courses?educationalProgramme=" educationalProgrammeId)}}))}
   "/course-offerings"       {:type       :course-offering
                              :attributes {:course-offering/course {:follow-ref? true
                                                                    :attributes  {:course/educationalProgramme {:hidden? true}}}}
                              :pre        (fn [{:course-offering/keys [courseOfferingId] :as e} world]
                                            (assoc e :_links {:self      {:href (str "/course-offerings/" courseOfferingId)}
                                                              :lecturers (mapv person-link
                                                                               (lecturers-for-offering world courseOfferingId))}))}
   "/persons"                {:type       :person
                              :attributes {:person/institution {:hidden? true}}
                              :pre        (fn [{:person/keys [personId displayName] :as e} _]
                                            (assoc e :_links {:self {:href (str "/persons/" personId)}}
                                        ; link to courses not
                                        ; implemented because that
                                        ; only supports students
                                                   ))}
   "/courses"                {:type       :course
                              :attributes {:course/coordinator {:hidden? true}
                                           :course/service     {:hidden? true}}
                              :pre        (fn [{:course/keys [courseId coordinator] :as e} world]
                                            (assoc e :_links {:self                  {:href (str "/courses/" courseId)}
                                                              :coordinator           (person-link (world/get-entity world coordinator))
                                                              :lecturers             (map person-link (lecturers-for-course world courseId))
                                                              :courseOfferings       {:href (str "/course-offerings?course=" courseId)}
                                                              :educationalProgrammes (map (fn [programme]
                                                                                            {:title (:educational-programme/name programme)
                                                                                             :href  (str "/educational-programmes/" (:educational-programme/educationalProgrammeId programme))})
                                                                                          (programmes-for-course world courseId))}))}})

(def data
  nil)

(defn load-json
  [path]
  (json/decode-stream (io/reader (io/resource path))
                                              keyword))

(defn generate!
  [seed]
  (println (str "Generating data with seed " seed))
  (alter-var-root #'data
                  (fn [_]
                    (binding [dgen/*rnd* (java.util.Random. seed)]
                      (-> (load-json "ooapi-schema.json")
                          (config/load)
                          (world/gen (load-json "ooapi-population.json"))
                          (export/export export-conf))))))

