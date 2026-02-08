(ns pino.runtime.errors-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.errors :as errors]))

(deftest domain-error-maps-by-format
  (let [html (errors/map-error {:type :validation/failed} :html)
        api (errors/map-error {:type :validation/failed} :json)]
    (is (= 422 (:status html)))
    (is (= 422 (:status api)))))
