# Alarm CLI contract

The task menu exposes alarm operations without changing existing task filters,
priority ordering, or dashboard semantics.

## Operations

| Operation | Input | Success behavior | Failure behavior |
|---|---|---|---|
| Create alarm | Task identity, integer hours, integer minutes | Validate, calculate `triggerAt`, persist, and start monitoring if needed | Standard invalid-input, task-not-found, limit, temporal, or persistence message; no partial record |
| Edit alarm | Alarm identity plus new advance | Recalculate against current task, reset dispatch state for a new future instant, persist | Standard validation/persistence message; previous valid state remains if update fails |
| List alarms | Task identity | Display persisted alarms and dispatch state | Standard task-not-found or persistence message |
| Remove alarm | Alarm identity | Remove record and stop monitor when no monitorable alarms remain | Standard not-found or persistence message |

## Notification contract

The console notifier receives a task and alarm identity plus the trigger
instant. It emits one human-readable warning per due persisted trigger. The
service, not the menu, decides whether a `DONE` task is silent and whether an
alarm is eligible for repetition.

## Error contract

Validation errors use the project's established user-facing error flow.
Persistence errors use a dedicated runtime exception whose cause is the
original `IOException` or parsing exception. Controllers display the
standardized persistence prefix and do not expose stack traces or silently
recover.
