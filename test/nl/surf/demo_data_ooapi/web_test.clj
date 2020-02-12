(ns nl.surf.demo-data-ooapi.web-test
  (:require [nl.surf.demo-data-ooapi.web :as web]
            [clojure.test :refer [deftest is]]))

(deftest test-wrap-pagination
  (let [handler (-> {:status 200
                     :body   (mapv (fn [n]
                                     {:name (str n)})
                                   ["j" "i" "d" "e" "l" "g" "c" "h" "f" "m" "b" "k" "a"])}
                    constantly
                    web/wrap-pagination)]
    (is (= {:status 200
            :body
            {:_embedded  {:items [{:name "g"} {:name "h"} {:name "i"}]}
             :_links
             {:self {:href "/foo?pageSize=3&pageNumber=3"}
              :prev {:href "/foo?pageSize=3&pageNumber=2"}
              :next {:href "/foo?pageSize=3&pageNumber=4"}}
             :pageSize   3
             :pageNumber 3}}
           (handler {:uri "/foo" :params {"pageSize"   3
                                          "pageNumber" 3}})))

    (is (= {:status 200
            :body
            {:_embedded  {:items [{:name "a"} {:name "b"} {:name "c"}]}
             :_links
             {:self {:href "/foo?pageSize=3&pageNumber=1"}
              :next {:href "/foo?pageSize=3&pageNumber=2"}}
             :pageSize   3
             :pageNumber 1}}
           (handler {:uri "/foo" :params {"pageSize"   3
                                          "pageNumber" 1}})))

    (is (= {:status 200
            :body
            {:_embedded  {:items [{:name "m"}]}
             :_links
             {:self {:href "/foo?pageSize=3&pageNumber=5"}
              :prev {:href "/foo?pageSize=3&pageNumber=4"}}
             :pageSize   3
             :pageNumber 5}}
           (handler {:uri "/foo" :params {"pageSize"   3
                                          "pageNumber" 5}})))))

