package zg.acelera.repository;

import java.nio.file.Path;
import java.util.Objects;

public final class AlarmStoragePath {
    public static final String DEFAULT_FILE_NAME = "alarms.csv";

    private final Path path;

    public AlarmStoragePath() {
        this(Path.of(DEFAULT_FILE_NAME));
    }

    public AlarmStoragePath(Path path) {
        this.path = Objects.requireNonNull(path, "Alarm storage path cannot be null");
    }

    public Path path() {
        return path;
    }
}
