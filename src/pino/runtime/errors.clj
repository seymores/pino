(ns pino.runtime.errors)

(defn map-error
  [err fmt]
  (case [(:type err) fmt]
    [:validation/failed :html] {:status 422 :body "Validation failed"}
    [:validation/failed :json] {:status 422 :body {:error "validation_failed"}}
    {:status 500 :body "Internal error"}))
