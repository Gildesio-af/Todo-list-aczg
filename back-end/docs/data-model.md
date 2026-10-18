# Data model: Task alarms

## Alarm

The persisted `Alarm` entity represents both configuration and dispatch state.

| Field | Type | Rules |
|---|---|---|
| `id` | `String` | Required, unique per alarm, stable across updates |
| `taskId` | `String` | Required; identifies exactly one task using the existing task identity convention |
| `advanceMinutes` | `int` | Positive; derived from integer hours/minutes and at least 1 |
| `triggerAt` | `LocalDateTime` | Task end minus advance; must be in the future at create/update time |
| `primary` | `boolean` | Exactly the alarm with the smallest advance for its task is primary |
| `fired` | `boolean` | Records whether the current trigger instant has emitted |
| `lastFiredAt` | `LocalDateTime?` | The instant represented by the latest emitted event |
| `nextTriggerAt` | `LocalDateTime?` | Next repeated event for the primary alarm; null for a pending one-shot/non-primary alarm |

The CSV record is ordered as:
`id;taskId;advanceMinutes;triggerAt;primary;fired;lastFiredAt;nextTriggerAt`.
Nullable timestamps are empty fields. Values are ISO-8601 `LocalDateTime` and
must be rejected if malformed.

## Alarm input DTO

The create/update DTO keeps CLI input separate from the domain entity:

- task identifier/name
- integer hours
- integer minutes

The DTO rejects missing task identity, negative values, non-integer values, and
an advance below one minute. The service revalidates public calls and applies
task-dependent rules that require the current task.

## Relationships

- One `Task` has zero to three `Alarm` records.
- One `Alarm` belongs to exactly one `Task`.
- Deleting a task cascades to `deleteByTask` in the alarm repository.
- Editing a task preserves alarm records. If the edit changes the task end-time,
  each resulting trigger is recalculated and validated; a future trigger resets
  `fired`, `lastFiredAt`, and `nextTriggerAt`.

## State transitions

```text
PENDING --(now >= triggerAt, task TODO/DOING)--> FIRED
FIRED --(primary and task TODO/DOING and next interval reached)--> FIRED
PENDING/FIRED --(task DONE)--> SILENCED
FIRED --(valid future trigger after task edit)--> PENDING
```

Only the primary alarm may transition repeatedly. A due alarm is emitted once
per persisted trigger instant; the service updates CSV state in the same
operation. A `DONE` task never emits and never schedules a repeat.

## Validation invariants

1. The task has a valid start and end, with start before end.
2. `advanceMinutes < Duration.between(start, end).toMinutes()`.
3. `triggerAt = task.endDate - advanceMinutes`.
4. `triggerAt` is not before the creation/update `Clock` instant.
5. The task has fewer than three alarms before creating a new one.
6. Primary selection is recalculated by minimum `advanceMinutes`, independent
   of task priority.
