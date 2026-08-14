package zg.acelera.domain;

import lombok.*;
import zg.acelera.domain.enums.Status;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Task {
    @EqualsAndHashCode.Include
    private String name;
    @EqualsAndHashCode.Include
    private String description;
    private Integer priority;
    private Status status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Category category;
}
