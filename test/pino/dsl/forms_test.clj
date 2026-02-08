(ns pino.dsl.forms-test
  (:require [clojure.test :refer [deftest is]]
            [pino.dsl :as dsl]))

(deftest app-and-feature-expand-to-manifest-data
  (let [manifest
        (dsl/emit
         '(dsl/app {:name "demo"}
            (dsl/feature :users
              (dsl/page :index {:get "/users"})
              (dsl/api :index {:get "/api/users"}))))]
    (is (= "demo" (:name manifest)))
    (is (= :users (-> manifest :features first :id)))))
