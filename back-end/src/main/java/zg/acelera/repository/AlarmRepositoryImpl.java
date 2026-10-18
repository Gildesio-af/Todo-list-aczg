package zg.acelera.repository;

import zg.acelera.domain.Alarm;
import zg.acelera.util.exception.AlarmPersistenceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AlarmRepositoryImpl implements AlarmRepository {
    private static final int FIELD_COUNT = 8;
    private final Path filePath;

    public AlarmRepositoryImpl() {
        this(new AlarmStoragePath().path());
    }

    public AlarmRepositoryImpl(Path path) {
        this.filePath = Objects.requireNonNull(path, "Alarm storage path cannot be null");
        initializeFile();
    }

    private void initializeFile() {
        try {
            if (!Files.exists(filePath)) {
                Path parent = filePath.toAbsolutePath().getParent();
                if (parent != null) Files.createDirectories(parent);
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new AlarmPersistenceException("Unable to initialize alarm storage", e);
        }
    }

    @Override
    public List<Alarm> findAll() {
        try {
            return Files.readAllLines(filePath).stream()
                    .filter(line -> !line.trim().isEmpty())
                    .map(this::parse)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException | RuntimeException e) {
            if (e instanceof AlarmPersistenceException alarmException) throw alarmException;
            throw new AlarmPersistenceException("Unable to read alarm storage", e);
        }
    }

    private Alarm parse(String line) {
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new AlarmPersistenceException("Malformed alarm record: expected "
                    + FIELD_COUNT + " fields", new IllegalArgumentException(line));
        }
        try {
            return Alarm.builder()
                    .id(required(fields[0], "id"))
                    .taskId(required(fields[1], "taskId"))
                    .advanceMinutes(Integer.parseInt(fields[2]))
                    .triggerAt(LocalDateTime.parse(fields[3]))
                    .primary(parseBoolean(fields[4], "primary"))
                    .fired(parseBoolean(fields[5], "fired"))
                    .lastFiredAt(parseNullable(fields[6]))
                    .nextTriggerAt(parseNullable(fields[7]))
                    .build();
        } catch (RuntimeException e) {
            throw new AlarmPersistenceException("Malformed alarm record", e);
        }
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing " + field);
        return value;
    }

    private LocalDateTime parseNullable(String value) {
        return value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private boolean parseBoolean(String value, String field) {
        if (!"true".equals(value) && !"false".equals(value)) {
            throw new IllegalArgumentException("Invalid " + field + " boolean: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    public List<Alarm> findByTask(String taskId) {
        return findAll().stream().filter(alarm -> alarm.getTaskId().equalsIgnoreCase(taskId)).toList();
    }

    @Override
    public Alarm findById(String id) {
        return findAll().stream().filter(alarm -> alarm.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Alarm save(Alarm alarm) {
        List<Alarm> alarms = findAll();
        alarms.add(alarm);
        saveAll(alarms);
        return alarm;
    }

    @Override
    public Alarm update(Alarm alarm) {
        List<Alarm> alarms = findAll();
        int index = -1;
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).getId().equals(alarm.getId())) {
                index = i;
                break;
            }
        }
        if (index < 0) return null;
        alarms.set(index, alarm);
        saveAll(alarms);
        return alarm;
    }

    @Override
    public boolean delete(String id) {
        List<Alarm> alarms = findAll();
        boolean removed = alarms.removeIf(alarm -> alarm.getId().equals(id));
        if (removed) saveAll(alarms);
        return removed;
    }

    @Override
    public int deleteByTask(String taskId) {
        List<Alarm> alarms = findAll();
        int originalSize = alarms.size();
        alarms.removeIf(alarm -> alarm.getTaskId().equalsIgnoreCase(taskId));
        if (alarms.size() != originalSize) saveAll(alarms);
        return originalSize - alarms.size();
    }

    private void saveAll(List<Alarm> alarms) {
        Path temporaryPath = null;
        try {
            List<String> lines = alarms.stream().map(this::serialize).toList();
            Path parent = filePath.toAbsolutePath().getParent();
            if (parent == null) {
                throw new IOException("Alarm storage has no parent directory");
            }
            temporaryPath = Files.createTempFile(parent, filePath.getFileName().toString(), ".tmp");
            Files.write(temporaryPath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporaryPath, filePath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(temporaryPath, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            deleteTemporaryFile(temporaryPath, e);
            throw new AlarmPersistenceException("Unable to write alarm storage", e);
        } catch (RuntimeException e) {
            deleteTemporaryFile(temporaryPath, e);
            if (e instanceof AlarmPersistenceException alarmException) throw alarmException;
            throw new AlarmPersistenceException("Unable to serialize alarm storage", e);
        }
    }

    private void deleteTemporaryFile(Path temporaryPath, Throwable failure) {
        if (temporaryPath == null) return;
        try {
            Files.deleteIfExists(temporaryPath);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private String serialize(Alarm alarm) {
        return String.join(";",
                alarm.getId(), alarm.getTaskId(), String.valueOf(alarm.getAdvanceMinutes()),
                alarm.getTriggerAt().toString(), String.valueOf(alarm.isPrimary()),
                String.valueOf(alarm.isFired()), format(alarm.getLastFiredAt()),
                format(alarm.getNextTriggerAt()));
    }

    private String format(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }
}
