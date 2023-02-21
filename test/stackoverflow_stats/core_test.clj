(ns stackoverflow-stats.core-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [stackoverflow-stats.core :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (handler (mock/request :get "/search?tag=clojure&tag=python&tag=clojurescript"))]
      (is (= (:status response) 200)))))
