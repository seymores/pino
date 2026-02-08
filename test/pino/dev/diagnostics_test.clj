(ns pino.dev.diagnostics-test
  (:require [clojure.test :refer [deftest is]]
            [pino.dev.diagnostics :as diag]))

(deftest compiler-errors-include-source-location
  (let [data (diag/format-compile-error {:path [:feature :users]
                                         :line 12
                                         :col 5})]
    (is (= 12 (:line data)))
    (is (= 5 (:col data)))))
