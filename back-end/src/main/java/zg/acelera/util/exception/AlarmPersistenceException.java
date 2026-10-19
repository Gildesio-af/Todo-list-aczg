package zg.acelera.util.exception;

public class AlarmPersistenceException extends RuntimeException {
    public AlarmPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlarmPersistenceException(Throwable cause) {
        super("Unable to persist alarms", cause);
    }
}
