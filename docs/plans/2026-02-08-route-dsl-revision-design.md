# Pino Route DSL Revision Design (v1)

Date: 2026-02-08  
Status: Validated through brainstorming

## 1. Goal

Adopt a more composable and readable route DSL for v1 documentation and upcoming implementation work.

Target canonical usage:

```clojure
(def get-user-by-id
  (get user-handler))

(def routes
  (route "/users/{id}" get-user-by-id))

(app routes)
```

## 2. Validated Decisions

1. Canonical style is composable, two-step construction (`def` method route operation, then bind with `route`).
2. Handler receives `req` plus a second map of bound params:
   - `(defn user-handler [req params] ...)`
3. Path params are inferred from the path template (not repeated in `(get ...)`).
4. Multiple methods on one path are represented as separate `route` declarations.
5. API prefixing is explicit-only in v1:
   - use `/api/...` paths directly
   - no automatic API prefix helpers in this phase
6. Param typing/validation is declared at route level.

## 3. Canonical Forms

### 3.1 Method operation

```clojure
(get user-handler)
(post create-user-handler)
(put update-user-handler)
(delete delete-user-handler)
```

Method forms do not list path params; they are inferred from the route path.

### 3.2 Route declaration

```clojure
(route "/users/{id}" (get user-handler))
```

With route-level validation:

```clojure
(route "/users/{id}"
  {:params {:id int?}}
  (get user-handler))
```

### 3.3 App assembly

```clojure
(def get-user-by-id (get user-handler))
(def routes (route "/users/{id}" {:params {:id int?}} get-user-by-id))
(app routes)
```

## 4. Handler Contract

Handlers use:

```clojure
(fn [req params] ...)
```

`params` contains bound and validated values from route processing:

```clojure
{:id 123}
```

Validation occurs before handler execution. On failure, runtime returns format-aware errors (HTML-oriented vs JSON-oriented response shape), using existing error mapping facilities.

## 5. Runtime/Compiler Impact

No runtime model rewrite is needed. The change is a DSL surface evolution that compiles into existing manifest and route table structures.

Implementation impact areas:

1. Add parsing support for `route` + method operation forms.
2. Add method operation constructors (`get`, `post`, `put`, `delete`) to DSL namespace.
3. Extend manifest shape to represent route-level validation metadata.
4. Reuse runtime pipeline, error mapping, and async handling.

## 6. Compatibility Strategy

`page/api` DSL remains supported in v1 as compatibility syntax while docs/examples move to the new `route` canonical style.

Migration direction:

1. New docs and generated examples use `route` DSL.
2. Existing `page/api` apps keep working.
3. Later minor release may add migration helpers from `page/api` to `route`.

## 7. Out-of-Scope (This Revision)

1. Auto `/api` path prefixing helpers.
2. Positional param injection (e.g. passing `id` as separate arg).
3. Multi-method single-`route` container syntax.
4. HTMX/HTMLX-specific route semantics.

## 8. Next Step

Translate this design note into an implementation delta plan:

1. parser/macro additions
2. manifest updates
3. compiler wiring
4. tests (unit + acceptance) for new route DSL

