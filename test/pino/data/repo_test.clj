(ns pino.data.repo-test
  (:require [clojure.test :refer [deftest is]]
            [pino.data.repo :as repo]))

(deftest repo-contract-exposes-crud
  (is (fn? repo/find-by-id))
  (is (fn? repo/create!))
  (is (fn? repo/update!))
  (is (fn? repo/delete!)))
