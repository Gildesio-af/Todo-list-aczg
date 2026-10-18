package zg.acelera.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
public class Alarm {
    private String id;
    private String taskId;
    private int advanceMinutes;
    private LocalDateTime triggerAt;
    private boolean primary;
    private boolean fired;
    private LocalDateTime lastFiredAt;
    private LocalDateTime nextTriggerAt;
}
