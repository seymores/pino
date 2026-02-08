(ns pino.auth.policy-test
  (:require [clojure.test :refer [deftest is]]
            [pino.auth.policy :as policy]))

(deftest policy-denies-unauthorized-user
  (is (false? (policy/allowed? {:role :user} :admin-only))))
