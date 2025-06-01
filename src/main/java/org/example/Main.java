/**
 * Copyright (C) University of Southampton - All Rights Reserved
 *
 * This source code is protected under international copyright law. All rights
 * reserved and protected by the copyright holders.
 * This file is confidential and only available to authorized individuals with the
 * permission of the copyright holders. If you encounter this file and do not have
 * permission, please contact the copyright holders and delete this file.
 */

package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import extra.ScreenScore;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import screens.Comprehension;
import screens.Explanation;
import screens.Generation;

public class Main extends Application implements EventHandler<ActionEvent> {

    public static Logger logger = org.apache.logging.log4j.LogManager.getLogger(Llm.class);
    public static Llm llm = new Llm();
    static Stage stage;
    static Scene scene;

    static final int SCENE_WIDTH = 1500;
    static final int SCENE_HEIGHT = 800;
    static final boolean TEST_MODE = true;
    public static boolean report_visible = false;

    public static String programmingLanguage = "";
    public static String currentModule = "";

    public static BorderPane outerPane;
    static Button leftArrow;
    static Button rightArrow;
    public static int currentScreenIndex, totalScreenCount, bonusScreenCount, highestScreenPassed;
    static ArrayList<VBox> screens = new ArrayList<>();

    public static int currentScore = 0;
    public static int maxScore = 0;
    public static ArrayList<ScreenScore> scoresInfo = new ArrayList<>();

