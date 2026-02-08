(ns pino.acceptance.hybrid-feature-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.app :as app]))

(defn- call-async
  [handler req]
  (let [result (promise)]
    (handler req
             #(deliver result %)
             #(deliver result {:status 500 :body (.getMessage %)}))
    @result))

(deftest generated-feature-serves-html-and-json
  (let [application (app/build-app
                     '(pino.dsl/app {:name "demo"}
                        (pino.dsl/feature :users
                          (pino.dsl/page :index {:get "/users"})
                          (pino.dsl/api :index {:get "/api/users"}))))
        html-res (call-async application {:request-method :get :uri "/users"})
        json-res (call-async application {:request-method :get :uri "/api/users"})]
    (is (= 200 (:status html-res)))
    (is (= 200 (:status json-res)))))
