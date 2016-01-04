package gr.planetz;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import gr.planetz.impl.HttpPingingService;

@RunWith(MockitoJUnitRunner.class)
public class HttpPingingServiceTest {

    private static final String URI = "uri_to_nowhere.hell";

    private static final String NICKNAME = "AlGreed";

    private static final String JSON_RESPONSE = "{\"Players\":{\"AlGreed\":\"100.50.23.34\",\"DukeNukem\":\"123.34.52.12\"}}";

    private final HttpClient client = mock(HttpClient.class);

    private final HttpResponse response = mock(HttpResponse.class);

    private PingingService objectUnderTest;

    @Before
    public void before() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.objectUnderTest = new HttpPingingService(URI, NICKNAME, null, null);
    }

    @After
    public void after() {
        if (this.objectUnderTest != null) {
            this.objectUnderTest.stop();
        }
    }

    @Test(expected = NullPointerException.class)
    public void assertUriIsNotNull() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        new HttpPingingService(null, NICKNAME,  null, null);
    }

    @Test(expected = NullPointerException.class)
    public void assertNicknameIsNotNull() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        new HttpPingingService(URI, null,  null, null);
    }

    @Test
    public void assertThatServiceStarts() throws Exception {
        // perform
        this.objectUnderTest.start();

        // test
        assertTrue("The service should be started!", this.objectUnderTest.isRunning());
    }

    @Test
    public void assertThatServiceIsStoppedAtTheBeginning() throws Exception {
        // test
        assertFalse("The service should be stopped!", this.objectUnderTest.isRunning());
    }

    @Test
    public void assertThatServiceStops() throws Exception {
        // perform
        this.objectUnderTest.start();
        this.objectUnderTest.stop();

        // test
        assertFalse("The service should be stopped!", this.objectUnderTest.isRunning());
    }

    @Test
    public void assertThatChangingOfPeriodIsWorking() throws Exception {
        // prepare
        final long defaultPeriod = 2000;
        final long newPeriod = 4000;

        // test
        assertEquals("The period should be default!", defaultPeriod, this.objectUnderTest.getPeriod());
        this.objectUnderTest.setPeriod(newPeriod);
        assertEquals("The period should not be default!", newPeriod, this.objectUnderTest.getPeriod());
    }
}
