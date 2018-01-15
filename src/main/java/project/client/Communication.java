package project.client;

import project.client.exceptions.NonFatalServerException;
import project.client.exceptions.WrongResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import static project.Options.*;

public class Communication {
    private final String HOST_NAME = "127.0.0.1";
    private final int TIME_BANK_PORT_NUMBER = 8632;
    private PrintWriter out;
    private BufferedReader in;


    public Communication() {
        try {
            Socket echoSocket = new Socket(HOST_NAME, TIME_BANK_PORT_NUMBER);
            out = new PrintWriter(echoSocket.getOutputStream(), true);
            in = new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + HOST_NAME);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + HOST_NAME);
            System.exit(1);
        }
    }

    public String sendMessage(String message) throws WrongResponseException {
        out.println(message);
        try {
            String response = in.readLine();
            System.out.println("server response: " + response);
            if (response == null) {
                throw new WrongResponseException(WrongResponseException.getNullResponseMessage());
            } else if (getOption(response).equals(ERROR)) {
                NonFatalServerException nfse = new NonFatalServerException(WrongResponseException.getErrorMessage(getFirstArg(response)));
                nfse.setNewAppState(IDLE);
                throw nfse;
            }
            return response;
        } catch (IOException e) {
            throw new WrongResponseException(WrongResponseException.getDefaultMessage(), e);
        }
    }

}
