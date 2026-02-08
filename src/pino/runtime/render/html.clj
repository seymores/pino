(ns pino.runtime.render.html
  (:require [selmer.parser :as parser]))

(defn render
  [{:keys [template data]}]
  {:status 200
   :headers {"content-type" "text/html; charset=utf-8"}
   :body (parser/render-file (str "templates/" template) data)})
