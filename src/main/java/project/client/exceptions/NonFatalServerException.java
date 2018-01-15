package project.client.exceptions;

import static project.Options.ERRORSTATE;

public class NonFatalServerException extends WrongResponseException {
    public String getNewAppState() {
        return newAppState==null?ERRORSTATE:newAppState;
    }

    public void setNewAppState(String newAppState) {
        this.newAppState = newAppState;
    }

    String newAppState;

    public NonFatalServerException() {
        super();
    }

    public NonFatalServerException(String message) {
        super(message);
    }

    public NonFatalServerException(Throwable cause) {
        super(cause);
    }

    public NonFatalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
