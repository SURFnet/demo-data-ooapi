(ns nl.surf.demo-data-ooapi.main
  (:require [nl.surf.demo-data-ooapi.config :as config]
            [nl.surf.demo-data-ooapi.web :as web])
  (:gen-class))


(defn -main [& _]
  (let [seed (Integer/parseInt (or (System/getenv "SEED") "42"))]
    (config/generate! seed)
    (web/start!)
    (.join @web/server-atom)))



