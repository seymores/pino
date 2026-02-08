(ns pino.acceptance.model-query-test
  (:require [clojure.test :refer [deftest is]]
            [pino.data.model :as model]
            [pino.data.query :as query]
            [pino.data.sql-adapter :as adapter]))

(deftest model-and-query-workflow
  (let [entity (model/entity :user {:id 1 :email "a@b.com"})
        compiled (query/compile {:select [:id]
                                 :from [:users]
                                 :where [:= :id 1]}
                                :postgres)
        supported (set (adapter/supported-adapters))]
    (is (model/valid? entity))
    (is (string? (:sql compiled)))
    (is (= [1] (:params compiled)))
    (is (every? supported [:postgres :mysql :sqlite]))))
