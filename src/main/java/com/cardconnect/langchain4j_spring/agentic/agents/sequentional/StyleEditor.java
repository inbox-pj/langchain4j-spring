package com.cardconnect.langchain4j_spring.agentic.agents.sequentional;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface StyleEditor {

    @UserMessage("""
        You are a professional editor.
        Analyze and rewrite the following story to better fit and be more coherent with the {{style}} style.
        Return only the story and nothing else.
        The story is "{{story}}".
        """)
    @Agent(description = "Edits a story to better fit a given style", name = "StyleEditor", outputKey = "story")
    String editStory(@V("story") String story, @V("style") String style);
}