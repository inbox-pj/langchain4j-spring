package com.cardconnect.langchain4j_spring.agentic.agents.humantntheloop;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DecisionsReachedService {
    @SystemMessage("Given the interaction, return true if a decision has been reached, " +
            "false if further discussion is needed to find a solution.")
    @UserMessage("""
            Interaction so far:
             Secretary: {{proposal}}
             Invitee: {{candidateAnswer}}
    """)
    @Agent("Determines if a decision has been reached based on the proposal and candidate answer")
    boolean isDecisionReached(@V("proposal") String proposal, @V("candidateAnswer") String candidateAnswer);
}