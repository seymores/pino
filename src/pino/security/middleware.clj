(ns pino.security.middleware)

(defn wrap-csrf
  [handler]
  (fn [req]
    (if (and (= :post (:request-method req))
             (nil? (get-in req [:headers "x-csrf-token"])))
      {:status 403
       :body "CSRF token required"}
      (handler req))))
