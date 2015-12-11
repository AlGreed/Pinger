package gr.planetz.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PingResponse {

    @JsonProperty("Players")
    private final Map<String, String> players;

    @JsonCreator
    public PingResponse(
            @JsonProperty("Players")
            final Map<String, String> players) {
        this.players = players;
    }

    @Override
    public int hashCode() {
        return this.players != null ? this.players.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PingResponse)) {
            return false;
        }

        final PingResponse that = (PingResponse) o;

        if (this.players != null ? !this.players.equals(that.players) : that.players != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return this.getPlayers().toString();
    }

    public Map<String, String> getPlayers() {
        return this.players;
    }
}
