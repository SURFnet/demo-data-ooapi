(defproject nl.surf/demo-data-ooapi "1.0.0"
  :description "SURF Demo OOAPI server"
  :license {:name "GPLv3"
            :url "https://www.gnu.org/licenses/gpl-3.0.en.html"}
  :url "https://github.com/SURFnet/demo-data-ooapi"
  :dependencies [[camel-snake-kebab "0.4.1"]
                 [hiccup "1.0.5"]
                 [log4j/log4j "1.2.17"]
                 [nl.surf/demo-data "1.0.0"]
                 [nl.zeekat/ring-openapi-validator "0.1.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.30"]
                 [ring/ring-codec "1.1.2"]
                 [ring/ring-jetty-adapter "1.8.0"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-devel "1.8.0"]]
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns nl.surf.demo-data-ooapi.web}
  :resource-paths ["resources" "generated"]
  :main nl.surf.demo-data-ooapi.main
  :uberjar-name "demo-data-server-standalone.jar")


