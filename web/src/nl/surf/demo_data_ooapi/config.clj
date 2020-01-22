(ns nl.surf.demo-data-ooapi.config
  (:require [clojure.string :as s]
            [nl.surf.demo-data.config :as config]
            [nl.surf.demo-data.generators :as gen]
            [nl.surf.demo-data.date-util :as date-util]
            [nl.surf.demo-data.world :as world]))

(defmethod config/generator "programme-ects" [_]
  (fn programme-ects [world level]
    (if (= level "Bachelor")
      (* ((gen/int 2 8) world) 30)
      (* ((gen/int-cubic 2 8) world) 30))))

(defmethod config/generator "course-ects" [_]
  (fn course-ects [world]
    (- 60 (* 2.5 ((gen/int-cubic 1 24) world)))))

(defmethod config/generator "length-of-programme" [_]
  (fn length-of-programme [_ ects]
    (-> ects (/ 60) (* 12) int)))

;; TODO move to demo-data repo
(defmethod config/generator "object" [_]
  (fn object [world & keys-n-args]
    (when-not (even? (count keys-n-args))
      (ex-info "Expected even amount of arguments" {:args keys-n-args}))
    (let [n (/ (count keys-n-args) 2)]
      (apply hash-map (mapcat (fn [k v] [k v])
                              (take n keys-n-args)
                              (drop n keys-n-args))))))

;; TODO move to demo-data repo
(defmethod config/generator "join" [_]
  (fn join [world & xs]
    (->> xs (filter identity) (s/join " "))))

(defmethod config/generator "date" [_]
  (fn date [world lo hi]
    (let [lo (date-util/->msecs-since-epoch (date-util/parse-date lo))
          hi (date-util/->msecs-since-epoch (date-util/parse-date hi))]
      (date-util/<-msecs-since-epoch ((gen/int lo hi) world)))))

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