    static StackPane layeredPane;
    static Pane loadingOverlay;
    static boolean loadingLoaded = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception { //Stage = window; Scene = stuff in the window

        //Initialisation of key components
        this.stage = stage;
        BorderPane menuPane = new BorderPane();
        scene = new Scene(menuPane, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        //Main menu
        VBox menuContainer = new VBox();
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setStyle("-fx-padding: 20;");

        Label directlyImplementedLanguagesLabel = new Label("Directly implemented languages");
        directlyImplementedLanguagesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding-bottom: 30px;");
        directlyImplementedLanguagesLabel.setPadding(new Insets(10, 10, 10, 10));

        HBox directlyImplementedLanguagesBox = new HBox(10);
        directlyImplementedLanguagesBox.setAlignment(Pos.CENTER);
        directlyImplementedLanguagesBox.setPadding(new Insets(0, 0, 10, 0));

        ArrayList<String> directlyImplementedLanguagesList = new ArrayList<>(Arrays.asList("Java"));
        for(String s : directlyImplementedLanguagesList) {
            Button button = new Button(s);
            button.setStyle("-fx-font-size: 14px;");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    programmingLanguage = s;
                    openModuleSelectionScreen();
                }
            });
            directlyImplementedLanguagesBox.getChildren().add(button);
        }

        Label translatedLanguagesLabel = new Label("Translated languages");
        translatedLanguagesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding-top: 30px; -fx-padding-bottom: 30px;");
        translatedLanguagesLabel.setPadding(new Insets(30, 10, 10, 10));

        HBox translatedLanguagesBox = new HBox(10);
        translatedLanguagesBox.setAlignment(Pos.CENTER);

        ArrayList<String> translatedLanguagesList = new ArrayList<>(Arrays.asList("C#", "Python", "Rust"));
        for(String s : translatedLanguagesList) {
            Button button = new Button(s);
            button.setStyle("-fx-font-size: 14px;");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    programmingLanguage = s;
                    openModuleSelectionScreen();
                }
            });
            translatedLanguagesBox.getChildren().add(button);
        }
        menuContainer.getChildren().addAll(directlyImplementedLanguagesLabel, directlyImplementedLanguagesBox, translatedLanguagesLabel, translatedLanguagesBox);
        menuPane.setCenter(menuContainer);

        //Start loading overlay
        loadingOverlay = new Pane();
        loadingOverlay.setVisible(false);
        Rectangle background = new Rectangle(SCENE_WIDTH, SCENE_HEIGHT, Color.rgb(255, 255, 255, 0.5));
        Label loadingText = new Label("Loading...");
        loadingText.setStyle("-fx-font-size: 30px");
        loadingText.setLayoutX(SCENE_WIDTH / 2 - loadingText.getLayoutBounds().getWidth() / 2);
        loadingText.setLayoutY(SCENE_HEIGHT / 2 - loadingText.getLayoutBounds().getHeight() / 2);
        loadingOverlay.getChildren().addAll(background, loadingText);
    }

    public static void openModuleSelectionScreen() {
        //Module selection screen
        Label moduleSelectionText = new Label("Select a module:");
        moduleSelectionText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding-bottom: 30px;");
        moduleSelectionText.setPadding(new Insets(30, 10, 10, 10));
        moduleSelectionText.setAlignment(Pos.CENTER);
        HBox moduleSelectionBox = new HBox(10);
        ArrayList<String> modulesList = new ArrayList<>(Arrays.asList("Variables", "Functions", "File Handling"));
        for (String s : modulesList) {
            Button button = new Button(s);
            button.setStyle("-fx-font-size: 14px;");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    currentModule = s;
                    startModule();
                }
            });
            moduleSelectionBox.getChildren().add(button);
        }
        moduleSelectionBox.setAlignment(Pos.CENTER);
        BorderPane modulesPain = new BorderPane();
        VBox modulesBox = new VBox(10);
        modulesBox.setAlignment(Pos.CENTER);
        modulesBox.getChildren().addAll(moduleSelectionText, moduleSelectionBox);
        modulesPain.setCenter(modulesBox);
        scene = new Scene(modulesPain, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    private static void startModule() {
        //Module outer part
        outerPane = new BorderPane();
        layeredPane = new StackPane(outerPane, loadingOverlay);
        leftArrow = new Button("<-");
        leftArrow.setAlignment(Pos.CENTER);
        leftArrow.setPrefHeight(200);
        leftArrow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                logger.error(currentScreenIndex);
                if (currentScreenIndex > 1) {
                    currentScreenIndex--;
                    switchScreen();
                }
                report_visible = false;
                outerPane.setTop(null);
            }
        });
        rightArrow = new Button("->");
        rightArrow.setAlignment(Pos.CENTER);
        rightArrow.setPrefHeight(200);
        rightArrow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                logger.error(currentScreenIndex);

                if (currentScreenIndex <= highestScreenPassed) {
                    currentScreenIndex++;
                    switchScreen();
                }
                report_visible = false;
                outerPane.setTop(null);
            }
        });

        Button reportButton = new Button("!");
        reportButton.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(245, 66, 66);");
        reportButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (report_visible == false && TEST_MODE == true) {
                    report_visible = true;

                    TextField reportTextArea = new TextField();
                    reportTextArea.setStyle("-fx-font-size: 20px; -fx-color-label-visible: rgba(148, 0, 0);");
                    reportTextArea.setPadding(new Insets(10, 10, 10, 10));
                    reportTextArea.setAlignment(Pos.CENTER);

                    Button reportSubmitButton = new Button("Submit");
                    reportSubmitButton.setStyle("-fx-font-size: 14px; -fx-background-color: rgba(245, 66, 66);");
                    reportSubmitButton.setAlignment(Pos.CENTER);
                    reportSubmitButton.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            String filePath = "reports.txt";
                            try (FileWriter writer = new FileWriter(filePath, true)) {
                                writer.write(programmingLanguage + ": " + currentModule + "; " + reportTextArea.getText() + "\n");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            report_visible = false;
                            outerPane.setTop(null);
                        }
                    });

                    HBox reportHBox = new HBox(2);
                    reportHBox.getChildren().addAll(reportTextArea, reportSubmitButton);
                    reportHBox.setAlignment(Pos.CENTER);
                    outerPane.setTop(reportHBox);
                } else {
                    report_visible = false;
                    outerPane.setTop(null);
                }

            }
        });

        VBox leftBox = new VBox(2);
        leftBox.setAlignment(Pos.CENTER);
        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.CENTER);

        leftBox.getChildren().add(leftArrow);
        if (TEST_MODE == true) {
            rightBox.getChildren().add(reportButton);
        }
        rightBox.getChildren().add(rightArrow);
        outerPane.setLeft(leftBox);
        outerPane.setRight(rightBox);

        //Read JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String filename = "";
        if (currentModule.equals("Variables")) {
            filename = "variables.json";
        } else if (currentModule.equals("Functions")) {
            filename = "functions.json";
        } else if (currentModule.equals("File Handling")) {
            filename = "filehandling.json";
        }
        if (programmingLanguage.equals("Java")) {
            try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("json/" + filename)) {
                if (inputStream == null) {
                    throw new IOException("File not found");
                }
                ArrayList<Map<String, Object>> dataList = objectMapper.readValue(inputStream, ArrayList.class);
                for (Map<String, Object> data : dataList) {
                    String type = (String) data.get("type");
                    VBox screen;
                    if (type.equals("explanation")) {
                        screen = new Explanation((String) data.get("text"), (String) data.get("code_example"));
                    } else if (type.equals("comprehension")) {
                        screen = new Comprehension((String) data.get("text"), (String) data.get("code_example"), (String) data.get("question"), (String) data.get("hint"), (String) data.get("topic"));
                    } else if (type.equals("generation")) {
                        screen = new Generation((String) data.get("text"), (String) data.get("partial_code"), (String) data.get("hint"), (String) data.get("topic"));
                    } else {
                        throw new IllegalArgumentException("Unknown question type");
                    }
                    screens.add(screen);
                    scoresInfo.add(new ScreenScore((String) data.get("type"), (String) data.get("topic")));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = "";
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getClassLoader().getResourceAsStream("json/" + filename)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString += (line);
                }

                ArrayList<Map<String, Object>> translatedCourse = translate(jsonString);
                for (Map<String, Object> data : translatedCourse) {
                    String type = (String) data.get("type");
                    VBox screen;
                    if (type.equals("explanation")) {
                        screen = new Explanation((String) data.get("text"), (String) data.get("code_example"));
                    } else if (type.equals("comprehension")) {
                        screen = new Comprehension((String) data.get("text"), (String) data.get("code_example"), (String) data.get("question"), (String) data.get("hint"), (String) data.get("topic"));
                    } else if (type.equals("generation")) {
                        screen = new Generation((String) data.get("text"), (String) data.get("partial_code"), (String) data.get("hint"), (String) data.get("topic"));
                    } else {
                        throw new IllegalArgumentException("Unknown question type");
                    }
                    screens.add(screen);
                    scoresInfo.add(new ScreenScore((String) data.get("type"), (String) data.get("topic")));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //More module initialisation bits
        currentScreenIndex = 1;
        totalScreenCount = screens.size();
        bonusScreenCount = 0;
        highestScreenPassed = 0;

        switchScreen();

        scene = new Scene(layeredPane, SCENE_WIDTH, SCENE_HEIGHT);
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void switchScreen() {
        VBox thisScreen = screens.get(currentScreenIndex - 1);
        thisScreen.setAlignment(Pos.CENTER);
        outerPane.setCenter(thisScreen);
    }

    public static void startBonusScreens() {
        ArrayList<String> topics = new ArrayList<>();
        for (ScreenScore screen : scoresInfo) {
            if (screen.getType().equals("comprehension") && screen.getScore() < 2) {
                topics.add(screen.getTopic());
            } else if (screen.getType().equals("generation") && screen.getScore() < 3) {
                topics.add(screen.getTopic());
            }
        }
        if (currentScore / maxScore < 0.75) { //Less than 75% score = 2 bonus questions
            bonusScreenCount += 2;
            totalScreenCount += 2;
            Comprehension bonusComprehension = generateComprehension();
            Generation bonusGeneration = generateGeneration();
            screens.add(bonusComprehension);
            screens.add(bonusGeneration);
        } else if (currentScore / maxScore < 1.0) { //Less than 100% score = 1 bonus question
            bonusScreenCount++;
            totalScreenCount++;
            int marksLostOnComprehension = 0;
            int marksLostOnGeneration = 0;
            for (ScreenScore screen : scoresInfo) {
                if (screen.getType().equals("comprehension")) {
                    marksLostOnComprehension += 2 - screen.getScore();
                } else if (screen.getType().equals("generation")) {
                    marksLostOnGeneration += 3 - screen.getScore();
                }
            }
            if (marksLostOnComprehension > marksLostOnGeneration) {
                Comprehension bonusComprehension = generateComprehension();
                screens.add(bonusComprehension);
            } else {
                Generation bonusGeneration = generateGeneration();
                screens.add(bonusGeneration);
            }
        } else {
            displayScores();
        }
    }

    public static void displayScores() {
        Label scoreLabel = new Label("Final Score: " + currentScore + "/" + totalScreenCount);
        outerPane.setCenter(scoreLabel);
        outerPane.setLeft(null);
        outerPane.setRight(null);
    }

    public static void displayFinalScores() {
        //RESET
        outerPane.setLeft(null);
        outerPane.setRight(null);
        currentModule = "";
        totalScreenCount = 0;
        bonusScreenCount = 0;
        highestScreenPassed = 0;
        currentScreenIndex = 0;
        Iterator<ScreenScore> scoreIterator = scoresInfo.iterator();
        while (scoreIterator.hasNext()) {
            scoreIterator.next();
            scoreIterator.remove();
        }
        Iterator<VBox> screenIterator = screens.iterator();
        while (screenIterator.hasNext()) {
            screenIterator.next();
            screenIterator.remove();
        }

        Label finalScoreString = new Label("Final Score:");
        finalScoreString.setPadding(new Insets(10, 10, 10, 10));
        finalScoreString.setStyle("-fx-font-size:20px");
        Label finalScore = new Label("Final Score: " + currentScore + "/" + maxScore);
        finalScore.setPadding(new Insets(10, 10, 10, 10));
        finalScore.setStyle("-fx-font-size:36px");
        Button finishButton = new Button("Finish");
        finishButton.setPadding(new Insets(10, 10, 10, 10));
        finishButton.setStyle("-fx-font-size:20px");
        finishButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Main.openModuleSelectionScreen();
                currentScore = 0;
                maxScore = 0;
            }
        });
        VBox scoresVBox = new VBox();
        scoresVBox.getChildren().addAll(finalScoreString, finalScore, finishButton);
        scoresVBox.setAlignment(Pos.CENTER);
        outerPane.setCenter(scoresVBox);
    }

    private static ArrayList<Map<String, Object>> translate(String jsonString) throws Exception {
        String prompt = "The following JSON object is a Java programming course on the topic of " + currentModule + ". Please output a JSON object of this course translated into the " + programmingLanguage + " programming language. All code should be translated and all text and hints should be changed to match the new language. The JSON list is enclosed in triple backticks below:\n```\n" + jsonString + "\n```\n";
        String translatedJsonString = llm.chat(prompt);

        int jsonStart = translatedJsonString.indexOf('[');
        int jsonEnd = -1;
        String json = "";
        Stack<Character> stack = new Stack<>();

        try {
            for (int i = jsonStart; i < translatedJsonString.length(); i++) {
                char c = translatedJsonString.charAt(i);
                if (c == '[') {
                    stack.push(c);
                } else if (c == ']') {
                    stack.pop();
                    if (stack.isEmpty()) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
            if (jsonStart == -1 || jsonEnd == -1) {
                throw new IllegalArgumentException("No valid JSON list found in input");
            }
            translatedJsonString = translatedJsonString.substring(jsonStart, jsonEnd + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Map<String, Object>> jsonList = (ArrayList<Map<String, Object>>) mapper.readValue(translatedJsonString, new TypeReference<List<Map<String, Object>>>() {});
        return jsonList;
    }

    private static Comprehension generateComprehension() {
        String topicsString = "";
        for (ScreenScore screenScore : scoresInfo) {
            topicsString = topicsString + screenScore.getTopic() + " ";
        }
        String prompt = "A student has completed some exercises on programming in " + programmingLanguage + " on the subject of " + currentModule + ". They answered some questions incorrectly with the following topics: " + topicsString + ". Please generate a new easy question for then to answer based on a couple of their weaknesses. The question should be outputted in JSON format and contain some context, a piece of complete code and a question such as \"what does this code do?\" or \"what is wrong with this code?\" or \"what is a more efficient way to write this code?\". It should also contain a hint and a topic based on what it is about. Here are some example question JSON objects:\n```json\n{\n\t\"type\":\"comprehension\",\n\t\"text\":\"The below code has been created by a student to output whether the input number provided is 18 or over.\",\n\t\"code_example\":\"age = 16;\nif (age >= 18) {\n\tSystem.out.println('Yes, you are 18 or over');\n} else if {\n\tSystem.out.println('No, you are under 18');\n}\",\n\t\"question\":\"What is wrong with the above code?\",\n\t\"topic\":\"else if statements\",\"hint\":\"What should an else if statement specify?\"\n}\n\n{\n\t\"type\":\"comprehension\",\n\t\"text\":\"A student is trying to output their times tables using for loops.\",\n\t\"code_example\":\"for (int i = 1, i <= 12, i++) {\n\tSystem.out.println(i);\n\tSystem.out.println(i*2);\n\tSystem.out.println(*3_;\n}\",\n\t\"questions\":\"What does this code output?\",\n\t\"topic\":\"for loops\",\n\t\"hint\":\"The loop should include the operation i * 3.\"\n}\n```\\nIf the list of topics is large, just pick one or two to focus on to keep the question simple. Remember that the question should be in the " + programmingLanguage + "programming langugage.";
        String result = llm.chat(prompt);

        int jsonStart = result.indexOf('{');
        int jsonEnd = -1;
        String json = "";
        Stack<Character> stack = new Stack<>();

        try {
            for (int i = jsonStart; i < result.length(); i++) {
                char c = result.charAt(i);
                if (c == '{') {
                    stack.push(c);
                } else if (c == '}') {
                    stack.pop();
                    if (stack.isEmpty()) {
                        jsonEnd = i;
                        break;
                    }
                }
            }
            logger.error("COMPREHENSION TEST: " + result);
            if (jsonStart == -1 || jsonEnd == -1) {
                throw new IllegalArgumentException("No valid JSON object found in input");
            }
            json = result.substring(jsonStart, jsonEnd + 1);
            logger.error("JSON: " + json);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> finalQuestionMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Comprehension finalQuestion = new Comprehension ("BONUS: " + finalQuestionMap.get("text"), (String) finalQuestionMap.get("code_example"), (String) finalQuestionMap.get("question"), (String) finalQuestionMap.get("hint"), (String) finalQuestionMap.get("topic"));
            scoresInfo.add(new ScreenScore("comprehension", (String) finalQuestionMap.get("topic")));
            return finalQuestion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Generation generateGeneration() {
        String topicsString = "";
        for (ScreenScore screenScore : scoresInfo) {
            topicsString = topicsString + screenScore.getTopic() + " ";
        }
        String prompt = "A student has completed some exercises on programming in " + programmingLanguage + " on the subject of " + currentModule + ". They answered some questions incorrectly with the following topics: \"" + topicsString + "\". Please generate a new easy question for them to answer based on a couple of their weaknesses. The question should be outputted in JSON format and contain a question, some uncompleted code for them to complete, a topic and an optional hint. Here are some example question JSON objects:\n```json\n{\n\t\"type\":\"generation\",\n\t\"text\":\"The below code should validate whether the input is true or false. Complete the code.\",\n\t\"partial_code\":\"input = true;\nif (/* Code here */) {\n\tSystem.out.println('Yes, the input is true');\n}\",\n\t\"topic\":\"comparison operators\",\n\t\"hint\":\"You will need to use the == comparison operator\"\n}\n\n{\n\t\"type\":\"generation\",\n\t\"text\":\"The below code should store the result of multiplying two numbers and then print the result. Complete the code.\",\n\t\"partial_code\":\"int a = 4;\nint b = 5;\nint result = /* Code here */;\nSystem.out.println(result);\",\n\t\"topic\":\"multiplication operator\",\n\t\"hint\":\"Use the * operator to multiply two numbers.\"\n}\n```\nIf the list of topics is large, just pick one or two to focus on to keep the question simple. Remember that the question should be in the " + programmingLanguage + "programming langugage.";
        String result = llm.chat(prompt);

        int jsonStart = result.indexOf('{');
        int jsonEnd = -1;
        String json = "";
        Stack<Character> stack = new Stack<>();

        try {
            for (int i = jsonStart; i < result.length(); i++) {
                char c = result.charAt(i);
                if (c == '{') {
                    stack.push(c);
                } else if (c == '}') {
                    stack.pop();
                    if (stack.isEmpty()) {
                        jsonEnd = i;
                        break;
                    }
                }
            }

            if (jsonStart == -1 || jsonEnd == -1) {
                throw new IllegalArgumentException("No valid JSON object found in input");
            }
            json = result.substring(jsonStart, jsonEnd + 1);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> finalQuestionMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Generation finalQuestion = new Generation ("BONUS: " + (String) finalQuestionMap.get("text"), (String) finalQuestionMap.get("partial_code"), (String) finalQuestionMap.get("hint"), (String) finalQuestionMap.get("topic"));
            scoresInfo.add(new ScreenScore("generation", (String) finalQuestionMap.get("topic")));
            return finalQuestion;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void startLoading() {
        Platform.runLater(() -> loadingOverlay.setVisible(true));
    }

    public static void endLoading() {
        Platform.runLater(() -> loadingOverlay.setVisible(false));
    }


    @Override
    public void handle(ActionEvent actionEvent) {
        return;
    }
}