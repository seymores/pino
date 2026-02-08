(ns pino.runtime.render.json-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.render.json :as json]))

(deftest json-renderer-uses-rest-contract
  (let [res (json/render {:status 201 :data {:id 10}})]
    (is (= 201 (:status res)))
    (is (= "application/json; charset=utf-8"
           (get-in res [:headers "content-type"])))))
