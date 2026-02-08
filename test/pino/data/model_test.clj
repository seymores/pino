(ns pino.data.model-test
  (:require [clojure.test :refer [deftest is]]
            [pino.data.model :as model]))

(deftest model-schema-validates-entity
  (let [user (model/entity :user {:id 1 :email "a@b.com"})]
    (is (= :user (:model user)))
    (is (model/valid? user))))
