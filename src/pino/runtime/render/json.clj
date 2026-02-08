(ns pino.runtime.render.json
  (:require [cheshire.core :as json]))

(defn render
  [{:keys [status data]}]
  {:status (or status 200)
   :headers {"content-type" "application/json; charset=utf-8"}
   :body (json/generate-string data)})
