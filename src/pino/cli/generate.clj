(ns pino.cli.generate
  (:require [clojure.java.io :as io]))

(defn- target-app-name
  [target]
  (-> (io/file target) .getName))

(defn- ensure-parent!
  [path]
  (.mkdirs (.getParentFile (io/file path))))

(defn- write-file!
  [path content]
  (ensure-parent! path)
  (spit path content))

(defn- parse-target
  [opts]
  (loop [[k v & more] opts]
    (cond
      (nil? k) "."
      (= "--target" k) v
      :else (recur more))))

(defn feature!
  [feature opts]
  (let [target (parse-target opts)
        app-name (target-app-name target)
        file (format "%s/src/%s/features/%s/routes.clj" target app-name feature)]
    (write-file! file
                 (str "(ns " app-name ".features." feature ".routes)\n\n"
                      "(def routes\n"
                      "  [[:get \"/" feature "\" :index]])\n"))
    {:path file}))

(defn app!
  [app-name opts]
  (let [target (parse-target opts)
        file (format "%s/%s/deps.edn" target app-name)]
    (write-file! file "{:paths [\"src\"]}\n")
    {:path file}))

(defn model!
  [model opts]
  (let [target (parse-target opts)
        app-name (target-app-name target)
        file (format "%s/src/%s/models/%s.clj" target app-name model)]
    (write-file! file
                 (str "(ns " app-name ".models." model ")\n"))
    {:path file}))

(defn resource!
  [resource opts]
  (feature! resource opts))

(defn auth!
  [_ opts]
  (let [target (parse-target opts)
        app-name (target-app-name target)
        file (format "%s/src/%s/auth.clj" target app-name)]
    (write-file! file
                 (str "(ns " app-name ".auth)\n"))
    {:path file}))
