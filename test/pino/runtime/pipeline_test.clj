(ns pino.runtime.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.pipeline :as pipeline]))

(deftest ring-async-contract-is-respected
  (let [handler (pipeline/build {:route-table {[:get "/users"] (fn [_] {:status 200 :body "ok"})}})
        result (promise)]
    (handler {:request-method :get :uri "/users"}
             #(deliver result %)
             #(deliver result {:status 500 :body %}))
    (is (= 200 (:status @result)))))
