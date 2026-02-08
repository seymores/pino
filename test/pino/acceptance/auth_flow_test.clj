(ns pino.acceptance.auth-flow-test
  (:require [clojure.test :refer [deftest is]]
            [pino.auth.oidc :as oidc]
            [pino.auth.session :as session]))

(deftest oauth-and-sql-session-flow
  (let [start (oidc/start-login! {:client-id "id"
                                  :client-secret "secret"
                                  :issuer "issuer"
                                  :redirect-uri "http://localhost/callback"})
        callback (oidc/callback! {:code "abc"})
        id (session/create! nil {:user-id 42})
        stored (session/fetch nil id)]
    (is (= 302 (:status start)))
    (is (= 200 (:status callback)))
    (is (= 42 (:user-id stored)))))
