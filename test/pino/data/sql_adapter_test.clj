(ns pino.data.sql-adapter-test
  (:require [clojure.test :refer [deftest is]]
            [pino.data.sql-adapter :as sql-adapter]))

(deftest adapters-supported-in-v1
  (is (= #{:postgres :mysql :sqlite}
         (set (sql-adapter/supported-adapters)))))
