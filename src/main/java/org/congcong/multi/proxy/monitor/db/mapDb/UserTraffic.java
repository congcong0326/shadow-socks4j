package org.congcong.multi.proxy.monitor.db.mapDb;

import java.util.Objects;

public class UserTraffic {

    private final String userId;
    private final long totalTraffic;

    public UserTraffic(String userId, long totalTraffic) {
        this.userId = userId;
        this.totalTraffic = totalTraffic;
    }

    public String getUserId() {
        return userId;
    }

    public long getTotalTraffic() {
        return totalTraffic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTraffic that = (UserTraffic) o;
        return totalTraffic == that.totalTraffic && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, totalTraffic);
    }

    @Override
    public String toString() {
        return "UserTraffic{" +
                "userId='" + userId + '\'' +
                ", totalTraffic=" + totalTraffic +
                '}';
    }
}
