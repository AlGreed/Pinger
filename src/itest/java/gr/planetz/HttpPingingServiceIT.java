package gr.planetz;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.planetz.impl.HttpPingingService;

import com.github.tomakehurst.wiremock.http.MimeType;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

public class HttpPingingServiceIT {

    private static final Logger LOG = LoggerFactory.getLogger(HttpPingingServiceIT.class.getName());

    private static final int PORT = 12555;

    private static final String URI = "http://localhost:" + PORT;

    @ClassRule
    public static WireMockClassRule WIRE_MOCK_RULE = new WireMockClassRule(PORT);

    @Rule
    public WireMockClassRule wireMockInstanceRule = WIRE_MOCK_RULE;

    private static final String NICKNAME = "AlGreed";

    @BeforeClass
    public static void setStubs() {
        WIRE_MOCK_RULE.addMockServiceRequestListener(
                (request, response) -> {
                    LOG.info("Request: {}", request.getBodyAsString());
                    LOG.info("Response: {}", response.getBodyAsString());
                });
    }

    @Test
    public void assertThatRequestWillBeSentAndResponseContainsRightNumberOfPlayers() throws Exception {
        // prepare
        final String expectedJson = "{\"Players\":{\"AlGreed\":\"100.50.23.34\",\"DukeNukem\":\"123.34.52.12\"}}";
        final int numberOfPlayers = 2;
        final PingingService service = new HttpPingingService(URI, NICKNAME, null);

        // @formatter:off
        WIRE_MOCK_RULE.stubFor(post(urlMatching(".*"))
                .withHeader("Accept", equalTo(MimeType.JSON.toString()))
                .withHeader("Content-Type", equalTo(MimeType.JSON.toString()))
                .willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED).withFixedDelay(200)
                        .withHeader("Content-Type", MimeType.JSON.toString())
                        .withBody(expectedJson)));
        // @formatter:on

        // perform
        service.start();
        Thread.sleep(service.getPeriod() / 2);

        // test
        assertEquals("Number of players is wrong!", numberOfPlayers, service.getPlayers().size());

        // clean
        service.stop();
    }

    @Test
    public void assertThatChangingWithTimeNumberOfPlayersWillBeCorrectRecognized() throws Exception {
        // prepare
        final String twoPlayers = "{\"Players\":{\"AlGreed\":\"100.50.23.34\",\"DukeNukem\":\"123.34.52.12\"}}";
        final String onePlayer = "{\"Players\":{\"AlGreed\":\"100.50.23.34\"}}";
        final String threePlayers = "{\"Players\":{\"AlGreed\":\"100.50.23.34\",\"Flash\":\"123.34.52.13\",\"Batman\":\"123.34.52.14\"}}";
        final PingingService service = new HttpPingingService(URI, NICKNAME, null);

        // perform
        service.start();

        // test
        createStub(twoPlayers);
        Thread.sleep(service.getPeriod() / 2);
        assertEquals("Number of players is wrong!", 2, service.getPlayers().size());

        createStub(onePlayer);
        Thread.sleep(service.getPeriod());
        assertEquals("Number of players is wrong!", 1, service.getPlayers().size());

        createStub(threePlayers);
        Thread.sleep(service.getPeriod());
        assertEquals("Number of players is wrong!", 3, service.getPlayers().size());

        // clean
        service.stop();
    }

    private void createStub(final String content) {
        WIRE_MOCK_RULE.resetMappings();
        // @formatter:off
        WIRE_MOCK_RULE.stubFor(post(urlMatching(".*"))
                .withHeader("Accept", equalTo(MimeType.JSON.toString()))
                .withHeader("Content-Type", equalTo(MimeType.JSON.toString()))
                .willReturn(aResponse().withStatus(HttpStatus.SC_ACCEPTED)
                        .withHeader("Content-Type", MimeType.JSON.toString())
                        .withBody(content)));
        // @formatter:on
    }
}
