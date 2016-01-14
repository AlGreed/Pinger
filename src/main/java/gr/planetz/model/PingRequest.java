package gr.planetz.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PingRequest {

    @JsonProperty(value = "nickname", required = true)
    private final String nickname;

    @JsonProperty(value = "address", required = true)
    private final String address;

    public PingRequest(@JsonProperty("nickname") final String nickname, @JsonProperty("address") final String address) {
        this.nickname = nickname;
        this.address = address;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PingRequest that = (PingRequest) o;

        if (nickname != null ? !nickname.equals(that.nickname) : that.nickname != null)
            return false;
        return address != null ? address.equals(that.address) : that.address == null;

    }

    @Override
    public int hashCode() {
        int result = nickname != null ? nickname.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JoinToRoomRequest{" + "nickname='" + nickname + '\'' + ", address='" + address + '\'' + '}';
    }
}