(def config
  {:types [{:name       "service"
            :refs       {:institution {:deps ["institution/institutionId"]}}
            :attributes {:serviceId     {:generator   "id"
                                         :constraints ["unique"]}
                         :owner         {:deps [["service/institution" "institution/name"]]}
                         :logo          {:value "https://example.com/logo.png"}
                         :specification {:value "https://example.com/specification"}
                         :documentation {:value "https://example.com/documentation"}
                         :courseLevels  {:value ["Bachelor" "Master"]}
                         :roomTypes     {:value ["General purpose", "Lecture hall" , "PC lab"]}}}

           {:name       "institution"
            :attributes {:institutionId          {:generator   "id"
                                                  :constraints ["unique"]}
                         :brin                   {:generator ["format" "%c%c%c%c"]
                                                  :deps      ["institution/_brin1" "institution/_brin2" "institution/_brin3" "institution/_brin4"]}
                         :_brin1                 {:hidden    true
                                                  :generator ["char" "0" "9"]}
                         :_brin2                 {:hidden    true
                                                  :generator ["char" "0" "9"]}
                         :_brin3                 {:hidden    true
                                                  :generator ["char" "A" "Z"]}
                         :_brin4                 {:hidden    true
                                                  :generator ["char" "A" "Z"]}
                         :name                   {:generator ["format" "%s van %s"]
                                                  :deps      ["institution/_type" "institution/city"]}
                         :domain                 {:hidden    true
                                                  :generator ["format" "%s.nl"]
                                                  :deps      ["institution/_domainBase"]}
                         :_domainBase            {:hidden    true
                                                  :generator "sanitize"
                                                  :deps      ["institution/name"]}
                         :description            {:generator "lorum-ipsum"}
                         :academicCalendar       {:generator ["object" :year]
                                                  :deps      ["institution/_academicCalendarYear"]}
                         :_academicCalendarYear  {:hidden    true
                                                  :generator ["format" "%d-%d"]
                                                  :deps      ["institution/_academicCalendarYear1"
                                                              "institution/_academicCalendarYear2"]}
                         :_academicCalendarYear1 {:hidden    true
                                                  :generator ["int" 1995 2020]}
                         :_academicCalendarYear2 {:hidden    true
                                                  :generator "inc"
                                                  :deps      ["institution/_academicCalendarYear1"]}
                         :_type                  {:hidden    true
                                                  :generator ["one-of" ["Universiteit" "Hogeschool" "Academie"]]}
                         :logo                   {:generator ["format" "https://%s/images/logo.png"]
                                                  :deps      ["institution/domain"]}
                         :address                {:generator ["object" :street :zip :city :country]
                                                  :deps      ["institution/_street" "institution/_zip" "institution/city" "institution/_country"]}
                         :_street_name           {:hidden    true
                                                  :generator ["one-of-resource-lines" "nl/street-names.txt"]}
                         :_house_number          {:hidden    true
                                                  :generator ["int" 1 200]}
                         :_street                {:hidden    true
                                                  :generator ["format" "%s %d"]
                                                  :deps      ["institution/_street_name" "institution/_house_number"]}
                         :_zip_digits            {:hidden    true
                                                  :generator ["int" 1000 9999]}
                         :_zip_letter_1          {:hidden    true
                                                  :generator ["char" "A" "Z"]}
                         :_zip_letter_2          {:hidden    true
                                                  :generator ["char" "A" "Z"]}
                         :_zip                   {:hidden    true
                                                  :generator ["format" "%d%c%c"]
                                                  :deps      ["institution/_zip_digits" "institution/_zip_letter_1" "institution/_zip_letter_2"]}
                         :city                   {:hidden    true
                                                  :generator ["one-of-resource-lines" "nl/city-names.txt"]}
                         :_country               {:value "NL"}}}

           {:name       "educational-programme"
            :refs       {:service {:deps ["service/serviceId"]}}
            :attributes {:educationalProgrammeId    {:generator   "id"
                                                     :constraints ["unique"]}
                         :name                      {:deps      ["educational-programme/fieldsOfStudy"]
                                                     :generator ["one-of-keyed-resource" "nl/programme-names.yml"]}
                         :description               {:generator "lorum-ipsum"}
                         :termStartDate             {:generator ["first-weekday-of" "monday"]
                                                     :deps      ["educational-programme/_termStartYear" "educational-programme/_termStartMonth"]}
                         :_termMinYear              {:hidden true
                                                     :value  1990}
                         :_termMaxStartYear         {:hidden true
                                                     :value  2019}
                         :_termMaxEndYear           {:hidden true
                                                     :value  2030}
                         :_termStartYear            {:hidden    true
                                                     :generator "int"
                                                     :deps      ["educational-programme/_termMinYear" "educational-programme/_termMaxStartYear"]}
                         :_termStartMonth           {:hidden    true
                                                     :generator ["one-of" ["february" "september"]]}
                         :termEndDate               {:optional  true ;; TODO never empty
                                                     :generator "last-day-of"
                                                     :deps      ["educational-programme/_termEndYear" "educational-programme/_termEndMonth"]}
                         :_termStartYearAfter       {:hidden    true
                                                     :generator "inc"
                                                     :deps      ["educational-programme/_termStartYear"]}
                         :_termEndYear              {:hidden    true
                                                     :generator "int"
                                                     :deps      ["educational-programme/_termStartYearAfter" "educational-programme/_termMaxEndYear"]}
                         :_termEndMonth             {:hidden    true
                                                     :generator ["one-of" ["january" "august"]]}
                         :ects                      {:generator "programme-ects"
                                                     :deps      ["educational-programme/levelOfQualification"]}
                         :mainLanguage              {:generator ["weighted" {"NL-nl" 5
                                                                             "GB-en" 1}]}
                         :qualificationAwarded      {:deps      ["educational-programme/levelOfQualification" "educational-programme/name"]
                                                     :generator ["format" "%s of %s"]}
                         :lengthOfProgramme         {:generator "length-of-programme"
                                                     :deps      ["educational-programme/ects"]}
                         :levelOfQualification      {:deps      [["educational-programme/service" "service/courseLevels"]]
                                                     :generator "one-of"}
                         :profileOfProgramme        {:generator "lorum-ipsum"}
                         :programmeLearningOutcomes {:generator "lorum-ipsum"}
                         :modeOfStudy               {:generator ["weighted" {"full-time"  5
                                                                             "part-time"  2
                                                                             "dual"       1
                                                                             "e-learning" 2}]}
                         :fieldsOfStudy             {:generator ["one-of" ["Economie"
                                                                           "Gezondheidszorg"
                                                                           "Gedrag & Maatschappij"
                                                                           "Landbouw & Natuurlijke omgeving"
                                                                           "Natuur"
                                                                           "Onderwijs"
                                                                           "Taal & Cultuur"
                                                                           "Recht"
                                                                           "Techniek"
                                                                           "Sectoroverstijgend"]]}}}

           {:name "course-programme"
            :refs {:_ {:unique     [true false] ;; TODO make this prettier!
                       :deps       ["course/courseId" "educational-programme/educationalProgrammeId"]
                       :attributes ["course" "educational-programme"]}}}

           {:name       "course"
            :refs       {:service     {:deps ["service/serviceId"]}
                         :coordinator {:deps ["person/personId"]}}
            :attributes {:courseId         {:generator   "id"
                                            :constraints ["unique"]}
                         :name             {:generator "format"
                                            :deps      ["course/_nameFormat" "course/_programmeName"]}
                         :_nameFormat      {:hidden    true
                                            :generator ["one-of" ["Inleiding tot %s"
                                                                  "Geschiedenis van de %s"
                                                                  "Filosofie van %s"
                                                                  "Psychologie van de %s"
                                                                  "Wiskunde van de %s"
                                                                  "Macro %s"
                                                                  "Micro %s"
                                                                  "%s in de praktijk"
                                                                  "%s in de vorige eeuw"
                                                                  "%s van de toekomst"
                                                                  "%s voor gevorderden"]]}
                         :_programmeName   {:hidden    true
                                            :generator ["one-of-keyed-resource" "nl/programme-names.yml"]
                                            :deps      ["course/_field"]}
                         :_field           {:hidden    true
                                            :generator "one-of"
                                            ;; TODO joins are ugly!
                                            :deps      [[["course-programme/course" "course/courseId"] "course-programme/educational-programme" "educational-programme/fieldsOfStudy"]]}
                         :abbreviation     {:generator   "abbreviate"
                                            :deps        ["course/name"]
                                            :constraints ["unique"]}
                         :ects             {:generator "course-ects"}
                         :description      {:generator "lorum-ipsum"}
                         :learningOutcomes {:generator "lorum-ipsum"}
                         :goals            {:generator "lorum-ipsum"}
                         :requirements     {:generator "course-requirements"
                                            :deps      ["course/name"]}
                         :level            {:generator "one-of"
                                            :deps      [["course/service" "service/courseLevels"]]}
                         :format           {:generator ["weighted" {"practicum"   1
                                                                    "hoorcollege" 1}]}
                         :modeOfDelivery   {:generator ["weighted-set" {"e-learning"   1
                                                                        "face-to-face" 2
                                                                        "class-room"   20}]}
                         :mainLanguage     {:generator ["weighted" {"NL-nl" 5
                                                                    "GB-en" 1}]}
                         :enrollment       {:generator "lorum-ipsum"}
                         :resources        {:generator "lorum-ipsum"}
                         :exams            {:generator "lorum-ipsum"}
                         :schedule         {:generator ["weighted" {"1e periode" 2
                                                                    "2e periode" 2
                                                                    "3e periode" 2
                                                                    "4e periode" 2
                                                                    "jan-feb"    1
                                                                    "feb-mrt"    1
                                                                    "mrt-apr"    1
                                                                    "apr-mei"    1
                                                                    "mei-jun"    1
                                                                    "jun-jul"    1
                                                                    "sep-okt"    1
                                                                    "okt-nov"    1
                                                                    "nov-dec"    1}]}}}
           {:name       "course-offering"
            :refs       {:course {:deps ["course/courseId"]}}
            :attributes {:courseOfferingId      {:generator   "id"
                                                 :constraints ["unique"]}
                         :courseId              {:deps [["course-offering/course" "course/courseId"]]}
                         :maxNumberStudents     {:generator ["int-cubic" 20 50]}
                         :currentNumberStudents {:generator ["int" 10]
                                                 :deps      ["course-offering/maxNumberStudents"]}
                         :academicYear          {:generator ["format" "%d-%d"]
                                                 :deps      ["course-offering/_academicYear1"
                                                             "course-offering/_academicYear2"]}
                         :_academicYear1        {:hidden    true
                                                 :generator ["int" 1995 2020]}
                         :_academicYear2        {:hidden    true
                                                 :generator "inc"
                                                 :deps      ["course-offering/_academicYear1"]}
                         :period                {:generator ["one-of" ["1e periode"
                                                                       "2e periode"
                                                                       "3e periode"
                                                                       "4e periode"]]}}}

           {:name "lecturer"
            :refs {:_ {:unique     true
                       :deps       ["person/personId" "course-offering/courseOfferingId"]
                       :attributes ["person" "courseOffering"]}}}

           {:name       "person"
            :refs       {:institution {:deps ["institution/institutionId"]}}
            :attributes {:personId         {:generator   "id"
                                            :constraints ["unique"]}
                         :givenName        {:generator ["one-of-resource-lines"
                                                        "nl/surf/demo_data_ooapi/first-names.txt"]}
                         :surname          {:generator ["one-of-resource-lines"
                                                        "nl/surf/demo_data_ooapi/last-names.txt"]}
                         :surnamePrefix    {:generator ["weighted" {nil       50
                                                                    "van"     3
                                                                    "van de"  3
                                                                    "van het" 3
                                                                    "van 't"  2
                                                                    "in de"   2
                                                                    "in 't"   2
                                                                    "aan de"  1
                                                                    "aan het" 1
                                                                    "bij"     1
                                                                    "bij de"  1
                                                                    "bij het" 1
                                                                    "op de"   1
                                                                    "op het"  1
                                                                    "op 't"   1}]}
                         :displayName      {:generator "join"
                                            :deps      ["person/givenName" "person/surnamePrefix" "person/surname"]}
                         :dateOfBirth      {:generator ["date" "1990-01-01" "2005-01-01"]}
                         :affiliations     {:value ["employee"]}
                         :mail             {:generator ["format" "%s@%s"]
                                            :deps      ["person/_mailName" ["person/institution" "institution/domain"]]}
                         :_mailName        {:hidden      true
                                            :generator   "sanitize"
                                            :deps        ["person/displayName"]
                                            :constraints ["unique"]}
                         :telephoneNumber  {:generator ["format" "0%09d"]
                                            :deps      ["person/_telephoneNumber"]}
                         :_telephoneNumber {:hidden      true
                                            :generator   ["int" 100000000 999999999]
                                            :constraints ["unique"]}
                         :mobileNumber     {:generator ["format" "06%08d"]
                                            :deps      ["person/_mobileNumber"]}
                         :_mobileNumber    {:hidden      true
                                            :generator   ["int" 0 99999999]
                                            :constraints ["unique"]}
                         :photoSocial      {:value "https://docs.atlassian.com/aui/8.4.1/docs/images/avatar-person.svg"}
                         :photoOfficial    {:value "https://docs.atlassian.com/aui/8.4.1/docs/images/avatar-person.svg"}
                         :gender           {:generator ["weighted" {"M" 46
                                                                    "F" 50
                                                                    "X" 2
                                                                    "U" 2}]}
                         :title            {:generator ["weighted" {nil     50
                                                                    "dr."   4
                                                                    "mr."   4
                                                                    "ir."   4
                                                                    "ing."  5
                                                                    "drs."  4
                                                                    "prof." 4
                                                                    "bacc." 2
                                                                    "kand." 2}]}
                         :office           {:generator ["format" "%c%c%c %c.%c%c"]
                                            :deps      ["person/_office1" "person/_office2" "person/_office3"
                                                        "person/_office4" "person/_office5" "person/_office6"]}
                         :_office1         {:hidden    true
                                            :generator ["char" "A" "Z"]}
                         :_office2         {:hidden    true
                                            :generator ["char" "A" "Z"]}
                         :_office3         {:hidden    true
                                            :generator ["char" "A" "Z"]}
                         :_office4         {:hidden    true
                                            :generator ["char" "0" "9"]}
                         :_office5         {:hidden    true
                                            :generator ["char" "0" "9"]}
                         :_office6         {:hidden    true
                                            :generator ["char" "0" "9"]}}}]})

(comment
  (->> (config/load config)
       (filter :hidden)
       (map :name))
  (->> (world/gen (config/load config)
                  {:service               1
                   :institution           1
                   :educational-programme 2
                   :course-programme      30
                   :course                20
                   :lecturer              30
                   :course-offering       30
                   :person                30})


       :course
       (map #(select-keys % [:course/name])))
  )
