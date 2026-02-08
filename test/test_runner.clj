(ns test-runner
  (:require [clojure.test :as test]))

(defn- namespaces-from-args [args]
  (loop [xs args
         out []]
    (if (empty? xs)
      out
      (let [[x y & more] xs]
        (if (= x "-n")
          (recur more (conj out (symbol y)))
          (recur (cons y more) out))))))

(defn -main [& args]
  (let [nss (namespaces-from-args args)]
    (doseq [ns-sym nss]
      (require ns-sym))
    (let [result (if (seq nss)
                   (apply test/run-tests nss)
                   (test/run-all-tests #"^pino\\."))
          failures (+ (:fail result) (:error result))]
      (shutdown-agents)
      (System/exit (if (zero? failures) 0 1)))))
