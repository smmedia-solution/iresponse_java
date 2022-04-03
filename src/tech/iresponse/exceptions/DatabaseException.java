package tech.iresponse.exceptions;

public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable paramThrowable) {
        super(message, paramThrowable);
    }

    public DatabaseException(Throwable paramThrowable) {
        super(paramThrowable);
    }

    public DatabaseException(String message, Throwable paramThrowable, boolean paramBoolean1, boolean paramBoolean2) {
        super(message, paramThrowable, paramBoolean1, paramBoolean2);
    }
}
