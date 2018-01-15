package project.client;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import project.Options;
import project.client.exceptions.NonFatalServerException;
import project.client.exceptions.WrongResponseException;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import static project.Options.*;

public class Client extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private Communication communication = new Communication();
    private Timer timer = new Timer();
    private Timer colorTimer = new Timer();
    private String state = PREPARATION;
    private int round = 1;
    private String userName = "";
    private int serverSeqLen = 0;
    private List<String> userSeq = new ArrayList<>();
    //GUI
    private Label up;
    private Label logLabel;
    private List<Button> buttons;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Memory");
        primaryStage.setWidth(500);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        buttons = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button nextButton = new Button("+");
                nextButton.setDisable(true);
                nextButton.setOnAction(event -> buttonPress(event));
                gridPane.setConstraints(nextButton, i, j);
                buttons.add(nextButton);
                gridPane.getChildren().add(nextButton);
            }
        }
        gridPane.setAlignment(Pos.CENTER);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 10, 10, 10));
        up = new Label(userName);
        up.setTextAlignment(TextAlignment.CENTER);
        up.setAlignment(Pos.CENTER);
        logLabel = new Label();

        vBox.getChildren().addAll(up, gridPane, logLabel);
        Scene scene = new Scene(vBox);
        scene.getStylesheets().add("styles.css");

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            timer.cancel();
            colorTimer.cancel();
        });
        primaryStage.show();

        try {
            communication.sendMessage(INIT);
        } catch (WrongResponseException se) {
            handleServerException(se);
        }
        QuestionBox userNameRequest = new QuestionBox("What is your name?", "Your name");
        userNameRequest.setConstraint(new QuestionBox.Constraint() {
            @Override
            public boolean isOk(String in) {
                return Options.isArgCorrect(in);
            }

            @Override
            public String getDescription() {
                return "name must not contain semicolon";
            }
        });
        userNameRequest.display();

        String joinMessage = makeMessage(Options.JOIN, Options.correctArg(userNameRequest.getAnswer()));
        String joinResponse;
        try {
            joinResponse = communication.sendMessage(joinMessage);
            up.setText("Player: " + getFirstArg(joinResponse));
        } catch (NonFatalServerException nfse) {
            handleNonFatalServerException(nfse);
        } catch (WrongResponseException se) {
            handleServerException(se);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                switch (state) {
                    case PREPARATION:
                        String howManyMessage = makeMessage(Options.HOWMANY);
                        String howManyResponse;
                        try {
                            howManyResponse = communication.sendMessage(howManyMessage);
                            String[] responseNums = howManyResponse.split(";");
                            if (getFirstArg(howManyResponse).equals(Options.START)) {
                                state = ROUND;
                                log(getLogTime() + "game starts");
                            } else if (responseNums[1].equals(responseNums[2])) {
                                log(getLogTime() + ":Wait for players to choose their names...");
                            } else {
                                log(getLogTime() + ":Currently " + responseNums[1] + " players in room. Must be " + responseNums[2] + ".");
                            }
                        } catch (NonFatalServerException nfse) {
                            handleNonFatalServerException(nfse);
                        } catch (WrongResponseException e) {
                            handleServerException(e);
                        }
                        break;
                    case ROUND:
                        log("round " + round);
                        playRound(round);
                        state = ROUNDPLAY;
                        break;
                    case ROUNDPLAY:
                        break;
                    case ROUNDWAIT:
                        try {
                            String roundwaitMessage = makeMessage(ASKSEQSTATE, String.valueOf(round));
                            String response = communication.sendMessage(roundwaitMessage);
                            if (getFirstArg(response).equals(YES)) {
                                round++;
                                state = ROUND;
                            } else if (getFirstArg(response).equals(GAMEOVER)) {
                                log("gameover");
                            } else {
                                log("waiting for other players");
                            }
                        } catch (NonFatalServerException nfse) {
                            handleNonFatalServerException(nfse);
                        } catch (WrongResponseException se) {
                            handleServerException(se);
                        }
                        break;
                    case GAMEOVER:
                        try {
                            String res = communication.sendMessage(makeMessage(RESULTS));
                            if (!getFirstArg(res).equals(NO)) {
                                log(getResultsAnnouncement(res.split(";")));
                                cancel();
                            }
                        } catch (NonFatalServerException nfse) {
                            handleNonFatalServerException(nfse);
                        } catch (WrongResponseException se) {
                            handleServerException(se);
                        }
                        break;
                    case IDLE:
                        log("you were removed from game for idleness");
                    case ERRORSTATE:
                        cancel();
                        break;
                }
            }
        }, 0, 4000);

    }

    private void playRound(int roundNum) {
        try {
            String seq = communication.sendMessage(makeMessage(Options.ASKSEQ, String.valueOf(roundNum)));
            showSeq(seq.split(";"));
        } catch (NonFatalServerException nfse) {
            handleNonFatalServerException(nfse);
        } catch (WrongResponseException se) {
            handleServerException(se);
        }
    }

    void showSeq(String[] seq) {
        ColorButton colorButtons = new ColorButton(buttons, seq);
        colorButtons.onFinish(() -> {
            buttons.forEach(b -> b.setDisable(false));
            serverSeqLen = seq.length;
        });
        colorTimer.schedule(colorButtons, 0, 500);
    }

    void buttonPress(ActionEvent event) {
        userSeq.add(String.valueOf(buttons.indexOf(event.getSource())));
        if (--serverSeqLen == 0) {
            buttons.forEach(b -> b.setDisable(true));
            Stream<String> args = Stream.concat(Stream.of(String.valueOf(round)), userSeq.stream());
            try {
                String resp = communication.sendMessage(makeMessage(Options.SENDSEQ, args));
                if (getSecondArg(resp).equals(Options.NO)) {

                    log("you made a mistake! wait for results...");

                    state = GAMEOVER;
                } else {
                    state = ROUNDWAIT;
                }
                userSeq.clear();
            } catch (NonFatalServerException nfse) {
                state = nfse.getNewAppState();
                log(nfse.getMessage());
            } catch (WrongResponseException se) {
                handleServerException(se);
            }
        }
    }

    public static String getLogTime() {
        LocalTime now = LocalTime.now();
        return now.getHour() + ":" + now.getMinute() + ":" + now.getSecond();
    }

    private void handleServerException(WrongResponseException se) {
        state = ERRORSTATE;
        log("PLEASE RESTART, ERROR OCCURED:" + se.getMessage());
    }

    private void handleNonFatalServerException(NonFatalServerException nfse) {
        state = nfse.getNewAppState();
        log(nfse.getMessage());
    }

    private void log(String logMessage) {
        Platform.runLater(() -> {
            if (logLabel != null)
                logLabel.setText(logMessage);
        });
    }
}
