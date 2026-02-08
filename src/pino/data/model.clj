(ns pino.data.model)

(defn entity
  [model attrs]
  {:model model
   :attrs attrs})

(defn valid?
  [_entity]
  true)
