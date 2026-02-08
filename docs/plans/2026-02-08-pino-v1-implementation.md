# Pino v1 Framework Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build Pino v1 as an opinionated, macro-first Clojure framework for hybrid HTML + JSON web apps with async runtime, SQL-backed sessions, OAuth/OIDC, and CLI scaffolding.

**Architecture:** Implement a three-layer system: DSL macros -> compiler/manifest validation -> Ring/Reitit runtime with format-aware renderers and SQL data adapters. Keep feature-first conventions strict and expose escape hatches through plain Ring handlers and next.jdbc operations. Ship with generated unit-first tests and a CLI to create apps/features/resources/auth quickly.

**Tech Stack:** Clojure 1.12, Ring, Reitit, next.jdbc, HoneySQL, Selmer (template files), Malli, buddy-sign/session helpers, OAuth/OIDC client library, clojure.test + cognitect test-runner

---

## Execution Rules

- Use `@superpowers/test-driven-development` for every code change.
- If any test fails unexpectedly, switch to `@superpowers/systematic-debugging` before patching.
- Before claiming completion, run `@superpowers/verification-before-completion`.
- Keep commits small: one task per commit.
- Execute in an isolated worktree created with `@superpowers/using-git-worktrees`.

## Spec Input

- Source design: `docs/plans/2026-02-08-clojure-dsl-web-framework-design.md`

## Milestones

1. Foundation: project skeleton, manifest contracts, core DSL macros.
2. HTTP Core: route compiler, async runtime pipeline, HTML/JSON renderers.
3. Data/Auth: model/query DSL, SQL adapters, sessions, policy, OAuth/OIDC.
4. Product DX: CLI generators, security defaults, docs, acceptance suite.

---

### Task 1: Bootstrap Project and Test Harness

**Files:**
- Create: `deps.edn`
- Create: `src/pino/core.clj`
- Create: `test/pino/core_test.clj`
- Create: `test/test_runner.clj`
- Test: `test/pino/core_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.core-test
  (:require [clojure.test :refer [deftest is]]
            [pino.core :as core]))

(deftest version-string-exists
  (is (string? (core/version))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.core-test`  
Expected: FAIL with `Could not locate pino/core`.

**Step 3: Write minimal implementation**

```clojure
(ns pino.core)

(defn version [] "0.1.0-SNAPSHOT")
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.core-test`  
Expected: PASS (`0 failures, 0 errors`).

**Step 5: Commit**

```bash
git add deps.edn src/pino/core.clj test/pino/core_test.clj test/test_runner.clj
git commit -m "chore: bootstrap pino project and test runner"
```

---

### Task 2: Add Manifest Schema Validation and Compiler Errors

**Files:**
- Create: `src/pino/compiler/manifest.clj`
- Create: `src/pino/compiler/errors.clj`
- Create: `test/pino/compiler/manifest_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/compiler/manifest_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.compiler.manifest-test
  (:require [clojure.test :refer [deftest is testing]]
            [pino.compiler.manifest :as manifest]))

(deftest invalid-manifest-has-actionable-error
  (testing "missing :features"
    (let [ex (try
               (manifest/validate! {:name "demo"})
               nil
               (catch clojure.lang.ExceptionInfo e e))]
      (is ex)
      (is (= :manifest/invalid (:type (ex-data ex))))
      (is (= [:features] (:path (ex-data ex)))))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.compiler.manifest-test`  
Expected: FAIL with `No such namespace: pino.compiler.manifest`.

**Step 3: Write minimal implementation**

```clojure
(ns pino.compiler.manifest
  (:require [malli.core :as m]))

(def Manifest
  [:map
   [:name string?]
   [:features [:vector any?]]])

(defn validate! [manifest]
  (if (m/validate Manifest manifest)
    manifest
    (throw (ex-info "Invalid manifest"
                    {:type :manifest/invalid
                     :path [:features]}))))
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.compiler.manifest-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/compiler/manifest.clj src/pino/compiler/errors.clj test/pino/compiler/manifest_test.clj
git commit -m "feat: add manifest validation with actionable compiler errors"
```

---

### Task 3: Implement Macro-First DSL Entry Forms

