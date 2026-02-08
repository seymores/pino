(ns pino.auth.policy)

(defn allowed?
  [identity policy-id]
  (case policy-id
    :admin-only (= :admin (:role identity))
    true))
