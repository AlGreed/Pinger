package gr.planetz.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gr.planetz.PingingService;
import gr.planetz.model.PingRequest;
import gr.planetz.model.PingResponse;

public class HttpPingingService implements PingingService {

    private static final Logger LOG = LoggerFactory.getLogger(HttpPingingService.class.getName());

    private final String uri;

    private final ObjectMapper mapper;

    private final HttpClient httpClient;

    private final PingRequest request;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final Map<String, String> players = new ConcurrentHashMap<>();

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private long period = 2000;

    public HttpPingingService(final String uri, final String nickname, final String address, final String keystore, final String password) throws CertificateException, NoSuchAlgorithmException,
            KeyStoreException, IOException, KeyManagementException {
        this.uri = Objects.requireNonNull(uri, "|uri| must not be null!");
        this.request = new PingRequest(Objects.requireNonNull(nickname, "|nickname| must not be null!"), Objects.requireNonNull(address, "|address| must not be null!"));
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        if (keystore != null && password != null){
            LOG.info("Configuring of SSL...");
            final SSLContext sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(new File(keystore), password.toCharArray(), new TrustSelfSignedStrategy()).build();
            final SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            httpClientBuilder = httpClientBuilder.setSSLSocketFactory(sslsf);
        }
        this.httpClient = httpClientBuilder.build();
        this.mapper = new ObjectMapper();
    }

    @Override
    public void start() {
        LOG.info("HttpPingingService is started.");
        if (!isRunning()) {
            this.isRunning.set(true);
            this.executor.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final PingResponse response = ping();
                                LOG.debug("Players on server: {}.", response.toString());
                                final Map<String, String> map = response.getPlayers();
                                for (final String player : players.keySet()) {
                                    if (!map.containsKey(player)) {
                                        players.remove(player);
                                    }
                                }
                                for (final String player : map.keySet()) {
                                    players.putIfAbsent(player, map.get(player));
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, this.period, TimeUnit.MILLISECONDS);
        }
    }

    private PingResponse ping() throws Exception {
        final HttpResponse response = this.httpClient.execute(createHttpPost());
        final String resp = EntityUtils.toString(response.getEntity());
        return this.mapper.readValue(resp, PingResponse.class);
    }

    private HttpPost createHttpPost() throws Exception {
        final HttpPost httpPost = new HttpPost(this.uri);
        final StringEntity params = new StringEntity(this.mapper.writeValueAsString(this.request));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.setEntity(params);
        return httpPost;
    }

    @Override
    public void stop() {
        if (isRunning()) {
            this.players.clear();
            this.executor.shutdown();
            this.isRunning.set(false);
        }
    }

    @Override
    public Map<String, String> getPlayersDirectlyOverHttpGetRequest() throws IOException {
        if (!isRunning()) {
            final HttpGet request = new HttpGet(URI.create(this.uri));
            final HttpResponse response = httpClient.execute(request);
            return this.mapper.readValue(EntityUtils.toString(response.getEntity()), PingResponse.class).getPlayers();
        }
        return null;
    }

    @Override
    public Map<String, String> getPlayers() {
        final Map<String, String> shallowCopy = new HashMap<>();
        shallowCopy.putAll(this.players);
        return shallowCopy;
    }

    @Override
    public long getPeriod() {
        return this.period;
    }

    @Override
    public void setPeriod(final long period) {
        this.period = period;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }
}
