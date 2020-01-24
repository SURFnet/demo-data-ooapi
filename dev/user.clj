(ns user
  (:require [yaml.core :as yaml]
            [clojure.walk :refer [postwalk]]))

(defn resolve-refs
  "Read a YAML OpenAPI specification from a local file and resolve all
  `$ref` entries, which should have relative file paths as
  values. Returns the full specification map."
  [path-to-spec]
  (let [[_ dir] (re-find #"^(.*)/" path-to-spec)]
    (postwalk (fn [x]
                (if-let [ref (and (map? x) (:$ref x))]
                  (resolve-refs (str dir "/" ref))
                  x))
              (or (yaml/from-file path-to-spec)
                  (throw (ex-info "Can't find spec" {:path path-to-spec}))))))

