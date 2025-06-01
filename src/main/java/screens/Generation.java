/**
 * Copyright (C) University of Southampton - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holders.
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holders. If you encounter this file and do not have
 * permission, please contact the copyright holders and delete this file.
 */
package screens;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.Main;

import org.fxmisc.richtext.CodeArea;

public class Generation extends VBox {
    static String type = "generation";
    String text = "No text provided";
    String partial_code = "No code provided";
    String hint = "No hint provided";
    String topic = "No topic provided";

    CodeArea codeArea;
    HBox buttonBox = new HBox();
    Button okButton;
    Button continueButton;
    Button hintButton;
    Label hintBox;
    Label scoreBox;

    int mark;
    String reasoning;
    Label reasonBox;

    public Generation(String text, String partial_code, String hint, String topic) {
        this.text = text;
        this.partial_code = partial_code;
        this.hint = hint;
        this.topic = topic;

        Label textBox = new Label(text);
        textBox.setWrapText(true);
        textBox.setPadding(new Insets(10, 10, 10, 10));
        textBox.setAlignment(Pos.CENTER);
        textBox.setStyle("-fx-font-size:20px;");
        hintBox = new Label("Hint: " + hint);
        hintBox.setStyle("-fx-font-size:18px");
        hintBox.setPadding(new Insets(10, 10, 10, 10));
        codeArea = new CodeArea(partial_code);
        codeArea.setPrefSize(800, 400);
        codeArea.setStyle("-fx-font-size:20px");
        codeArea.setPadding(new Insets(10, 10, 10, 10));
        okButton = new Button("Submit Answer");
        okButton.setStyle("-fx-font-size:28px");
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //Handle scoring
                mark();
                int score = mark;
                Main.currentScore += score;
                Main.logger.error("Score test: " + Main.currentScore);
                Main.scoresInfo.get(Main.currentScreenIndex - 1).setScore(score);
                Main.maxScore += 3;
                scoreBox = new Label("Score: " + score + "/3");
                scoreBox.setStyle("-fx-font-size:28px");
                reasonBox = new Label("Reason: " + reasoning);
                reasonBox.setStyle("-fx-font-size:20px");
                reasonBox.setWrapText(true);
                addContinueButton();
                removeAnswerBox();
            }
        });
        continueButton = new Button("Continue");
        continueButton.setStyle("-fx-font-size:28px");
        continueButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Main.highestScreenPassed++;
                Main.currentScreenIndex++;
                Main.logger.error("Scores test: " + Main.currentScore + "/" + Main.maxScore + "; " + Main.currentScreenIndex + "/" + Main.totalScreenCount + " b" + Main.bonusScreenCount);
                if (Main.currentScreenIndex >= Main.totalScreenCount) {
                    if (Main.currentScore < Main.maxScore && Main.bonusScreenCount == 0) {
                        Main.startBonusScreens();
                        Main.switchScreen();
                    } else {
                        Main.displayFinalScores();
                    }
                } else {
                    Main.switchScreen();
                }
                removeContinueButton();
                Main.report_visible = false;
                Main.outerPane.setTop(null);
            }
        });
        hintButton = new Button("Hint");
        hintButton.setStyle("-fx-font-size:28px");
        hintButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                addHint();
            }
        });
        buttonBox.getChildren().addAll(okButton, hintButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(textBox, codeArea, buttonBox);
    }

    private void removeAnswerBox() {
        this.getChildren().remove(buttonBox);
        codeArea.setEditable(false);
    }

    private void addHint() {
        this.getChildren().add(3, hintBox);
        this.buttonBox.getChildren().remove(hintButton);
    }

    private void addContinueButton() {
        this.getChildren().addAll(continueButton, scoreBox);
        if (mark < 3) {
            this.getChildren().add(reasonBox);
        }
    }

    private void removeContinueButton() {
        this.getChildren().remove(continueButton);
    }

    private void mark() {
        try {
            String sanitisedAnswer = sanitise(codeArea.getText());
            String prompt = "A student has been given a piece of " + Main.programmingLanguage + " code and been told to complete it with the following instruction enclosed in triple quotes: \"\"\"" + text + "\"\"\". Below is the uncompleted code box that the student was provided with, enclosed in triple backticks:\n```\n" + partial_code + "\n```\nThis next piece of code is the student's attempt at completing the code, again in triple backticks:\n```\n" + sanitisedAnswer + "\n```\nExplain in detail whether you think their completed code has the desired output.  Then, output their mark and a reason why they were given that mark, making sure to cover what the correct answer was (but not including multi-line code), in second person, in a very simple JSON object, such as the following:\n```\n{\n\t\"mark\": N,\n\t\"reason\": \"Not enough detail\"\n}\n```\nWhere N is the number of marks the student is awarded. The user will be awarded 0 marks if their answer is not code, completely incorrect and/or completely irrelevant to the question. The user will be awarded 1 mark if their answer is somewhat correct. The user will be awarded two marks if their code is almost correct but with a minor syntax or logical error. The user will be awarded 3 marks if their answer is completely correct.";
            String result = Main.llm.chat(prompt);
            Main.logger.error(result);
            int markIndex = result.indexOf("\"mark\": ");
            String resultEnd = result.substring(markIndex);
            int mark = 0;
            for (int i = 0; i < resultEnd.length(); i++) {
                if (resultEnd.charAt(i) == '0') {
                    break;
                } else if (resultEnd.charAt(i) == '1') {
                    mark = 1;
                    break;
                } else if (resultEnd.charAt(i) == '2') {
                    mark = 2;
                    break;
                } else if (resultEnd.charAt(i) == '3') {
                    mark = 3;
                }
            }
            this.mark = mark;
            int reasonColonIndex = result.indexOf("\"reason\": ") + 8;
            String reasonEnd = result.substring(reasonColonIndex);
            int startQuote = result.indexOf("\"", reasonColonIndex);
            int endQuote = result.indexOf("\"", startQuote + 1);
            this.reasoning = result.substring(startQuote + 1, endQuote);

        } catch (Exception e) {
            Main.logger.error("Error while marking: " + e.getMessage());
            this.mark = 0;
            this.reasoning = "an error occurred";
        }
    }

    private String sanitise(String text) {
        return text.replaceAll("`", "").replaceAll("(?i)\"mark\": ?", "");
    }
}