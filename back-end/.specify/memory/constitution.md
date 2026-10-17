<!--
Sync Impact Report
- Version change: unversioned template → 1.0.0
- Modified principles: PRINCIPLE_1_NAME → I. Existing Folder Architecture;
  PRINCIPLE_2_NAME → II. Boundary Input Validation; PRINCIPLE_3_NAME →
  III. Test-First Delivery; PRINCIPLE_4_NAME → IV. Standardized Error Handling;
  PRINCIPLE_5_NAME → V. Strict Typing and Layered Contracts
- Added sections: Technical Constraints; Development Workflow and Quality Gates
- Removed sections: none
- Follow-up TODOs: confirm the original ratification date
-->

# TODO List Back-end Constitution

## Core Principles

### I. Existing Folder Architecture
The back-end MUST preserve the established package and folder boundaries:
`app`, `controller`, `service`, `repository`, `domain`, `dto`, and `util`.
New responsibilities MUST be placed in the narrowest appropriate existing layer;
new top-level architectural patterns require a documented amendment before use.
This keeps the Clean Architecture separation predictable and limits coupling.

### II. Boundary Input Validation
Every input entering through a menu, controller, service public method, or DTO
boundary MUST be validated before business logic or persistence executes.
Validation MUST cover required values, formats, allowed ranges, and cross-field
invariants; invalid input MUST produce the standard error contract. Tests MUST
cover valid, invalid, and boundary values because persisted data and user-facing
behavior depend on rejecting malformed state at its first boundary.

### III. Test-First Delivery
Automated tests MUST be created or updated and demonstrate the expected behavior
before production implementation is written. The required sequence is failing
test, minimal implementation, and refactoring with tests green. Unit tests MUST
cover service and repository behavior, and controller or integration tests MUST
cover each changed boundary. This makes requirements executable and prevents
regressions from being discovered only after implementation.

### IV. Standardized Error Handling
Errors MUST use the established exception and user-interface flow rather than
ad hoc messages, silent fallbacks, or broad catches. Each failure MUST preserve
its cause, expose an actionable user-facing message at the interface boundary,
and avoid leaking internal implementation details. New error categories MUST
define their type, message contract, and tests before they are used.

### V. Strict Typing and Layered Contracts
Production Java code MUST use explicit domain, DTO, repository, service, and
controller types, with no raw collections, unchecked conversions, or
unnecessary casts. Each layer MUST depend only on the contract it needs, and
data transformations MUST be explicit at DTO and domain boundaries. Type
precision is required to make invalid states harder to represent and to keep
changes reviewable.

## Technical Constraints

The back-end MUST remain a Java 17-or-newer Gradle application using the
existing source layout under `src/main/java` and automated tests under
`src/test`. Persistence MUST continue to use the repository layer and Java NIO
file operations unless a separately approved amendment changes that constraint.
Lombok MAY reduce boilerplate only when it preserves clear, typed contracts.

## Development Workflow and Quality Gates

Every change MUST begin with a focused test plan and corresponding automated
tests before implementation. A change is complete only when the relevant
Gradle test task passes, compilation succeeds, validation and error paths are
covered, and the diff preserves the established package boundaries. Reviews
MUST verify these constitution rules and reject undocumented deviations.

## Governance

This constitution is the highest-priority project guidance for back-end
development. Amendments MUST describe the affected rule, rationale, migration
impact, and tests or quality gates required by the change. An amendment MUST
be reviewed together with all affected implementation and test changes.

The version follows semantic versioning: MAJOR for incompatible removals or
redefinitions, MINOR for new principles or materially expanded requirements,
and PATCH for clarifications that do not change obligations. Compliance MUST be
reviewed during every change review and at least once per release; violations
require either correction before merge or an explicitly documented amendment.

**Version**: 1.0.0 | **Ratified**: TODO(RATIFICATION_DATE): confirm original adoption date | **Last Amended**: 2026-10-17
