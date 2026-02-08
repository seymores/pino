(ns pino.auth.session-test
  (:require [clojure.test :refer [deftest is]]
            [pino.auth.session :as session]))

(deftest sql-session-store-roundtrip
  (let [id (session/create! nil {:user-id 42})
        row (session/fetch nil id)]
    (is (= 42 (:user-id row)))))
