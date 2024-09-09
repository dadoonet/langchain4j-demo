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
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationScript;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.tinylog.Logger;

import java.io.IOException;

public class Step7SearchForVectorsTest extends AbstractParentTest {

    private static TextSegment game1;
    private static TextSegment game2;
    private static Response<Embedding> response1;
    private static Response<Embedding> response2;
    private static RestClient client;
    private static OpenAiEmbeddingModel model;

    @BeforeAll
    public static void startElasticsearchContainer() throws IOException {
        // Create the elasticsearch container.
        ElasticsearchContainer container =
                new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.15.0")
                        .withPassword("changeme")
                        .withReuse(true);

        // Start the container. This step might take some time...
        container.start();

        // As we don't want to make our TestContainers code more complex than
        // needed, we will use login / password for authentication.
        // But note that you can also use API keys which is preferred.
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));

        // Create a low level Rest client which connects to the elasticsearch container.
        client = RestClient.builder(HttpHost.create("https://" + container.getHttpHostAddress()))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    httpClientBuilder.setSSLContext(container.createSslContextFromCa());
                    return httpClientBuilder;
                })
                .build();

        // Check the cluster is running
        client.performRequest(new Request("GET", "/"));
    }

    @BeforeAll
    public static void generateEmbeddings() {
        model = OpenAiEmbeddingModel.builder()
                .apiKey(getOpenAiApiKey())
                .build();
        game1 = TextSegment.from("""
                    The game starts off with the main character Guybrush Threepwood stating "I want to be a pirate!"
                    To do so, he must prove himself to three old pirate captains. During the perilous pirate trials,
                    he meets the beautiful governor Elaine Marley, with whom he falls in love, unaware that the ghost pirate
                    LeChuck also has his eyes on her. When Elaine is kidnapped, Guybrush procures crew and ship to track
                    LeChuck down, defeat him and rescue his love.
                """, Metadata.from("gameName", "The Secret of Monkey Island"));
        response1 = model.embed(game1);
        game2 = TextSegment.from("""
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
        response2 = model.embed(game2);
    }

    @Test
    public void testSearch() throws IOException {
        EmbeddingStore<TextSegment> embeddingStore =
                ElasticsearchEmbeddingStore.builder()
                        .indexName("games")
                        .restClient(client)
                        .build();
        embeddingStore.removeAll();
        embeddingStore.add(response1.content(), game1);
        embeddingStore.add(response2.content(), game2);

        client.performRequest(new Request("POST", "/games/_refresh"));

        String question = "I want to pilot a car";
        Embedding questionAsVector = model.embed(question).content();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(questionAsVector)
                .build());
        result.matches().forEach(m -> Logger.info("{} - score [{}]",
                m.embedded().metadata().getString("gameName"), m.score()));
    }

    @Test
    public void testSearchWithScript() throws IOException {
        EmbeddingStore<TextSegment> embeddingStore =
                ElasticsearchEmbeddingStore.builder()
                        .configuration(ElasticsearchConfigurationScript.builder().build())
                        .indexName("games")
                        .restClient(client)
                        .build();
        embeddingStore.removeAll();
        embeddingStore.add(response1.content(), game1);
        embeddingStore.add(response2.content(), game2);

        client.performRequest(new Request("POST", "/games/_refresh"));

        String question = "I want to pilot a car";
        Embedding questionAsVector = model.embed(question).content();

        Logger.info("{}", questionAsVector.vectorAsList());

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(questionAsVector)
                .build());
        result.matches().forEach(m -> Logger.info("{} - score [{}]",
                m.embedded().metadata().getString("gameName"), m.score()));
    }
}
