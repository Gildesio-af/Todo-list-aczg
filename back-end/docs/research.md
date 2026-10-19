# Research: Task alarms

## Decision: Preserve the existing layered architecture

**Rationale:** The current flow is `TaskMenu -> TaskController -> TaskService
-> TaskRepository -> TaskRepositoryImpl`, and the constitution requires the
existing package boundaries. Alarm operations will use an `AlarmController`
(or the task controller's alarm methods), `AlarmService`, `AlarmRepository`,
and `AlarmRepositoryImpl`. `Main` will construct and inject these collaborators.

**Alternatives considered:** Direct file access from `TaskMenu` was rejected
because it violates the repository boundary and makes error handling and tests
inconsistent. A new top-level notification package was rejected because it
would require an architectural amendment.

## Decision: Use a single-thread fixed-rate monitor with injected time

**Rationale:** `ScheduledExecutorService.scheduleAtFixedRate` directly models
the required one-minute polling and avoids drift from `scheduleWithFixedDelay`.
An injected `Clock` makes due-window and restart tests deterministic. The
monitor will expose an explicit `close`/`stop` lifecycle, avoid duplicate
scheduler tasks, stop when no monitorable alarms remain, and keep polling
failures from cancelling future checks.

**Alternatives considered:** Real-time sleeps were rejected because they make
tests slow and flaky. An operating-system scheduler is out of scope.
`scheduleWithFixedDelay` was rejected because long checks could drift the
one-minute cadence.

## Decision: Persist alarm state in a dedicated validated CSV

**Rationale:** Java NIO and the existing semicolon format meet the storage
constraint without adding dependencies. The repository will validate field
count and every typed value, use try-with-resources or `readAllLines`, and
raise a persistence exception that preserves the original I/O or parse cause.
Updates can rewrite the small file after validating all records.

**Alternatives considered:** A CSV library would improve escaping but add an
unnecessary dependency. Silently skipping malformed rows or recreating a
missing file was rejected by the specification.

## Decision: Store a stable alarm identity and task association explicitly

**Rationale:** Each alarm needs independent fired/repetition state, while the
existing task model identifies tasks by mutable name. The design retains the
current task-name lookup convention for compatibility and includes an alarm
identifier plus task identifier in each record. Task edits must update the
association when the task identifier changes; task deletion removes all
associated alarm records.

**Alternatives considered:** Using only the calculated instant cannot
distinguish alarms with equal advances. Using only list position cannot
survive edits or restarts.

## Decision: Make notification and monitor checks synchronously testable

**Rationale:** The service exposes a single-check operation to the monitor,
and notification is an injected console-facing contract. Tests can invoke one
check with a fixed clock without waiting a minute, while a lifecycle test
verifies scheduling and shutdown.

**Alternatives considered:** Hard-coding `System.out` and
`LocalDateTime.now()` was rejected because it prevents reliable assertions and
couples business logic to the CLI.
