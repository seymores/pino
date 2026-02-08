(ns pino.compiler.manifest
  (:require [malli.core :as m]
            [pino.compiler.errors :as errors]))

(def Manifest
  [:map
   [:name string?]
   [:features [:vector any?]]])

(defn- first-path
  [explain-data]
  (or (-> explain-data :errors first :path vec)
      []))

(defn validate!
  [manifest]
  (if (m/validate Manifest manifest)
    manifest
    (let [explain-data (m/explain Manifest manifest)]
      (throw (errors/invalid-manifest (first-path explain-data) explain-data)))))
