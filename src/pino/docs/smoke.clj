(ns pino.docs.smoke
  (:refer-clojure :exclude [run!])
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn run!
  [readme-path]
  (let [file (io/file readme-path)]
    (if-not (.exists file)
      {:exit 1 :error "README not found"}
      (let [content (slurp file)
            required ["## Quickstart"
                      "clojure -M -m pino.cli.main new demo"
                      "clojure -M -m pino.cli.main generate feature users --target demo"
                      "clojure -M:test"]]
        (if (every? #(str/includes? content %) required)
          {:exit 0}
          {:exit 1 :error "README missing required commands"})))))
