---

description: "Task list for implementing the task alarm feature"
---

# Tasks: Task alarms

**Input**: Design documents from `docs/plan.md`, `docs/spec.md`,
`docs/data-model.md`, `docs/contracts/alarm-cli.md`, and `docs/quickstart.md`

**Prerequisites**: Java 17+, Gradle Wrapper, existing `zg.acelera` package
layout, and the current task repository/service/controller/menu contracts.

**Organization**: Tasks are grouped by user story so each increment can be
implemented and validated independently.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Establish the feature files and test seams without changing
existing task behavior.

- [ ] T001 [P] Add alarm feature source and test package placeholders under `src/main/java/zg/acelera/{domain,dto,repository,service,controller,util}` and `src/test/groovy/{repository,service,controller}`
- [ ] T002 [P] Add the dedicated `alarms.csv` path configuration seam without changing existing task, user, or category file paths in `src/main/java/zg/acelera/repository`
- [ ] T003 [P] Record the alarm-focused Gradle test selectors and build commands in `docs/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Define shared typed contracts and failure behavior required by all
alarm stories.

**⚠️ CRITICAL**: No user story implementation should begin until this phase is
complete.

- [ ] T004 [P] Define the typed alarm persistence failure exception preserving the original `IOException` or parsing cause in `src/main/java/zg/acelera/util/exception/AlarmPersistenceException.java`
- [ ] T005 [P] Define the typed console notification contract receiving task identity, alarm identity, and trigger instant in `src/main/java/zg/acelera/util/AlarmNotifier.java`
- [ ] T006 [P] Add alarm test fixtures for valid TODO/DOING/DONE tasks, fixed `Clock` values, and temporary CSV paths in `src/test/groovy/service/AlarmFixtures.groovy`
- [ ] T007 Define the repository and service-facing alarm state types needed by the data model, including `id`, `taskId`, `advanceMinutes`, `triggerAt`, `primary`, `fired`, `lastFiredAt`, and `nextTriggerAt`, in `src/main/java/zg/acelera/domain/Alarm.java`

**Checkpoint**: Shared types, exception behavior, and deterministic test seams
are ready; user story increments can now be delivered.

---

## Phase 3: User Story 1 - Create, validate, and persist alarms (Priority: P1) 🎯 MVP

**Goal**: A user can create, edit, list, and remove up to three valid alarms
for a task, with explicit validation and durable state in a separate CSV.

**Independent Test**: With a temporary `alarms.csv` and a fixed `Clock`, create
an alarm for a future TODO task, read it back with all persisted fields intact,
reject every invalid boundary, update its future trigger, and remove it without
touching the existing task files.

### Tests for User Story 1

- [ ] T008 [P] [US1] Add DTO validation specifications for missing task identity, non-integer input, negative values, zero advance, and the minimum one-minute advance in `src/test/groovy/service/AlarmDtoSpec.groovy`
- [ ] T009 [P] [US1] Add repository CSV round-trip and schema specifications for semicolon ordering, ISO-8601 timestamps, nullable timestamps, and malformed field rejection in `src/test/groovy/repository/AlarmRepositorySpec.groovy`
- [ ] T010 [P] [US1] Add service specifications for trigger calculation, task duration validation, past-trigger rejection, three-alarm limit, and priority-independent calculation in `src/test/groovy/service/AlarmServiceSpec.groovy`
- [ ] T011 [P] [US1] Add service specifications for edit-state reset, stable alarm identity, list/remove behavior, and preservation of prior state when persistence fails in `src/test/groovy/service/AlarmServiceSpec.groovy`
- [ ] T012 [P] [US1] Add persistence failure specifications for unreadable, unwritable, and malformed `alarms.csv` records with cause preservation in `src/test/groovy/repository/AlarmRepositorySpec.groovy`

### Implementation for User Story 1

- [ ] T013 [P] [US1] Implement the typed create/update DTO with task identity, integer hours, integer minutes, and boundary validation in `src/main/java/zg/acelera/dto/AlarmDTO.java`
- [ ] T014 [US1] Define the alarm repository contract for list, save, update, delete, and delete-by-task operations in `src/main/java/zg/acelera/repository/AlarmRepository.java`
- [ ] T015 [US1] Implement Java NIO semicolon-delimited CSV read/write, exact field-count validation, ISO timestamp parsing, and explicit `AlarmPersistenceException` failures in `src/main/java/zg/acelera/repository/AlarmRepositoryImpl.java`
- [ ] T016 [US1] Implement alarm construction, task lookup, duration/trigger validation, maximum-three enforcement, primary selection by minimum `advanceMinutes`, and DTO-to-domain mapping in `src/main/java/zg/acelera/service/AlarmService.java`
- [ ] T017 [US1] Implement alarm create, edit, list, remove, and task-cascade operations with atomic state updates and no partial persistence in `src/main/java/zg/acelera/service/AlarmService.java`

**Checkpoint**: User Story 1 is independently usable through service/repository
calls and all CRUD and persistence acceptance criteria are covered.

---

## Phase 4: User Story 2 - Dispatch one-shot and repeating notifications (Priority: P2)

**Goal**: Open applications notify due TODO/DOING alarms exactly once per
persisted trigger, repeat only the primary alarm at its configured interval,
silence DONE tasks, and recover correctly after restart.

**Independent Test**: Invoke one monitor check with a fixed `Clock`, verify
one-shot and primary-repeat state transitions plus DONE silence and restart
deduplication, then verify the scheduled monitor starts, polls every minute,
stops when idle, and closes cleanly.

### Tests for User Story 2

- [ ] T018 [P] [US2] Add service specifications for due-window dispatch, one-shot deduplication, DONE silence, persisted restart state, and closed-application recovery in `src/test/groovy/service/AlarmDispatchSpec.groovy`
- [ ] T019 [P] [US2] Add service specifications for primary-only repetition, configured-advance intervals, new-trigger reactivation, and persistence of every dispatch transition in `src/test/groovy/service/AlarmDispatchSpec.groovy`
- [ ] T020 [P] [US2] Add monitor lifecycle specifications for one-minute fixed-rate scheduling, up-to-59-second lateness, no duplicate scheduler task, idle stop, and controlled shutdown in `src/test/groovy/service/AlarmMonitorSpec.groovy`

### Implementation for User Story 2

- [ ] T021 [US2] Implement due-alarm evaluation, TODO/DOING status gating, persisted trigger deduplication, notification dispatch, and primary repetition state transitions in `src/main/java/zg/acelera/service/AlarmService.java`
- [ ] T022 [US2] Implement the injected-clock, injected-scheduler monitor with `scheduleAtFixedRate`, explicit `start`/`stop`/`close`, idle shutdown, and failure isolation in `src/main/java/zg/acelera/service/AlarmMonitor.java`
- [ ] T023 [US2] Implement the default console warning notifier without moving notification or file access into the menu layer in `src/main/java/zg/acelera/util/ConsoleAlarmNotifier.java`

**Checkpoint**: User Story 2 is independently verifiable through synchronous
checks and scheduler lifecycle tests, without waiting for a real minute.

---

## Phase 5: User Story 3 - Integrate alarm operations with tasks and CLI (Priority: P3)

**Goal**: The task menu exposes alarm create/edit/list/remove operations while
preserving existing filters, priority ordering, dashboard behavior, task edits,
task deletion cascade, and controlled application shutdown.

**Independent Test**: Run the task controller/menu flow with valid and invalid
input, confirm standardized success/error messages, edit a task without losing
alarms, delete a task with its alarms, and close the application without
leaving the monitor running.

### Tests for User Story 3

- [ ] T024 [P] [US3] Add controller specifications for alarm create, edit, list, and remove success paths plus task-not-found, validation-limit, and persistence error messages in `src/test/groovy/controller/AlarmControllerSpec.groovy`
- [ ] T025 [P] [US3] Add task controller/service specifications proving task edits preserve alarms, changed end times recalculate and validate triggers, and task deletion cascades through `deleteByTask` in `src/test/groovy/controller/TaskControllerSpec.groovy` and `src/test/groovy/service/TaskServiceSpec.groovy`
- [ ] T026 [P] [US3] Add menu/integration specifications proving existing filters, priority ordering, and dashboard output remain unchanged while alarm operations are available in `src/test/groovy/controller/TaskMenuSpec.groovy`

### Implementation for User Story 3

- [ ] T027 [US3] Implement the typed alarm controller boundary and standardized user-facing error flow for all alarm operations in `src/main/java/zg/acelera/controller/AlarmController.java`
- [ ] T028 [US3] Wire task edit and delete operations to recalculate/preserve alarms or cascade-delete them without changing existing task persistence semantics in `src/main/java/zg/acelera/service/TaskService.java` and `src/main/java/zg/acelera/controller/TaskController.java`
- [ ] T029 [US3] Add create/edit/list/remove alarm menu actions and input parsing while preserving existing task filters, ordering, dashboard, and error messages in `src/main/java/zg/acelera/app/TaskMenu.java`
- [ ] T030 [US3] Construct and inject alarm repository, service, notifier, monitor, and controller collaborators and close the monitor during controlled application shutdown in `src/main/java/zg/acelera/Main.java`

**Checkpoint**: All user-facing alarm operations work through the existing CLI
and existing task behavior remains regression-safe.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validate the complete increment, documentation, and release
quality without broadening feature scope.

- [ ] T031 [P] Run the focused alarm specifications with `./gradlew test --tests '*Alarm*'` and resolve failures in the directly affected `src/test/groovy` or `src/main/java` files
- [ ] T032 [P] Run the full regression suite with `./gradlew test` and preserve existing behavior across `src/test/groovy/controller`, `src/test/groovy/service`, and `src/test/groovy/repository`
- [ ] T033 Run release validation with `./gradlew build` and verify the manual CLI scenario from `docs/quickstart.md`
- [ ] T034 Update feature documentation and acceptance traceability in `docs/quickstart.md` and `docs/contracts/alarm-cli.md` without changing unrelated CLI semantics

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies; T001-T003 can run in parallel.
- **Foundational (Phase 2)**: Depends on Phase 1; T004-T006 can run in parallel,
  and T007 depends on the shared type decisions.
- **User Story 1 (Phase 3)**: Depends on Phase 2; tests T008-T012 can run in
  parallel, then DTO/repository work precedes service work.
- **User Story 2 (Phase 4)**: Depends on the alarm domain and repository/service
  contracts from US1; tests T018-T020 can run in parallel, then dispatch precedes
  monitor wiring.
- **User Story 3 (Phase 5)**: Depends on the stable service and monitor APIs
  from US1 and US2; tests T024-T026 can run in parallel before implementation.
- **Polish (Phase 6)**: Depends on all desired stories being complete.

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational; this is the MVP increment.
- **US2 (P2)**: Depends on US1 persistence and service contracts, but remains
  independently testable through fixed-clock service checks.
- **US3 (P3)**: Depends on US1 CRUD APIs and US2 monitor lifecycle contracts for
  application wiring; it preserves and integrates the prior increments.

### Parallel Opportunities

- Setup tasks T001-T003 and foundational contracts/fixtures T004-T006.
- US1 tests T008-T012; T013 can proceed independently of repository test work,
  while T014-T015 are separate contract/implementation files.
- US2 tests T018-T020; notifier T023 can proceed in parallel with monitor T022
  after the service dispatch contract is fixed.
- US3 tests T024-T026 and controller/menu work can proceed in parallel when
  shared constructor contracts are agreed.
- Different user story test preparation can be parallelized after the required
  preceding contracts are stable.

## Parallel Example: User Story 1

```text
Task T008: DTO validation specifications in src/test/groovy/service/AlarmDtoSpec.groovy
Task T009: CSV repository specifications in src/test/groovy/repository/AlarmRepositorySpec.groovy
Task T010: Service validation specifications in src/test/groovy/service/AlarmServiceSpec.groovy
Task T013: AlarmDTO in src/main/java/zg/acelera/dto/AlarmDTO.java
Task T014: AlarmRepository contract in src/main/java/zg/acelera/repository/AlarmRepository.java
```

## Parallel Example: User Story 2

```text
Task T018: Dispatch specifications in src/test/groovy/service/AlarmDispatchSpec.groovy
Task T019: Repetition specifications in src/test/groovy/service/AlarmDispatchSpec.groovy
Task T020: Monitor lifecycle specifications in src/test/groovy/service/AlarmMonitorSpec.groovy
Task T023: Console notifier in src/main/java/zg/acelera/util/ConsoleAlarmNotifier.java
```

## Implementation Strategy

### MVP First

1. Complete Setup and Foundational phases.
2. Complete US1, including failing-then-passing DTO, repository, and service
   specifications.
3. Stop and validate alarm CRUD, validation, and CSV persistence independently.

### Incremental Delivery

1. Add US2 to enable deterministic dispatch, repetition, restart recovery, and
   monitor lifecycle.
2. Add US3 to expose the feature through the existing CLI and task lifecycle.
3. Run focused tests, the full suite, and `./gradlew build` after integration.

## Independent Test Criteria

- **US1**: A valid future alarm round-trips through `alarms.csv`; invalid
  advances, temporal inconsistencies, fourth alarms, malformed records, and
  persistence failures are rejected with preserved causes.
- **US2**: Fixed-clock checks emit each due trigger once, repeat only the
  primary alarm, silence DONE tasks, recover after restart, and monitor on a
  one-minute cadence with controlled idle/shutdown behavior.
- **US3**: CLI CRUD operations show standardized outcomes; task edits preserve
  or recalculate alarm state, task deletion cascades, and existing filters,
  ordering, dashboard, and task behavior remain unchanged.
