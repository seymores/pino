(ns pino.data.query-test
  (:require [clojure.test :refer [deftest is]]
            [pino.data.query :as query]))

(deftest named-query-compiles-to-sql-and-params
  (let [{:keys [sql params]}
        (query/compile {:select [:id]
                        :from [:users]
                        :where [:= :id 1]}
                       :postgresql)]
    (is (string? sql))
    (is (= [1] params))))
