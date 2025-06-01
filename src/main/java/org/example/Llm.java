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

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.*;

import com.openai.models.chat.completions.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Llm {

    OpenAIClient client;
    //static ChatModel MODEL = ChatModel.GPT_3_5_TURBO;
    static ChatModel MODEL = ChatModel.GPT_4O_2024_08_06;

    Logger logger = org.apache.logging.log4j.LogManager.getLogger(Llm.class);

    //Initialise scanner and OpenAI client
    public Llm() {
        client = OpenAIOkHttpClient.builder()
                .apiKey("") //Add API key here
                .build();
    }

    //Send input to API and receive response
    public String chat(String input) {
        ChatCompletionCreateParams parameters = ChatCompletionCreateParams.builder()
                .model(MODEL)
                .addMessage(ChatCompletionUserMessageParam.builder()
                        .content(input)
                        .build())
                .build();
        try {
            ChatCompletion response = client.chat().completions().create(parameters);
            String output = response.choices().get(0).message().content().get();
            return output;
        } catch (OpenAIServiceException e) {
            if (e.statusCode() == 429) {
                logger.error("Client error while calling OpenAI API: status code 429: not enough credits!");
            }
            else if (String.valueOf(e.statusCode()).charAt(0) == '4') {
                logger.error("Client error while calling OpenAI API: status code " + e.statusCode() + ": " + e.getMessage());
            } else {
                logger.error("Server error while calling OpenAI API: status code " + e.statusCode() + ": " + e.getMessage());
            }
            return "no u";
        }
    }
}