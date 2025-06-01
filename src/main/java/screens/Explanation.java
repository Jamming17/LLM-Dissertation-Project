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

import java.util.ArrayList;

public class Explanation extends VBox {
    static String type = "explanation";
    String text = "No text provided";
    String code_example = "No code provided";
    int codeExampleIndex = 0;
    int totalNumberOfCodeExamples = 1;
    HBox codeExampleHBox = new HBox();
    ArrayList<CodeArea> codeExamples = new ArrayList<>();

    Button okButton;

    public Explanation(String text, String code_example) {
        this.text = text;
        this.code_example = code_example;

        Label textBox = new Label(this.text);
        textBox.setWrapText(true);
        textBox.setAlignment(Pos.CENTER);
        textBox.setStyle("-fx-font-size:20px;");
        okButton = new Button("I understand");
        okButton.setStyle("-fx-font-size:28px");
        okButton.setPadding(new Insets(10, 10, 10, 10));
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.highestScreenPassed++;
                Main.currentScreenIndex++;
                if (Main.currentScreenIndex > Main.totalScreenCount) {
                    Main.displayScores();
                } else {
                    Main.switchScreen();
                }
                remove();
                Main.report_visible = false;
                Main.outerPane.setTop(null);
            }
        });

        CodeArea codeArea = new CodeArea(code_example);
        codeArea.setPrefSize(800, 500);
        codeArea.setEditable(false);
        codeArea.setStyle("-fx-font-size:20px");
        codeArea.setPadding(new Insets(10, 10, 10, 10));
        codeExamples.add(codeArea);
        Button leftArrowButton = new Button("<-");
        leftArrowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (codeExampleIndex > 0) {
                    loadNewCodeExample(false);
                }
            }
        });
        Button rightArrowButton = new Button("->");
        rightArrowButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (codeExampleIndex < 2) {
                    CodeArea area = new CodeArea();
                    area.setPrefSize(800, 500);
                    area.setEditable(false);
                    area.setStyle("-fx-font-size:20px");
                    area.setPadding(new Insets(10, 10, 10, 10));
                    codeExamples.add(area);
                    loadNewCodeExample(true);
                    if (totalNumberOfCodeExamples < 3) {
                        area.replaceText(generateNewCodeExample());
                    }
                }
            }
        });
        codeExampleHBox.setPadding(new Insets(10, 10, 10, 10));
        codeExampleHBox.setAlignment(Pos.CENTER);
        codeExampleHBox.getChildren().addAll(leftArrowButton, codeExamples.get(0), rightArrowButton);
        this.getChildren().addAll(textBox, codeExampleHBox, okButton);
    }

    private void remove() {
        this.getChildren().remove(okButton);
    }

    private void loadNewCodeExample(boolean arrow) { //false <- -> true
        codeExampleHBox.getChildren().remove(codeExamples.get(codeExampleIndex));
        if (arrow == false) {
            codeExampleIndex--;
        } else {
            codeExampleIndex++;
        }
        codeExampleHBox.getChildren().add(1, codeExamples.get(codeExampleIndex));
    }

    private String generateNewCodeExample() {
        totalNumberOfCodeExamples++;
        String prompt = "A " + Main.programmingLanguage + " programming student has been offered the following explanation on the topic of " + Main.currentModule + ": \"" + text + "\". They were given the following simple code as an example: \"" + code_example + "\".";
        String extra_code = " They are also given this code example: \"" + codeExamples.get(1).getText() + "\".";
        String ending = " Please generate a new simple piece of code to demonstrate this feature in a similar but different way to enhance the student's understanding. Very importantly, the code must be just as simple as the other(s), but perform a different task. Explain your reasoning in text and delimit the code block with triple backticks. Leave no comments in the code itself.";
        if (codeExampleIndex == 2) {
            prompt += extra_code;
        }
        prompt += ending;
        String result = Main.llm.chat(prompt);
        Main.logger.error("PROMPT: " + prompt);
        int firstIndex = result.indexOf("```");
        int firstActualIndex = result.indexOf("\n", firstIndex);
        if (firstIndex == -1) {
            return "aaaaaa";
        }
        int secondIndex = result.indexOf("```", firstIndex + 3);
        if (secondIndex == -1) {
            return "bbbbbb";
        }
        return result.substring(firstActualIndex + 1, secondIndex);
    }
}