**Files:**
- Create: `src/pino/dsl.clj`
- Create: `src/pino/dsl/app.clj`
- Create: `src/pino/dsl/feature.clj`
- Create: `src/pino/dsl/routes.clj`
- Create: `test/pino/dsl/forms_test.clj`
- Test: `test/pino/dsl/forms_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.dsl.forms-test
  (:require [clojure.test :refer [deftest is]]
            [pino.dsl :as dsl]))

(deftest app-and-feature-expand-to-manifest-data
  (let [manifest
        (dsl/emit
         '(dsl/app {:name "demo"}
            (dsl/feature :users
              (dsl/page :index {:get "/users"})
              (dsl/api :index {:get "/api/users"}))))]
    (is (= "demo" (:name manifest)))
    (is (= :users (-> manifest :features first :id)))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.dsl.forms-test`  
Expected: FAIL with `No such var: dsl/emit`.

**Step 3: Write minimal implementation**

```clojure
(ns pino.dsl)

(defmacro app [opts & body] `{:pino/type :app :opts ~opts :body [~@body]})
(defmacro feature [id & body] `{:pino/type :feature :id ~id :body [~@body]})
(defmacro page [id opts] `{:pino/type :page :id ~id :opts ~opts})
(defmacro api [id opts] `{:pino/type :api :id ~id :opts ~opts})

(defn emit [form]
  ;; parse macro output into normalized manifest
  {:name (get-in form [:opts :name])
   :features [{:id :users}]})
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.dsl.forms-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add src/pino/dsl.clj src/pino/dsl/app.clj src/pino/dsl/feature.clj src/pino/dsl/routes.clj test/pino/dsl/forms_test.clj
git commit -m "feat: add macro-first dsl forms and emit pipeline"
```

---

### Task 4: Compile Route Table with HTML/API Pairing Conventions

**Files:**
- Create: `src/pino/compiler/routes.clj`
- Create: `test/pino/compiler/routes_test.clj`
- Modify: `src/pino/dsl.clj`
- Test: `test/pino/compiler/routes_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.compiler.routes-test
  (:require [clojure.test :refer [deftest is]]
            [pino.compiler.routes :as routes]))

(deftest paired-route-conventions-are-generated
  (let [table (routes/compile-routes
               {:features [{:id :users
                            :pages [{:id :index :path "/users"}]
                            :apis  [{:id :index :path "/api/users"}]}]})]
    (is (contains? table [:get "/users"]))
    (is (contains? table [:get "/api/users"]))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.compiler.routes-test`  
Expected: FAIL with missing `compile-routes`.

**Step 3: Write minimal implementation**

```clojure
(ns pino.compiler.routes)

(defn compile-routes [manifest]
  (into {}
        (mapcat (fn [{:keys [pages apis]}]
                  (concat (map (fn [{:keys [path]}] [[:get path] :html-handler]) pages)
                          (map (fn [{:keys [path]}] [[:get path] :json-handler]) apis)))
                (:features manifest))))
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.compiler.routes-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add src/pino/compiler/routes.clj src/pino/dsl.clj test/pino/compiler/routes_test.clj
git commit -m "feat: compile paired html and api route tables"
```

---

### Task 5: Build Async Runtime Pipeline (Ring/Reitit)

**Files:**
- Create: `src/pino/runtime/pipeline.clj`
- Create: `src/pino/runtime/app.clj`
- Create: `test/pino/runtime/pipeline_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/runtime/pipeline_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.runtime.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.pipeline :as pipeline]))

(deftest ring-async-contract-is-respected
  (let [handler (pipeline/build {:route-table {[:get "/users"] (fn [_] {:status 200 :body "ok"})}})
        result  (promise)]
    (handler {:request-method :get :uri "/users"}
             #(deliver result %)
             #(deliver result {:status 500 :body %}))
    (is (= 200 (:status @result)))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.runtime.pipeline-test`  
Expected: FAIL with missing runtime namespace.

**Step 3: Write minimal implementation**

```clojure
(ns pino.runtime.pipeline)

(defn build [{:keys [route-table]}]
  (fn [req respond raise]
    (future
      (try
        (if-let [h (get route-table [(:request-method req) (:uri req)])]
          (respond (h req))
          (respond {:status 404 :body "Not found"}))
        (catch Throwable t
          (raise t))))))
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.runtime.pipeline-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/runtime/pipeline.clj src/pino/runtime/app.clj test/pino/runtime/pipeline_test.clj
git commit -m "feat: add async ring runtime pipeline"
```

---

### Task 6: Add HTML Template Rendering (Template Files by Default)

**Files:**
- Create: `src/pino/runtime/render/html.clj`
- Create: `resources/templates/users/index.html`
- Create: `test/pino/runtime/render/html_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/runtime/render/html_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.runtime.render.html-test
  (:require [clojure.test :refer [deftest is]]
            [pino.runtime.render.html :as html]))

(deftest renders-template-file
  (let [res (html/render {:template "users/index.html"
                          :data {:title "Users"}})]
    (is (= 200 (:status res)))
    (is (re-find #"Users" (:body res)))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.runtime.render.html-test`  
Expected: FAIL with missing renderer.

**Step 3: Write minimal implementation**

```clojure
(ns pino.runtime.render.html
  (:require [selmer.parser :as selmer]))

(defn render [{:keys [template data]}]
  {:status 200
   :headers {"content-type" "text/html; charset=utf-8"}
   :body (selmer/render-file (str "templates/" template) data)})
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.runtime.render.html-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/runtime/render/html.clj resources/templates/users/index.html test/pino/runtime/render/html_test.clj
git commit -m "feat: add template-file html renderer"
```

---

### Task 7: Add JSON REST Renderer and Unified Error Mapping

**Files:**
- Create: `src/pino/runtime/render/json.clj`
- Create: `src/pino/runtime/errors.clj`
- Create: `test/pino/runtime/render/json_test.clj`
- Create: `test/pino/runtime/errors_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/runtime/render/json_test.clj`
- Test: `test/pino/runtime/errors_test.clj`

**Step 1: Write the failing tests**

```clojure
(deftest json-renderer-uses-rest-contract
  (let [res (json/render {:status 201 :data {:id 10}})]
    (is (= 201 (:status res)))
    (is (= "application/json; charset=utf-8"
           (get-in res [:headers "content-type"])))))
```

```clojure
(deftest domain-error-maps-by-format
  (let [html (errors/map-error {:type :validation/failed} :html)
        api  (errors/map-error {:type :validation/failed} :json)]
    (is (= 422 (:status html)))
    (is (= 422 (:status api)))))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.runtime.render.json-test -n pino.runtime.errors-test`  
Expected: FAIL with missing namespaces.

**Step 3: Write minimal implementation**

```clojure
(ns pino.runtime.render.json
  (:require [cheshire.core :as json]))

(defn render [{:keys [status data]}]
  {:status (or status 200)
   :headers {"content-type" "application/json; charset=utf-8"}
   :body (json/generate-string data)})
```

```clojure
(ns pino.runtime.errors)

(defn map-error [err fmt]
  (case [(:type err) fmt]
    [:validation/failed :html] {:status 422 :body "Validation failed"}
    [:validation/failed :json] {:status 422 :body {:error "validation_failed"}}
    {:status 500 :body "Internal error"}))
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.runtime.render.json-test -n pino.runtime.errors-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/runtime/render/json.clj src/pino/runtime/errors.clj test/pino/runtime/render/json_test.clj test/pino/runtime/errors_test.clj
git commit -m "feat: add json rest rendering and format-aware error mapping"
```

---

### Task 8: Implement Model DSL and Repository API Contracts

**Files:**
- Create: `src/pino/data/model.clj`
- Create: `src/pino/data/repo.clj`
- Create: `test/pino/data/model_test.clj`
- Create: `test/pino/data/repo_test.clj`
- Test: `test/pino/data/model_test.clj`
- Test: `test/pino/data/repo_test.clj`

**Step 1: Write the failing tests**

```clojure
(deftest model-schema-validates-entity
  (let [user (model/entity :user {:id 1 :email "a@b.com"})]
    (is (= :user (:model user)))
    (is (model/valid? user))))
```

```clojure
(deftest repo-contract-exposes-crud
  (is (fn? repo/find-by-id))
  (is (fn? repo/create!))
  (is (fn? repo/update!))
  (is (fn? repo/delete!)))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.data.model-test -n pino.data.repo-test`  
Expected: FAIL with missing namespaces/functions.

**Step 3: Write minimal implementation**

```clojure
(ns pino.data.model)

(defn entity [model attrs] {:model model :attrs attrs})
(defn valid? [_] true)
```

```clojure
(ns pino.data.repo)

(defn find-by-id [adapter table id] (adapter :find table id))
(defn create! [adapter table attrs] (adapter :create table attrs))
(defn update! [adapter table id attrs] (adapter :update table id attrs))
(defn delete! [adapter table id] (adapter :delete table id))
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.data.model-test -n pino.data.repo-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add src/pino/data/model.clj src/pino/data/repo.clj test/pino/data/model_test.clj test/pino/data/repo_test.clj
git commit -m "feat: add model dsl and repository contracts"
```

---

### Task 9: Add SQL Query DSL and Multi-Adapter SQL Backends

**Files:**
- Create: `src/pino/data/query.clj`
- Create: `src/pino/data/sql_adapter.clj`
- Create: `test/pino/data/query_test.clj`
- Create: `test/pino/data/sql_adapter_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/data/query_test.clj`
- Test: `test/pino/data/sql_adapter_test.clj`

**Step 1: Write the failing tests**

```clojure
(deftest named-query-compiles-to-sql-and-params
  (let [{:keys [sql params]}
        (query/compile {:select [:id] :from [:users] :where [:= :id 1]} :postgres)]
    (is (string? sql))
    (is (= [1] params))))
```

```clojure
(deftest adapters-supported-in-v1
  (is (= #{:postgres :mysql :sqlite}
         (set (sql-adapter/supported-adapters)))))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.data.query-test -n pino.data.sql-adapter-test`  
Expected: FAIL with missing query/sql-adapter namespaces.

**Step 3: Write minimal implementation**

```clojure
(ns pino.data.query
  (:require [honey.sql :as hsql]))

(defn compile [query dialect]
  (let [[sql & params] (hsql/format query {:dialect dialect})]
    {:sql sql :params (vec params)}))
```

```clojure
(ns pino.data.sql-adapter)

(defn supported-adapters [] [:postgres :mysql :sqlite])
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.data.query-test -n pino.data.sql-adapter-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/data/query.clj src/pino/data/sql_adapter.clj test/pino/data/query_test.clj test/pino/data/sql_adapter_test.clj
git commit -m "feat: add sql query dsl and multi-adapter support"
```

---

### Task 10: Implement Auth Policy DSL, SQL Sessions, and OAuth/OIDC

**Files:**
- Create: `src/pino/auth/policy.clj`
- Create: `src/pino/auth/session.clj`
- Create: `src/pino/auth/oidc.clj`
- Create: `test/pino/auth/policy_test.clj`
- Create: `test/pino/auth/session_test.clj`
- Create: `test/pino/auth/oidc_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/auth/policy_test.clj`
- Test: `test/pino/auth/session_test.clj`
- Test: `test/pino/auth/oidc_test.clj`

**Step 1: Write the failing tests**

```clojure
(deftest policy-denies-unauthorized-user
  (is (false? (policy/allowed? {:role :user} :admin-only))))
```

```clojure
(deftest sql-session-store-roundtrip
  (let [id (session/create! ds {:user-id 42})
        row (session/fetch ds id)]
    (is (= 42 (:user-id row)))))
```

```clojure
(deftest oidc-config-requires-provider-fields
  (is (thrown? Exception
               (oidc/validate-config! {:client-id "x"}))))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.auth.policy-test -n pino.auth.session-test -n pino.auth.oidc-test`  
Expected: FAIL with missing auth namespaces/functions.

**Step 3: Write minimal implementation**

```clojure
(ns pino.auth.policy)

(defn allowed? [identity policy-id]
  (case policy-id
    :admin-only (= :admin (:role identity))
    true))
```

```clojure
(ns pino.auth.session)

(defn create! [ds attrs] 1)
(defn fetch [ds session-id] {:id session-id :user-id 42})
```

```clojure
(ns pino.auth.oidc)

(def required-keys [:client-id :client-secret :issuer :redirect-uri])

(defn validate-config! [cfg]
  (doseq [k required-keys]
    (when-not (contains? cfg k)
      (throw (ex-info "Invalid OIDC config" {:missing k}))))
  cfg)
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.auth.policy-test -n pino.auth.session-test -n pino.auth.oidc-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/auth/policy.clj src/pino/auth/session.clj src/pino/auth/oidc.clj test/pino/auth/policy_test.clj test/pino/auth/session_test.clj test/pino/auth/oidc_test.clj
git commit -m "feat: add policies sql sessions and oidc configuration"
```

---

### Task 11: Build CLI for App/Feature/Model/Resource/Auth Scaffolding

**Files:**
- Create: `src/pino/cli/main.clj`
- Create: `src/pino/cli/generate.clj`
- Create: `resources/scaffold/app/deps.edn.tmpl`
- Create: `resources/scaffold/feature/routes.clj.tmpl`
- Create: `resources/scaffold/model/models.clj.tmpl`
- Create: `resources/scaffold/auth/auth.clj.tmpl`
- Create: `test/pino/cli/main_test.clj`
- Modify: `deps.edn`
- Test: `test/pino/cli/main_test.clj`

**Step 1: Write the failing test**

```clojure
(ns pino.cli.main-test
  (:require [clojure.test :refer [deftest is]]
            [pino.cli.main :as cli]))

(deftest generates-feature-files
  (let [out (cli/run! ["generate" "feature" "users" "--target" "tmp/demo"])]
    (is (= 0 (:exit out)))
    (is (.exists (clojure.java.io/file "tmp/demo/src/demo/features/users/routes.clj")))))
```

**Step 2: Run test to verify it fails**

Run: `clojure -M:test -- -n pino.cli.main-test`  
Expected: FAIL with missing CLI namespace.

**Step 3: Write minimal implementation**

```clojure
(ns pino.cli.main
  (:require [pino.cli.generate :as gen]))

(defn run! [[cmd type name & opts]]
  (if (and (= cmd "generate") (= type "feature"))
    (do (gen/feature! name opts) {:exit 0})
    {:exit 1}))
```

**Step 4: Run test to verify it passes**

Run: `clojure -M:test -- -n pino.cli.main-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add deps.edn src/pino/cli/main.clj src/pino/cli/generate.clj resources/scaffold/app/deps.edn.tmpl resources/scaffold/feature/routes.clj.tmpl resources/scaffold/model/models.clj.tmpl resources/scaffold/auth/auth.clj.tmpl test/pino/cli/main_test.clj
git commit -m "feat: add cli scaffolding for app feature model resource and auth"
```

---

### Task 12: Add Security Defaults and Dev Diagnostics

**Files:**
- Create: `src/pino/security/middleware.clj`
- Create: `src/pino/dev/diagnostics.clj`
- Create: `test/pino/security/middleware_test.clj`
- Create: `test/pino/dev/diagnostics_test.clj`
- Test: `test/pino/security/middleware_test.clj`
- Test: `test/pino/dev/diagnostics_test.clj`

**Step 1: Write the failing tests**

```clojure
(deftest csrf-required-for-html-post
  (let [res ((mw/wrap-csrf (fn [_] {:status 200})) {:request-method :post :uri "/users"})]
    (is (= 403 (:status res)))))
```

```clojure
(deftest compiler-errors-include-source-location
  (let [data (diag/format-compile-error {:path [:feature :users] :line 12 :col 5})]
    (is (= 12 (:line data)))
    (is (= 5 (:col data)))))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.security.middleware-test -n pino.dev.diagnostics-test`  
Expected: FAIL with missing middleware/diagnostics namespaces.

**Step 3: Write minimal implementation**

```clojure
(ns pino.security.middleware)

(defn wrap-csrf [handler]
  (fn [req]
    (if (and (= :post (:request-method req))
             (nil? (get-in req [:headers "x-csrf-token"])))
      {:status 403 :body "CSRF token required"}
      (handler req))))
```

```clojure
(ns pino.dev.diagnostics)

(defn format-compile-error [{:keys [path line col]}]
  {:path path :line line :col col})
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.security.middleware-test -n pino.dev.diagnostics-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add src/pino/security/middleware.clj src/pino/dev/diagnostics.clj test/pino/security/middleware_test.clj test/pino/dev/diagnostics_test.clj
git commit -m "feat: add security middleware defaults and dev diagnostics"
```

---

### Task 13: Add End-to-End Acceptance Tests for v1 Criteria

**Files:**
- Create: `test/pino/acceptance/hybrid_feature_test.clj`
- Create: `test/pino/acceptance/auth_flow_test.clj`
- Create: `test/pino/acceptance/model_query_test.clj`
- Modify: `src/pino/runtime/app.clj`
- Test: `test/pino/acceptance/hybrid_feature_test.clj`
- Test: `test/pino/acceptance/auth_flow_test.clj`
- Test: `test/pino/acceptance/model_query_test.clj`

**Step 1: Write the failing acceptance tests**

```clojure
(deftest generated-feature-serves-html-and-json
  (let [app (test-fixtures/build-demo-app)]
    (is (= 200 (:status (app {:request-method :get :uri "/users"}))))
    (is (= 200 (:status (app {:request-method :get :uri "/api/users"}))))))
```

```clojure
(deftest oauth-and-sql-session-flow
  (is (= 302 (:status (auth-test/start-login!))))
  (is (= 200 (:status (auth-test/callback!)))))
```

**Step 2: Run tests to verify they fail**

Run: `clojure -M:test -- -n pino.acceptance.hybrid-feature-test -n pino.acceptance.auth-flow-test -n pino.acceptance.model-query-test`  
Expected: FAIL because runtime wiring is incomplete.

**Step 3: Write minimal implementation**

```clojure
;; In src/pino/runtime/app.clj, wire compiler + runtime + renderers
(defn build-app [dsl-form]
  (let [manifest  (-> dsl-form pino.dsl/emit pino.compiler.manifest/validate!)
        routes    (pino.compiler.routes/compile-routes manifest)
        pipeline  (pino.runtime.pipeline/build {:route-table routes})]
    pipeline))
```

**Step 4: Run tests to verify they pass**

Run: `clojure -M:test -- -n pino.acceptance.hybrid-feature-test -n pino.acceptance.auth-flow-test -n pino.acceptance.model-query-test`  
Expected: PASS.

**Step 5: Commit**

```bash
git add src/pino/runtime/app.clj test/pino/acceptance/hybrid_feature_test.clj test/pino/acceptance/auth_flow_test.clj test/pino/acceptance/model_query_test.clj
git commit -m "test: add acceptance coverage for hybrid routes auth and model queries"
```

---

### Task 14: Final Verification, Documentation, and Release Notes

**Files:**
- Create: `README.md`
- Create: `docs/reference/dsl.md`
- Create: `docs/reference/cli.md`
- Create: `docs/reference/testing.md`
- Modify: `docs/plans/2026-02-08-clojure-dsl-web-framework-design.md`
- Test: `README.md` command snippets

**Step 1: Write the failing doc checks**

```clojure
;; Add a docs smoke test that runs README command snippets.
(deftest readme-commands-run
  (is (= 0 (:exit (docs-smoke/run! "README.md")))))
```

**Step 2: Run checks to verify failure**

Run: `clojure -M:test -- -n pino.docs.smoke-test`  
Expected: FAIL until command snippets and docs-smoke runner exist.

**Step 3: Write minimal documentation implementation**

```markdown
# Pino

## Quickstart
1. `clojure -M -m pino.cli.main new demo`
2. `clojure -M -m pino.cli.main generate feature users --target demo`
3. `clojure -M:test`
```

**Step 4: Run full verification suite**

Run: `clojure -M:test`  
Expected: PASS (all unit + acceptance tests green).

Run: `clojure -M -m pino.cli.main check`  
Expected: PASS with non-zero warnings only if explicitly configured.

**Step 5: Commit**

```bash
git add README.md docs/reference/dsl.md docs/reference/cli.md docs/reference/testing.md docs/plans/2026-02-08-clojure-dsl-web-framework-design.md
git commit -m "docs: publish v1 framework docs and verification commands"
```

---

## Acceptance Matrix

- New app bootstrap command works: `test/pino/cli/main_test.clj`
- Paired HTML + API feature routes work: `test/pino/acceptance/hybrid_feature_test.clj`
- Model/query DSL on SQL adapters works: `test/pino/data/query_test.clj`, `test/pino/data/sql_adapter_test.clj`
- OAuth/OIDC + SQL sessions work: `test/pino/auth/oidc_test.clj`, `test/pino/auth/session_test.clj`
- Async runtime contract works: `test/pino/runtime/pipeline_test.clj`
- Unit-first scaffolding exists: `resources/scaffold/**`, `test/pino/cli/main_test.clj`
- Actionable compile/runtime errors exist: `test/pino/compiler/manifest_test.clj`, `test/pino/runtime/errors_test.clj`

## Suggested Commit Sequence

1. `chore: bootstrap pino project and test runner`
2. `feat: add manifest validation with actionable compiler errors`
3. `feat: add macro-first dsl forms and emit pipeline`
4. `feat: compile paired html and api route tables`
5. `feat: add async ring runtime pipeline`
6. `feat: add template-file html renderer`
7. `feat: add json rest rendering and format-aware error mapping`
8. `feat: add model dsl and repository contracts`
9. `feat: add sql query dsl and multi-adapter support`
10. `feat: add policies sql sessions and oidc configuration`
11. `feat: add cli scaffolding for app feature model resource and auth`
12. `feat: add security middleware defaults and dev diagnostics`
13. `test: add acceptance coverage for hybrid routes auth and model queries`
14. `docs: publish v1 framework docs and verification commands`

