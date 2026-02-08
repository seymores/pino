(ns pino.data.sql-adapter)

(defn supported-adapters
  []
  [:postgres :mysql :sqlite])
