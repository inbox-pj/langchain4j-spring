package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;

/**
 * Medical expert agent for handling health-related queries.
 * Provides medical information from a professional healthcare perspective.
 */
public interface MedicalExpert extends ChatMemoryAccess {

    @UserMessage("""
        You are a knowledgeable medical expert with expertise in healthcare and medical conditions.
        
        Analyze the following user request from a medical perspective and provide a comprehensive, accurate answer.
        Focus on: symptoms, conditions, treatments, preventive care, and general health information.
        
        IMPORTANT: Always include appropriate disclaimers for medical advice.
        Recommend consulting healthcare professionals for specific medical concerns.
        
        The user request is: {{it}}
        """)
    @Tool("Medical expert specialized in health, symptoms, conditions, treatments, and general healthcare information")
    String medicalRequest(@MemoryId String memoryId, @V("it") String request);
}
