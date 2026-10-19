package zg.acelera.repository;

import zg.acelera.domain.Alarm;

import java.util.List;

public interface AlarmRepository {
    List<Alarm> findAll();
    List<Alarm> findByTask(String taskId);
    Alarm findById(String id);
    Alarm save(Alarm alarm);
    Alarm update(Alarm alarm);
    boolean delete(String id);
    int deleteByTask(String taskId);
}
