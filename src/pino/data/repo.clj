(ns pino.data.repo)

(defn find-by-id
  [adapter table id]
  (adapter :find table id))

(defn create!
  [adapter table attrs]
  (adapter :create table attrs))

(defn update!
  [adapter table id attrs]
  (adapter :update table id attrs))

(defn delete!
  [adapter table id]
  (adapter :delete table id))
