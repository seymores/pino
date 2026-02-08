(ns pino.auth.session)

(defn create!
  [_ds attrs]
  (:user-id attrs))

(defn fetch
  [_ds session-id]
  {:id session-id
   :user-id session-id})
