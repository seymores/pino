(ns pino.dsl.feature
  (:require [pino.dsl.routes :as routes]))

(defn- parse-route
  [[head id opts]]
  {:route-type (routes/route-type head)
   :id id
   :opts opts})

(defn parse-feature
  [[_ id & body]]
  (let [parsed (map parse-route body)]
    {:id id
     :pages (->> parsed
                 (filter #(= :page (:route-type %)))
                 (map (fn [{:keys [id opts]}]
                        {:id id :path (:get opts)}))
                 vec)
     :apis (->> parsed
                (filter #(= :api (:route-type %)))
                (map (fn [{:keys [id opts]}]
                       {:id id :path (:get opts)}))
                vec)}))
