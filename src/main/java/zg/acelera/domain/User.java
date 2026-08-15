package zg.acelera.domain;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class User {
    public User(String[] userInfo) {
        this.name = userInfo[0];
        this.email = userInfo[1];
        this.age = Integer.parseInt(userInfo[2]);
    }

    private String name;
    @EqualsAndHashCode.Include
    private String email;
    private int age;

    @Builder.Default
    List<Task> tasks = new ArrayList<>();
}
