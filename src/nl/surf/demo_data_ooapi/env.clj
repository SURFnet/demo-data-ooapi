(ns nl.surf.demo-data-ooapi.env
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as string]))

(defn get
  "Get environment value or default if value is nil or blank"
  [name default]
  (let [raw (System/getenv name)]
    (if (and (seq raw)
             (not (string/blank? raw)))
      raw
      default)))

