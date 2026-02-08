(ns test-runner
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test]))

(defn- namespaces-from-args [args]
  (loop [xs args
         out []]
    (if (empty? xs)
      out
      (let [[x y & more] xs]
        (if (= x "-n")
          (recur more (conj out (symbol y)))
          (recur (cons y more) out))))))

(defn- test-file?
  [f]
  (and (.isFile f)
       (.endsWith (.getName f) "_test.clj")))

(defn- file->ns-symbol
  [root f]
  (-> (.relativize (.toPath root) (.toPath f))
      str
      (str/replace #"\.clj$" "")
      (str/replace #"[/\\]" ".")
      (str/replace "_" "-")
      symbol))

(defn- discover-test-namespaces
  []
  (let [root (io/file "test")]
    (->> (file-seq root)
         (filter test-file?)
         (map #(file->ns-symbol root %))
         sort
         vec)))

(defn -main [& args]
  (let [requested (namespaces-from-args args)
        nss (if (seq requested)
              requested
              (discover-test-namespaces))]
    (doseq [ns-sym nss]
      (require ns-sym))
    (let [result (apply test/run-tests nss)
          failures (+ (:fail result) (:error result))]
      (shutdown-agents)
      (System/exit (if (zero? failures) 0 1)))))
