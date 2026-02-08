(ns pino.runtime.pipeline)

(defn build
  [{:keys [route-table]}]
  (fn [request respond raise]
    (future
      (try
        (if-let [handler (get route-table [(:request-method request) (:uri request)])]
          (respond (handler request))
          (respond {:status 404 :body "Not found"}))
        (catch Throwable t
          (raise t))))))
