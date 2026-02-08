(ns pino.dev.diagnostics)

(defn format-compile-error
  [{:keys [path line col]}]
  {:path path
   :line line
   :col col})
