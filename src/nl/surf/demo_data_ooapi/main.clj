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

(ns nl.surf.demo-data-ooapi.main
  (:require [nl.surf.demo-data-ooapi.config :as config]
            [nl.surf.demo-data-ooapi.env :as env]
            [nl.surf.demo-data-ooapi.web :as web])
  (:gen-class))


(defn -main [& _]
  (let [seed (Integer/parseInt (env/get "SEED" "42"))]
    (config/generate! seed)
    (web/start!)
    (.join @web/server-atom)))



