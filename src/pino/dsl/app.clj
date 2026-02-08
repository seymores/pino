(ns pino.dsl.app
  (:require [pino.dsl.feature :as feature]))

(defn- app-form?
  [head]
  (let [n (name head)]
    (or (= n "app")
        (= n "dsl/app")
        (= n "pino.dsl/app"))))

(defn parse-app
  [[head opts & body :as form]]
  (when-not (app-form? head)
    (throw (ex-info "Not an app form" {:type :dsl/invalid-app :form form})))
  {:name (:name opts)
   :features (mapv feature/parse-feature body)})
