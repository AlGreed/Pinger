package gr.planetz;

import java.io.IOException;
import java.util.Map;

public interface PingingService {

    /**
     * Start of service.
     */
    void start();

    /**
     * Stop of service.
     */
    void stop();

    /**
     * Getter of players with their ip addresses.
     *
     * @return players.
     */
    Map<String, String> getPlayers();

    /**
     * Frequency of pinging.
     *
     * @return period time in millis.
     */
    long getPeriod();

    /**
     * Frequency of pinging. Default value is 2000 millis. If you change the period value after start, you need to restart service.
     *
     * @param period time in millis.
     */
    void setPeriod(long period);

    /**
     * Check whether service is running.
     *
     * @return boolean
     */
    boolean isRunning();

    Map<String, String> getPlayersDirectlyOverHttpGetRequest() throws IOException;
}
