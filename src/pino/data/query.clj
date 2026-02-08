(ns pino.data.query
  (:refer-clojure :exclude [compile])
  (:require [honey.sql :as hsql]))

(defn- normalize-dialect
  [dialect]
  (case dialect
    (:postgres :postgresql :sqlite) :ansi
    :mysql :mysql
    dialect))

(defn compile
  [query dialect]
  (let [[sql & params] (hsql/format query {:dialect (normalize-dialect dialect)})]
    {:sql sql
     :params (vec params)}))
