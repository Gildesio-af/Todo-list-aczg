# Alarm feature validation guide

## Prerequisites

- Java 17 or newer
- Repository root at `back-end`
- Gradle Wrapper available as `./gradlew`

## Phase 1 setup

The alarm feature uses the existing `zg.acelera` source and test package
structure. Its persistence path is isolated in `AlarmStoragePath` and defaults
to `alarms.csv`; existing task, user, and category file paths are unchanged.

To compile the current setup and confirm the Gradle test task is available:

```text
./gradlew classes testClasses
```

## Automated validation

Run the focused feature tests after implementing the plan:

```text
./gradlew test --tests '*Alarm*'
```

The tests should cover:

1. Positive one-minute advance and calculated end-minus-advance instant.
2. Zero/negative advance, advance equal to duration, past trigger, and
   fourth-alarm rejection.
3. Priority-independent primary selection and preservation through task edits.
4. One-shot dispatch, primary repetition, `DONE` silence, restart state, and
   no duplicate firing for the same instant.
5. CSV round-trip, malformed-row rejection, missing/unreadable file failures,
   and cause preservation.
6. One-minute scheduling, idle stop, and controlled application shutdown.
7. Controller/menu success and standard error messages while existing filters,
   ordering, and dashboard behavior remain unchanged.

Then run the release-quality checks:

```text
./gradlew test
./gradlew build
```

## Manual end-to-end scenario

1. Start the CLI with `./gradlew run --console=plain -q`.
2. Create a task whose start/end span several hours and whose status is
   `TODO` or `DOING`.
3. Open task alarm management and create one valid alarm with at least one
   minute of advance.
4. Confirm the alarm is written to `alarms.csv` and appears in the list.
5. Wait for the monitor's due check and confirm one console warning.
6. Leave the task unfinished and confirm only the primary alarm repeats at its
   configured interval.
7. Mark the task `DONE`; confirm no further warnings are emitted.
8. Remove the final monitorable alarm and confirm the monitor shuts down
   without changing existing task files.

The record schema and state semantics are defined in
[data-model.md](./data-model.md), and the user-facing operation contract is
defined in [contracts/alarm-cli.md](./contracts/alarm-cli.md).
