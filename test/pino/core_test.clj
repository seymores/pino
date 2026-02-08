(ns pino.core-test
  (:require [clojure.test :refer [deftest is]]
            [pino.core :as core]))

(deftest version-string-exists
  (is (string? (core/version))))
