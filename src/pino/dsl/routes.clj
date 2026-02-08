(ns pino.dsl.routes)

(defn route-type
  [sym]
  (let [n (name sym)]
    (cond
      (or (= n "page")
          (= n "dsl/page")
          (= n "pino.dsl/page")) :page
      (or (= n "api")
          (= n "dsl/api")
          (= n "pino.dsl/api")) :api
      :else nil)))
