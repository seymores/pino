(ns pino.runtime.app
  (:require [pino.compiler.manifest :as manifest]
            [pino.compiler.routes :as routes]
            [pino.dsl :as dsl]
            [pino.runtime.pipeline :as pipeline]))

(defn build-app
  [dsl-form]
  (let [compiled-manifest (-> dsl-form dsl/emit manifest/validate!)
        route-table (routes/compile-routes compiled-manifest)]
    (pipeline/build {:route-table route-table})))
