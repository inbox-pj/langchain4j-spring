package com.cardconnect.langchain4j_spring.controller;

import com.cardconnect.langchain4j_spring.assistant.CustomerSupportAgent;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import dev.langchain4j.service.Result;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@RequestMapping("/agent")
public class AgentController {

    private CustomerSupportAgent customerSupportAgent;
    private RouterAgent agent;

    @GetMapping("/ask")
    public String chat(String request) {
        return agent.askToExpert(request);
    }

    @GetMapping("/support")
    public String customerSupportAgent(@RequestParam String sessionId, @RequestParam String userMessage) {
        Result<String> result = customerSupportAgent.answer(sessionId, userMessage);
        return result.content();
    }
}