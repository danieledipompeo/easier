package it.univaq.disim.sealab.metaheuristic.utils;

public class LQNException extends Exception {

     public LQNException(String message) {
        super(message);
    }

    public LQNException(String message, Throwable cause) {
        super(message, cause);
    }

    public LQNException(Throwable cause) {
        super(cause);
    }

    protected LQNException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
