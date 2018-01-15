package project;

import project.client.exceptions.WrongResponseException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Options {
    public static final String INIT = "INIT";
    public static final String JOIN = "JOIN";
    public static final String HOWMANY = "HOWMANY";
    public static final String ASKSEQ = "ASKSEQ";
    public static final String SENDSEQ = "SENDSEQ";
    public static final String ASKSEQSTATE = "ASKSEQSTATE";
    public static final String RESULTS = "RESULTS";
    public static final String ERROR = "ERROR";

    public static final String START = "START";
    public static final String IDLE = "IDLE";
    public static final String YES = "YES";
    public static final String NO = "NO";

    public static final String PREPARATION = "PREPARATION";
    public static final String ROUND = "ROUND";
    public static final String ROUNDPLAY = "ROUNDPLAY";
    public static final String ROUNDWAIT = "ROUNDWAIT";
    public static final String GAMEOVER = "GAMEOVER";
    public static final String ERRORSTATE = "ERRORSTATE";

    private static Set<String> validOptions = new HashSet<>();

    static {
        validOptions.addAll(Stream.of(INIT, JOIN, HOWMANY, ASKSEQ, SENDSEQ, ASKSEQSTATE, RESULTS, ERROR)
                .collect(Collectors.toSet()));
    }

    public static String makeMessage(String firstArg, String... otherArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstArg);
        for (String arg : otherArgs) {
            sb.append(";").append(arg); //TODO:validation
        }
        return sb.toString();
    }

    public static String makeMessage(String firstArg, Stream<String> otherArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstArg);
        otherArgs.forEach(arg -> sb.append(";").append(arg));
        return sb.toString();
    }

    public static String makeMessage(String firstArg, List<String> otherArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstArg);
        for (String arg : otherArgs) {
            sb.append(";").append(arg);
        }
        return sb.toString();
    }

    public static String getOption(String message) {
        int semicolonIndex = message.indexOf(";");
        if (semicolonIndex != -1) {
            return message.substring(0, message.indexOf(";"));
        } else {
            return message;
        }
    }

    public static String getFirstArg(String message) throws WrongResponseException {
        try {
            return message.split(";")[1];
        } catch (IndexOutOfBoundsException iobe) {
            throw new WrongResponseException(iobe);
        }
    }

    public static String getSecondArg(String message) throws WrongResponseException { //TODO: add exception
        try {
            return message.split(";")[2];
        } catch (IndexOutOfBoundsException iobe) {
            throw new WrongResponseException(iobe);
        }
    }

    public static String getResultsAnnouncement(String[] message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < message.length; i += 2) {
            sb.append(message[i]).append(": ").append(message[i + 1]).append("pts, ");
        }
        return sb.toString();
    }

    public static boolean isOptionCorrect(String option) {
        return validOptions.contains(option);
    }

    public static boolean isArgCorrect(String arg) {
        if (arg.contains(";")) {
            return false;
        }
        if (arg.contains("\n")) {
            return false;
        }
        return true;
    }

    public static String correctArg(String arg) {
        return arg.replaceAll(";", "").replaceAll("\n", "");
    }


}