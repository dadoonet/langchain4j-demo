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

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;
import org.tinylog.Logger;

public class Step5EmbedddingsTest extends AbstractParentTest {

    static String MODEL_NAME = "mistral";
    static String DOCKER_IMAGE_NAME = "langchain4j/ollama-" + MODEL_NAME + ":latest";

    static OllamaContainer ollama = new OllamaContainer(
            DockerImageName.parse(DOCKER_IMAGE_NAME).asCompatibleSubstituteFor("ollama/ollama"));

    @BeforeAll
    public static void setup() {
        ollama.start();
    }

    @AfterAll
    public static void teardown() {
        ollama.stop();
    }

    @Test
    public void testGenerateEmbeddings() {
        EmbeddingModel model = OllamaEmbeddingModel.builder()
                .baseUrl(ollama.getEndpoint())
                .modelName(MODEL_NAME)
                .build();

        Logger.info("Embedding model has {} dimensions.", model.dimension());

        TextSegment game1 = TextSegment.from("""
                    The game starts off with the main character Guybrush Threepwood stating \"I want to be a pirate!\"
                    To do so, he must prove himself to three old pirate captains. During the perilous pirate trials, 
                    he meets the beautiful governor Elaine Marley, with whom he falls in love, unaware that the ghost pirate 
                    LeChuck also has his eyes on her. When Elaine is kidnapped, Guybrush procures crew and ship to track 
                    LeChuck down, defeat him and rescue his love.
                """, Metadata.from("gameName", "The Secret of Monkey Island"));
        Response<Embedding> response1 = model.embed(game1);
        Logger.info("Generated a vector of {} dimensions.", response1.content().dimension());
        TextSegment game2 = TextSegment.from("""
                    Out Run is a pseudo-3D driving video game in which the player controls a Ferrari Testarossa 
                    convertible from a third-person rear perspective. The camera is placed near the ground, simulating 
                    a Ferrari driver's position and limiting the player's view into the distance. The road curves, 
                    crests, and dips, which increases the challenge by obscuring upcoming obstacles such as traffic 
                    that the player must avoid. The object of the game is to reach the finish line against a timer.
                    The game world is divided into multiple stages that each end in a checkpoint, and reaching the end 
                    of a stage provides more time. Near the end of each stage, the track forks to give the player a 
                    choice of routes leading to five final destinations. The destinations represent different 
                    difficulty levels and each conclude with their own ending scene, among them the Ferrari breaking 
                    down or being presented a trophy.
                """, Metadata.from("gameName", "Out Run"));
        Response<Embedding> response2 = model.embed(game2);
        Logger.info("Generated a vector of {} dimensions.", response2.content().dimension());
    }
}
