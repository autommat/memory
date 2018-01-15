package project.client;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class QuestionBox {
    private String prompt;
    private String answer;
    private String question;
    private Constraint constraint = null;

    private TextField userInput;
    private Stage window;
    private Label questionLabel;

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public String getAnswer() {
        return answer;
    }

    QuestionBox(String question, String prompt) {
        this.question = question;
        this.prompt = prompt;
    }

    public void display() {
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);

        questionLabel = new Label();
        if (constraint == null) {
            questionLabel.setText(question);
        } else {
            questionLabel.setText(question + "\n" + constraint.getDescription());
        }
        questionLabel.setTextAlignment(TextAlignment.CENTER);

        userInput = new TextField();
        userInput.setPromptText(prompt == null ? "" : prompt);
        userInput.setAlignment(Pos.CENTER);

        Button okButton = new Button("OK");
        okButton.setAlignment(Pos.CENTER);
        okButton.setOnAction(e -> closeWindow());

        VBox layout = new VBox();
        layout.getChildren().addAll(questionLabel, userInput, okButton);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.setOnCloseRequest(e -> {
            e.consume();
            closeWindow();
        });
        window.showAndWait();
    }

    private void closeWindow() {
        if (constraint == null) {
            answer = userInput.getText();
            window.close();
        } else if (constraint.isOk(userInput.getText())) {
            answer = userInput.getText();
            window.close();
        } else {
            questionLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public interface Constraint {
        boolean isOk(String in);

        String getDescription();
    }
}
