(ns pino.compiler.manifest-test
  (:require [clojure.test :refer [deftest is testing]]
            [pino.compiler.manifest :as manifest]))

(deftest invalid-manifest-has-actionable-error
  (testing "missing :features"
    (let [ex (try
               (manifest/validate! {:name "demo"})
               nil
               (catch clojure.lang.ExceptionInfo e e))
          data (ex-data ex)]
      (is ex)
      (is (= :manifest/invalid (:type data)))
      (is (= [:features] (:path data))))))
