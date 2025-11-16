package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.service.UserMessage;

public interface RouterAgent {

    @UserMessage("""
        Analyze the following user request and categorize it as 'legal', 'medical' 'technical' or mathematical,
        then forward the request as it is to the corresponding expert provided as a tool.
        Finally return the answer that you received from the expert without any modification.

        The user request is: '{{it}}'.
        """)
    String askToExpert(String request);
}

