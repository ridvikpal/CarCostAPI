package com.carcost.CarCost.model.chatgpt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class ChatGPTConnection {
    private static WebClient webClient;

    private static String openAiApiKey;

    private static final String openAiUrl = "https://api.openai.com/v1/chat/completions";

    @Autowired
    public ChatGPTConnection(WebClient.Builder webClient, @Value("${openai.api.key}") String openAiApiKey) {
        this.webClient = WebClient.builder().baseUrl(openAiUrl).build();
        this.openAiApiKey = openAiApiKey;
    }

    private static String getChatGPTResponse(String _model, String _request, double _temperature){
        ChatGPTRequest.Message _message = new ChatGPTRequest.Message(
                "user",
                _request
        );
        ArrayList<ChatGPTRequest.Message> _messages = new ArrayList<>();
        _messages.add(_message);

        ChatGPTRequest makeInfoRequest = new ChatGPTRequest(_model, _messages, _temperature);

        ChatGPTResponse makeInfoObject = webClient.post()
                .header("Authorization", "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(makeInfoRequest), ChatGPTRequest.class)
                .retrieve().bodyToMono(ChatGPTResponse.class).block();

        return makeInfoObject.getChoices().get(0).getMessage().getContent();
    }

    public static String getMakeInfo(String _make){
        return getChatGPTResponse(
                "gpt-3.5-turbo",
                "Provide a short description of the car manufacturer " + _make + ". Describe the " +
                        "manufacturer based on reviews, reliability, maintenance, cost of operation, and user experience. " +
                        " Then describe up to 5 cars in list format provided by the car manufacturer " + _make +
                        " in the following format: \n\n " +
                        "Name: [Output] \n\n " +
                        "Engine: [Output] \n\n " +
                        "Type: [Output] \n\n " +
                        "Features: [Output] \n\n" +
                        "MSRP: [Output]",
                0.7
        );
    }

    public static String getModelInfo(String _make, String _makeModel, Integer _year){
        if (_make == null || _make.trim().isEmpty() || _makeModel == null || _makeModel.trim().isEmpty()){
            return "";
        }

        if (_year == null || _year < 1950 && _year > 2021){
            return getChatGPTResponse(
                    "gpt-3.5-turbo",
                    "Give me information about the following car: " + _make + " " +  _makeModel,
                    0.7
            );
        }

        return getChatGPTResponse(
                "gpt-3.5-turbo",
                "Give me information about the following car: " + _make + " " +  _makeModel + " " + _year,
                0.7
        );
    }

    public static String getCarRecommendation(String _type, String _make){
        if (_make == null || _make.trim().isEmpty()){
            return getChatGPTResponse(
                    "gpt-3.5-turbo",
                    "Recommend me a " + _type + "based on reviews",
                    0.7
            );
        }
        return getChatGPTResponse(
                "gpt-3.5-turbo",
                "Recommend me a " + _make + " " + _type + "based on reviews",
                0.7
        );
    }
}
