# Pino

Opinionated Clojure web framework for hybrid HTML + JSON apps.

## Quickstart

1. `clojure -M -m pino.cli.main new demo`
2. `clojure -M -m pino.cli.main generate feature users --target demo`
3. `clojure -M:test`

## Sample Usage App

Planned canonical DSL for the next iteration:

```clojure
(ns demo.app
  (:require [pino.dsl :refer [app route get]]))

(defn user-handler [req params]
  ;; params => {:id 123}
  {:status 200
   :body {:user-id (:id params)}})

(def get-user-by-id
  (get user-handler))

(def routes
  (route "/users/{id}"
    {:params {:id int?}}
    get-user-by-id))

(app routes)
```

API routes are explicit in path naming, for example:

```clojure
(def get-user-by-id-api
  (route "/api/users/{id}"
    {:params {:id int?}}
    (get user-handler)))
```

Note: the `route/get` syntax above is currently documented design direction and tracked in:
- `docs/plans/2026-02-08-route-dsl-revision-design.md`
