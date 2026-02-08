(ns pino.dsl
  (:require [pino.dsl.app :as app]))

(defmacro app
  [opts & body]
  `(list 'pino.dsl/app ~opts ~@body))

(defmacro feature
  [id & body]
  `(list 'pino.dsl/feature ~id ~@body))

(defmacro page
  [id opts]
  `(list 'pino.dsl/page ~id ~opts))

(defmacro api
  [id opts]
  `(list 'pino.dsl/api ~id ~opts))

(defn emit
  [form]
  (app/parse-app form))
