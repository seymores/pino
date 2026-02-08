(ns pino.runtime.render.html-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.render.html :as html]))

(deftest renders-template-file
  (let [res (html/render {:template "users/index.html"
                          :data {:title "Users"}})]
    (is (= 200 (:status res)))
    (is (re-find #"Users" (:body res)))))
