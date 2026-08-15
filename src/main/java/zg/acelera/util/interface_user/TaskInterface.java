package zg.acelera.util.interface_user;

import zg.acelera.dto.TaskDTO;

public interface TaskInterface {
    void showMessage(String message);
    String readText();
    Integer readInteger();
    TaskDTO readTaskDTO();
}
