package project.client.exceptions;

import project.Options;

public class WrongResponseException extends Exception {
    private static final String IDLE_MESSAGE = "Client was removed from game for idleness";

    private static final String NULL_RESPONSE_MESSAGE = "Response was null";

    private static final String UNKNOWN_MESSAGE = "Unknown error occured";
    WrongResponseException() {
        super();
    }

    public WrongResponseException(String message) {
        super(message);
    }

    public WrongResponseException(Throwable cause) {
        super(cause);
    }

    public WrongResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String getErrorMessage(String errorArg) {
        switch (errorArg) {
            case Options.IDLE:
                return IDLE_MESSAGE;
            default:
                return UNKNOWN_MESSAGE;
        }
    }

    public static String getDefaultMessage(){
        return UNKNOWN_MESSAGE;
    }

    public static String getNullResponseMessage() {
        return NULL_RESPONSE_MESSAGE;
    }
}
