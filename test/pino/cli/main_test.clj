(ns pino.cli.main-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [pino.cli.main :as cli]))

(defn- delete-recursively!
  [f]
  (when (.exists f)
    (doseq [child (reverse (file-seq f))]
      (io/delete-file child true))))

(deftest generates-feature-files
  (let [target "tmp/demo"
        root (io/file "tmp")
        _ (delete-recursively! root)]
    (try
      (cli/run! ["generate" "feature" "users" "--target" target])
      (is (.exists (io/file "tmp/demo/src/demo/features/users/routes.clj")))
      (finally
        (delete-recursively! root)))))
