(ns pino.compiler.routes-test
  (:require [clojure.test :refer [deftest is]]
            [pino.compiler.routes :as routes]))

(deftest paired-route-conventions-are-generated
  (let [table (routes/compile-routes
               {:features [{:id :users
                            :pages [{:id :index :path "/users"}]
                            :apis [{:id :index :path "/api/users"}]}]})]
    (is (contains? table [:get "/users"]))
    (is (contains? table [:get "/api/users"]))))
