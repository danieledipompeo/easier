package it.univaq.disim.sealab.metaheuristic.utils;

public class EasierObjectiveNotFoundException extends Exception{

    public EasierObjectiveNotFoundException(String message) {
        super(message);
    }

    public EasierObjectiveNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EasierObjectiveNotFoundException(Throwable cause) {
        super(cause);
    }

    protected EasierObjectiveNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
