package project.server;

import project.Options;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import static project.Options.*;

public class PlayerHandlerTask implements Runnable {

    private Socket socket;
    private Set<String> userNames;
    private Room room;
    private int idle = 0;

    PlayerHandlerTask(Socket socket, Room room, Set<String> userNames) {
        this.socket = socket;
        this.room = room;
        room.addPlayer(socket.getPort());
        this.userNames = userNames;
    }

    @Override
    public void run() {
        try (
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
        ) {
            Timer timer = new Timer();
            TimerTask timeoutTask = new TimerTask() {
                @Override
                public void run() {
                    idle++;
                    if (idle > 1) {
                        room.setScore(socket.getPort(), 0);
                        room.fail(socket.getPort());
                        room.setReady(socket.getPort(), true);
                        room.addName(socket.getPort(), "IDLEPLAYER");
                    }
                }
            };

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                boolean informAboutIdle = idle > 1;
                idle = 0;
                //DEBUGGING
                System.out.println("port:" + socket.getPort());
                System.out.println("message:" + inputLine);

                String[] inputArgs = inputLine.split(";");

                String response;
                switch (inputArgs[0]) {
                    case Options.INIT:
                        timer.schedule(timeoutTask, 0, room.getTimeoutSeconds() * 1000);
                        response = Options.INIT;
                        break;
                    case Options.JOIN:
                        response = handleJoin(inputArgs);
                        break;
                    case Options.HOWMANY:
                        response = handleHowMany();
                        break;
                    case Options.ASKSEQ:
                        response = handleAskSeq(inputArgs);
                        break;
                    case Options.SENDSEQ:
                        response = handleSendSeq(inputArgs);
                        break;
                    case Options.ASKSEQSTATE:
                        response = handleAskSeqState();
                        break;
                    case RESULTS:
                        response = handleResults();
                        break;
                    default:
                        response = makeMessage(Options.ERROR);
                        break;
                }
                if (informAboutIdle) response = makeMessage(ERROR, IDLE);
                System.out.println("response:" + response);
                out.println(response);
            }
        } catch (IOException e) {
            return;
        }
    }

    private String handleResults() {
        StringBuilder sb = new StringBuilder(Options.RESULTS);
        if (room.isFinished()) {
            Map<String, Integer> scores = room.getScores();
            for (String n : scores.keySet()) {
                sb.append(";").append(n).append(";").append(scores.get(n));
            }
            return sb.toString();
        } else {
            return sb.append(";").append(NO).toString();
        }
    }

    private String handleAskSeqState() {
        String decis = room.isEveryoneReady() ? YES : NO;
        return makeMessage(Options.ASKSEQSTATE, decis);
    }

    private String handleSendSeq(String[] inputArgs) {
        try {
            int round = Integer.parseInt(inputArgs[1]);

            StringBuilder sb = new StringBuilder(inputArgs[2]);
            for (int i = 3; i < inputArgs.length; i++) { //TODO: correct
                sb.append(";").append(inputArgs[i]);
            }
            boolean isCorrectSeq = room.verifySeq(round, sb.toString());
            String message;
            if (isCorrectSeq) {
                room.setScore(socket.getPort(), Integer.parseInt(inputArgs[1]));
                message = makeMessage(SENDSEQ, String.valueOf(round), YES);
            } else {
                room.fail(socket.getPort());
                message = makeMessage(SENDSEQ, String.valueOf(round), NO);
            }
            room.setReady(socket.getPort(), true);
            return message;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return ERROR;
        }
    }

    private String handleAskSeq(String[] inputArgs) {
        room.setReady(socket.getPort(), false);
        try {
            int round = Integer.parseInt(inputArgs[1]);
            return room.getSeq(round);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return ERROR;
        }
    }

    private String handleHowMany() {
        if (room.isFull() && room.areNamesChosen()) {
            return makeMessage(Options.HOWMANY, Options.START);
        }
        return makeMessage(Options.HOWMANY, String.valueOf(room.howManyPlayersNow()), String.valueOf(room.getMaxPlayersInRoom()));
    }

    private String handleJoin(String[] inputArgs) {
        String userName;
        try {
            userName = inputArgs[1];
        } catch (IndexOutOfBoundsException iobe) {
            userName = "user" + new Random().nextInt(100);
        }
        if (userNames.add(userName)) {
            room.addName(socket.getPort(), userName);
            return makeMessage(Options.JOIN, userName);
        } else {
            int howManyTaken = 0;
            userName += howManyTaken;
            while (!userNames.add(userName)) {
                userName = userName.substring(0, userName.length() - 1).concat(String.valueOf(++howManyTaken));
            }
            room.addName(socket.getPort(), userName);
            return makeMessage(Options.JOIN, userName);
        }
    }
}
