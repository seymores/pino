(ns pino.docs.smoke-test
  (:require [clojure.test :refer [deftest is]]
            [pino.docs.smoke :as docs-smoke]))

(deftest readme-commands-run
  (is (= 0 (:exit (docs-smoke/run! "README.md")))))
