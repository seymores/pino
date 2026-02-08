(ns pino.compiler.routes)

(defn- html-handler
  [_req]
  {:status 200
   :headers {"content-type" "text/html; charset=utf-8"}
   :body "<h1>OK</h1>"})

(defn- json-handler
  [_req]
  {:status 200
   :headers {"content-type" "application/json; charset=utf-8"}
   :body "{\"ok\":true}"})

(defn- routes-for-feature
  [{:keys [pages apis]}]
  (concat
   (map (fn [{:keys [path]}]
          [[:get path] html-handler])
        pages)
   (map (fn [{:keys [path]}]
          [[:get path] json-handler])
        apis)))

(defn compile-routes
  [manifest]
  (into {}
        (mapcat routes-for-feature (:features manifest))))
