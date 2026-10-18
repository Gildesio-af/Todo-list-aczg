# Implementation Plan: Task alarms

**Branch**: `feat/notification` | **Date**: 2026-10-18 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from [spec.md](./spec.md)

## Summary

Add up to three task alarms, persisted in a dedicated `alarms.csv`, with
validation based on task dates, one-shot notifications, repetition for the
nearest alarm, and a one-minute in-process monitor. The implementation follows
the existing `app -> controller -> service -> repository -> domain` flow,
uses Java NIO and a semicolon-delimited file, and injects `Clock` plus
notification/scheduling collaborators so time and lifecycle behavior remain
testable.

## Technical Context

**Language/Version**: Java 17+

**Primary Dependencies**: Existing Gradle application, Groovy 4, Spock 2.3,
Lombok where it preserves explicit contracts, and JDK `java.time`,
`ScheduledExecutorService`, and Java NIO

**Storage**: Dedicated semicolon-delimited `alarms.csv`, accessed only through
the repository layer; existing task/category/user files remain unchanged

**Testing**: Spock unit tests for DTO/domain, service, repository, monitor,
controller/menu integration, and Gradle `test`/`build`

**Target Platform**: JVM command-line application on the existing supported
platforms

**Project Type**: Interactive CLI application

**Performance Goals**: Poll monitorable alarms once per minute; accept up to
59 seconds of polling lateness; no duplicate notification for the same
persisted trigger instant

**Constraints**: Maximum three alarms per task; minimum one-minute positive
advance; advance strictly shorter than task duration; only TODO/DOING tasks
fire; controlled scheduler shutdown; explicit persistence failures; no new
top-level package or dependency

**Scale/Scope**: Local single-user CLI with a small alarm file; changes limited
to alarm domain/DTO/service/repository/monitor/controller/menu wiring and
related tests

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Existing architecture**: PASS. Alarm responsibilities fit the established
  `app`, `controller`, `service`, `repository`, `domain`, `dto`, and `util`
  boundaries.
- **Boundary validation**: PASS by design. DTOs and public service methods
  validate task association, duration, advance, trigger time, status, and the
  three-alarm limit before persistence.
- **Test-first delivery**: PASS by plan. Spock tests are specified before
  implementation for validation, state transitions, CSV failures, monitor
  timing/lifecycle, and CLI/controller behavior.
- **Error handling**: PASS by design. A dedicated persistence exception will
  preserve I/O/parsing causes; malformed records will fail explicitly rather
  than being skipped.
- **Strict typing/layered contracts**: PASS. Domain, DTO, repository, service,
  controller, and monitor APIs use explicit Java types and constructor
  injection.
- **Technical constraints**: PASS. Java 17+, Gradle, Java NIO, existing
  namespace and test layout are retained; no dependency or package-boundary
  changes are required.

## Project Structure

### Documentation

```text
docs/
├── spec.md
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
   └── alarm-cli.md
```

### Source Code

```text
src/main/java/zg/acelera/
├── app/                 # Task menu and CLI flow
├── controller/          # Alarm/task boundary coordination
├── domain/              # Alarm and existing task entities
├── dto/                 # Alarm input/update records
├── repository/          # Alarm repository and CSV implementation
├── service/             # Alarm use cases and monitor
└── util/                # Persistence exception and notifier contract

src/test/groovy/
├── controller/
├── repository/
└── service/
```

**Structure Decision**: Extend the existing single Gradle CLI project in its
current `zg.acelera` package layout. The monitor is a service-layer
collaborator, the repository owns all CSV access, and the menu/controller
remain the only user-facing boundary. No front-end or new architectural layer
is introduced.

## Complexity Tracking

No violations. The design conforms to the constitution.
