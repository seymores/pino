# Pino Framework Design (v1)

Date: 2026-02-08  
Status: Draft validated through brainstorming session

## 1. Summary

Pino is an opinionated Clojure web framework for building hybrid web applications quickly. It targets beginners who want Rails-like productivity while staying in idiomatic Clojure. The core design is a macro-first domain-specific language (DSL) that compiles into predictable data and runtime behavior, not hidden magic.

v1 focuses on:
- Hybrid applications with first-class HTML and JSON experiences.
- Paired route conventions (`/feature/...` for HTML and `/api/feature/...` for JSON).
- Async-first request handling.
- Built-in auth with SQL-backed sessions and OAuth/OIDC.
- Feature-first project structure.
- A strong CLI and scaffolding experience.

HTMX/HTMLX support is planned for a later iteration and is out of scope for v1.

## 2. Goals

- Make building conventional web apps fast for beginners.
- Keep framework behavior explicit and understandable.
- Provide a high-level DSL without locking teams out of lower-level Ring/Reitit/next.jdbc interop.
- Offer enough built-in capability to ship small-to-medium production apps without assembling many libraries manually.
- Ensure generated defaults are secure and production-safe.

## 3. Non-Goals (v1)

- Frontend reactive transport patterns (HTMX/WebSocket component protocol) as a primary model.
- Database migrations DSL.
- Microservice orchestration concerns.
- Multi-tenant enterprise governance toolchains.
- Non-SQL databases as first-class targets.

## 4. Primary Persona

Primary user: Clojure beginners who want convention-over-configuration productivity.

Secondary users: small teams needing quick app delivery with clear structure.

Implications:
- Strong conventions by default.
- Clear generated code layout.
- Excellent error messages with actionable fixes.
- Minimal mandatory architecture decisions before coding.

## 5. Core Principles

- Opinionated defaults first, override points second.
- Macro DSL for readability; data manifest for predictability.
- One canonical project layout (feature-first).
- Shared domain logic across HTML and JSON surfaces.
- Security defaults enabled out of the box.
- Fast local development loops.

## 6. High-Level Architecture

Pino has three layers:

1. DSL Layer  
Macros define app intent: features, resources, pages, APIs, models, policies, actions.

2. Compile Layer  
Macros expand into a normalized app manifest (EDN-like data). The compiler validates declarations and generates runtime artifacts (routes, middleware graph, handlers, schema/query metadata, policy bindings).

3. Runtime Layer  
Built on Ring + Reitit + next.jdbc adapter system. Runtime executes async request pipeline, policy checks, validation, domain logic, and renderer selection.

Core runtime libraries:
- HTTP: Ring
- Routing: Reitit
- Data access: next.jdbc with adapter abstraction
- Sessions: SQL-backed session store

## 7. DSL Surface (v1)

### 7.1 Top-level forms

- `app`: global configuration, middleware defaults, auth setup, DB adapters.
- `feature`: bounded module that owns routes, templates, policies, model declarations, tests.
- `model`: entity schema, constraints, relations, query declarations.
- `resource`: REST route scaffolding and serializer/policy hooks.
- `page`: HTML endpoint declaration.
- `api`: JSON endpoint declaration (REST-first semantics).
- `action`: explicit state mutation endpoint.
- `policy`: authz/authn rules and route guards.

### 7.2 DSL style requirements

- Macro-first and highly declarative.
- Keyword options with strong defaults.
- Clear compile-time validation messages.
- Expansion output inspectable in dev mode.

### 7.3 Route pairing convention

Default pairing model:
- HTML route path: `/<feature>/...`
- JSON route path: `/api/<feature>/...`

Domain logic should be shared between both route surfaces through generated service namespaces or explicit service references.

### 7.4 Rendering model

Default HTML renderer:
- Template files with embedded expressions (not Hiccup-first).

Default JSON renderer:
- REST-first with conventional status and error contracts.

## 8. Feature-First Project Layout

Canonical structure for generated apps:

