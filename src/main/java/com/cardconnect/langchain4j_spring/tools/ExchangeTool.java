package com.cardconnect.langchain4j_spring.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class ExchangeTool {

    @Tool("Exchange the given amount of money from the original to the target currency")
    Double exchange(@P("originalCurrency") String originalCurrency, @P("amount") Double amount, @P("targetCurrency") String targetCurrency) {
        // Dummy exchange rates for demonstration purposes
        double exchangeRate = getExchangeRate(originalCurrency, targetCurrency);
        return amount * exchangeRate;
    }

    private double getExchangeRate(String originalCurrency, String targetCurrency) {
        // In a real application, this method would call an external service to get current exchange rates
        if (originalCurrency.equals("USD") && targetCurrency.equals("EUR")) {
            return 0.85; // Example rate
        } else if (originalCurrency.equals("EUR") && targetCurrency.equals("USD")) {
            return 1.18; // Example rate
        }
        // Default exchange rate
        return 1.0;
    }
}