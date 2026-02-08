(ns pino.auth.oidc)

(def required-keys
  [:client-id :client-secret :issuer :redirect-uri])

(defn validate-config!
  [cfg]
  (doseq [k required-keys]
    (when-not (contains? cfg k)
      (throw (ex-info "Invalid OIDC config" {:missing k}))))
  cfg)

(defn start-login!
  [cfg]
  (validate-config! cfg)
  {:status 302
   :headers {"location" (str (:issuer cfg) "/authorize")}})

(defn callback!
  [_params]
  {:status 200
   :body {:ok true}})
