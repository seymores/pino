(ns pino.security.middleware-test
  (:require [clojure.test :refer [deftest is]]
            [pino.security.middleware :as mw]))

(deftest csrf-required-for-html-post
  (let [res ((mw/wrap-csrf (fn [_] {:status 200}))
             {:request-method :post
              :uri "/users"
              :headers {}})]
    (is (= 403 (:status res)))))
