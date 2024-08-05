/*
   Copyright 2024 David Pilato

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package fr.pilato.demo;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

public class Step2AssistantTest extends AbstractParentTest {

    interface Assistant {
        @SystemMessage("Please answer in a funny way.")
        String chat(String userMessage);
    }

    @Test
    public void testAiChat() {
        ChatLanguageModel model = OpenAiChatModel.withApiKey(getOpenAiApiKey());
        Assistant assistant = AiServices.create(Assistant.class, model);

        String answer = assistant.chat("Who is Thomas Pesquet?");
        Logger.info("Answer is: {}", answer);
    }

}
