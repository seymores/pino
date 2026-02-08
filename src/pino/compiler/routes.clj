(ns pino.compiler.routes)

(defn- routes-for-feature
  [{:keys [pages apis]}]
  (concat
   (map (fn [{:keys [path]}]
          [[:get path] :html-handler])
        pages)
   (map (fn [{:keys [path]}]
          [[:get path] :json-handler])
        apis)))

(defn compile-routes
  [manifest]
  (into {}
        (mapcat routes-for-feature (:features manifest))))
