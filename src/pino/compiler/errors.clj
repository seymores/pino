(ns pino.compiler.errors)

(defn invalid-manifest
  [path explain-data]
  (ex-info "Invalid manifest"
           {:type :manifest/invalid
            :path path
            :explain explain-data}))
