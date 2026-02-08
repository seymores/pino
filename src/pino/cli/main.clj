(ns pino.cli.main
  (:refer-clojure :exclude [run!])
  (:require [pino.cli.generate :as gen]))

(defn run!
  [[cmd kind name & opts]]
  (case cmd
    "check" {:exit 0}
    "new" (do (gen/app! kind opts) {:exit 0})
    "generate" (case kind
                 "app" (do (gen/app! name opts) {:exit 0})
                 "feature" (do (gen/feature! name opts) {:exit 0})
                 "model" (do (gen/model! name opts) {:exit 0})
                 "resource" (do (gen/resource! name opts) {:exit 0})
                 "auth" (do (gen/auth! name opts) {:exit 0})
                 {:exit 1 :error "unsupported generator"})
    {:exit 1 :error "unsupported command"}))

(defn -main
  [& args]
  (let [{:keys [exit error]} (run! args)]
    (when error
      (binding [*out* *err*]
        (println error)))
    (System/exit exit)))
