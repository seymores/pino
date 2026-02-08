(ns pino.auth.oidc-test
  (:require [clojure.test :refer [deftest is]]
            [pino.auth.oidc :as oidc]))

(deftest oidc-config-requires-provider-fields
  (is (thrown? Exception
               (oidc/validate-config! {:client-id "x"}))))