`src/<app>/features/<feature>/`
- `routes.clj`
- `api.clj`
- `pages.clj`
- `actions.clj`
- `models.clj`
- `queries.clj`
- `policies.clj`
- `services.clj`
- `views/` (template files)
- `test/` (unit-first test scaffolds)

Framework-level directories:
- `src/<app>/app.clj` (global app DSL)
- `config/` (env config)
- `resources/` (templates/static)
- `test/` (cross-feature tests)

## 9. Model and Query DSL (Light, Powerful)

v1 model layer includes:
- Field definitions and type constraints.
- Validation rules.
- Relationship declarations.
- Named query declarations.
- Generated repository API.

v1 excludes:
- Migration generation/execution DSL.

Query/repository behavior:
- SQL-first adapter layer.
- PostgreSQL, MySQL, SQLite support from day one.
- Named query declarations compiled into parameterized SQL execution paths.
- Generated CRUD operations and query functions with consistent return semantics.

## 10. Runtime Request Lifecycle

Async-first pipeline for all handlers:

1. Request parse and context build.
2. Route resolution (Reitit).
3. Session/authentication resolution.
4. Policy enforcement.
5. Parameter coercion/validation.
6. Domain service invocation.
7. Render (template or JSON serializer).
8. Response normalization (headers/status/cookies).
9. Error mapping fallback.

Requirements:
- All generated handlers operate under async-capable contracts.
- Sync-style user logic can still be authored ergonomically.
- Middleware order is stable and documented.

## 11. Authentication, Authorization, Security

v1 auth stack:
- SQL-backed sessions (default).
- OAuth/OIDC built in from day one.
- Route and feature-level policy DSL.

Security defaults:
- CSRF protection enabled for HTML forms.
- Secure session cookie defaults.
- Common security headers enabled.
- Clear policy denial behavior for HTML vs JSON.

## 12. Error Handling and Developer Experience

Compile-time error goals:
- Reject malformed DSL declarations early.
- Include exact failing form, expected shape, and correction hint.

Runtime error goals:
- Map domain errors into format-aware responses.
- HTML routes return user-friendly pages/messages.
- API routes return structured JSON errors.
- Dev mode includes enhanced diagnostics and source linkage.
- Production mode sanitizes internals while preserving correlation IDs.

## 13. CLI and Scaffolding (Core Capability)

CLI is a first-class product surface in v1.

Required commands:
- New app bootstrap.
- Generate feature.
- Generate model.
- Generate resource.
- Set up auth (session + OAuth/OIDC).
- Run dev server.
- Run unit tests.
- Run framework checks/lint-style validations.

CLI output requirements:
- Deterministic structure.
- Idempotent safety checks when possible.
- Clear next-step guidance after generation.

## 14. Testing Strategy (Unit-First)

Default testing model:
- Unit-first for fast feedback loops.
- Generated tests per feature for models, services, policies, handlers.

Framework support:
- Test helpers for auth/session mocking.
- Repository abstraction mocks/stubs.
- Simple request simulation for handler-level tests.

Integration tests:
- Supported, but not the default emphasis in v1 scaffolding.

## 15. v1 Acceptance Criteria

v1 is considered successful when all criteria are met:

- New app can be generated and run with a single command sequence.
- A generated feature can serve both HTML and JSON using route pairing conventions.
- Model DSL can define schema + named queries and execute against PostgreSQL/MySQL/SQLite through the same API.
- OAuth/OIDC login and SQL sessions work with framework defaults.
- Async handler pipeline is functional and documented.
- Unit test scaffolding is generated and runnable for each new feature.
- Error messages are actionable in both compile and runtime contexts.
- Beginners can add a new feature without touching global plumbing code.

## 16. Out-of-Scope Backlog (v2+)

- HTMX/HTMLX interaction DSL and partial rendering conventions.
- Migration DSL.
- Realtime primitives.
- Richer API protocol options beyond REST defaults.
- Optional componentized view systems beyond template-file default.

## 17. Suggested Next Step

Create a formal implementation plan from this spec:
- Milestones and sequencing.
- Internal compiler/runtime boundaries.
- Acceptance test matrix per milestone.

