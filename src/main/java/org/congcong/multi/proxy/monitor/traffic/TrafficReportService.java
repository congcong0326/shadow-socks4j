package org.congcong.multi.proxy.monitor.traffic;

import org.congcong.multi.proxy.common.SingletonUtil;
import org.congcong.multi.proxy.common.TimeUtil;
import org.congcong.multi.proxy.entity.Context;
import org.congcong.multi.proxy.entity.UserAccessEntity;
import org.congcong.multi.proxy.entity.UserAccessIpEntity;
import org.congcong.multi.proxy.monitor.connect.ConnectionMonitor;
import org.congcong.multi.proxy.monitor.connect.IConnectionReport;
import org.congcong.multi.proxy.monitor.db.DataPartition;
import org.congcong.multi.proxy.monitor.db.mapDb.DataPartitionImpl;
import org.congcong.multi.proxy.monitor.db.mapDb.DbMangeService;
import org.congcong.multi.proxy.monitor.db.PartitionType;

import java.util.*;
import java.util.stream.Collectors;

public class TrafficReportService implements ITrafficReport, ITrafficAnalysis {




    private final DataPartition<Context> dataPartition = new DataPartitionImpl<>();
    private final DataPartition<Long> trafficPartition = new DataPartitionImpl<>();

    /**
     * 用户的总流量
     */
    private final String TRAFFIC_USER_TABLE = "TRAFFIC_USER_TABLE";

    /**
     * 总下行流量
     */
    private final String TRAFFIC_ALL_DOWN_TABLE = "TRAFFIC_ALL_DOWN_TABLE";

    /**
     * 总上行流量
     */
    private final String TRAFFIC_ALL_UP_TABLE = "TRAFFIC_ALL_UP_TABLE";

    //private final String TRAFFIC_ALL_30_DAT_TABLE = "TRAFFIC_ALL_30_DAT_TABLE";

    private static final IConnectionReport connectionReport = SingletonUtil.getInstance(ConnectionMonitor.class);


    @Override
    public void report(Context context) {
        // 每个人的访问数据，按照时间分区
        if (context != null && context.getUserName() != null) {
            dataPartition.writeByTime(context, context.getUserName(), PartitionType.DAY);
            long total = context.getUp() + context.getDown();
            trafficPartition.writeOrMerge(TRAFFIC_USER_TABLE, context.getUserName(), total, PartitionType.DAY);
            trafficPartition.writeOrMerge(TRAFFIC_ALL_DOWN_TABLE, TRAFFIC_ALL_DOWN_TABLE, context.getDown(), PartitionType.DAY);
            trafficPartition.writeOrMerge(TRAFFIC_ALL_UP_TABLE, TRAFFIC_ALL_UP_TABLE, context.getUp(), PartitionType.DAY);
            //trafficPartition.writeOrMerge(TRAFFIC_ALL_30_DAT_TABLE, TRAFFIC_ALL_30_DAT_TABLE, total, PartitionType.MONTH);
            connectionReport.reportAccessIp(context.getClientIp());
            DbMangeService.getMangeDb().commit();
        }

    }

    @Override
    public Long getTotalUpTraffic(long currentTime, PartitionType partitionType) {
        if (partitionType == PartitionType.DAY) {
            return getSingleDayUpTraffic(currentTime);
        } else {
            List<Long> timestampsFromTodayToMonthStart = TimeUtil.getTimestampsFromTodayToMonthStart(currentTime);
            Long result = 0L;
            for (Long ago : timestampsFromTodayToMonthStart) {
                result += getSingleDayUpTraffic(ago);
            }
            return result;
        }
    }

    private Long getSingleDayUpTraffic(long time) {
        List<Long> query = trafficPartition.query(time, TRAFFIC_ALL_UP_TABLE);
        if (query == null || query.isEmpty()) {
            return 0L;
        }
        return query.get(0);
    }

    @Override
    public Long getTotalDownTraffic(long currentTime, PartitionType partitionType) {
        if (partitionType == PartitionType.DAY) {
            return getSingleDayDownTraffic(currentTime);
        } else {
            List<Long> timestampsFromTodayToMonthStart = TimeUtil.getTimestampsFromTodayToMonthStart(currentTime);
            Long result = 0L;
            for (Long ago : timestampsFromTodayToMonthStart) {
                result += getSingleDayDownTraffic(ago);
            }
            return result;
        }
    }

    private Long getSingleDayDownTraffic(long time) {
        List<Long> query = trafficPartition.query(time, TRAFFIC_ALL_DOWN_TABLE);
        if (query == null || query.isEmpty()) {
            return 0L;
        }
        return query.get(0);
    }

    @Override
    public Map<String, Long> getTop10Traffic(long currentTime, PartitionType partitionType) {
        if (partitionType == PartitionType.DAY) {
            return getSingleUserTraffic(currentTime);
        } else {
            List<Long> timestampsFromTodayToMonthStart = TimeUtil.getTimestampsFromTodayToMonthStart(currentTime);
            Map<String, Long> result = new HashMap<>();
            for (Long ago : timestampsFromTodayToMonthStart) {
                getSingleUserTraffic(ago).forEach((k,v) -> {
                    if (result.containsKey(k)) {
                        result.put(k, result.getOrDefault(k, 0L) + v);
                    } else {
                        result.put(k, v);
                    }
                });
            }
            return result;
        }
    }

    private Map<String, Long> getSingleUserTraffic(long currentTime) {
        return trafficPartition.queryMap(currentTime, TRAFFIC_USER_TABLE);
    }

    @Override
    public List<UserAccessEntity> getUserAccessDetail(long currentTime, String userName) {
        Map<String, Context> stringContextMap = dataPartition.queryMap(System.currentTimeMillis(), userName);
        Map<String, UserAccessEntity> result = new HashMap<>();

        // 累计访问次数、上行流量和下行流量
        for (Context value : stringContextMap.values()) {
            UserAccessEntity userAccessEntity = result.get(value.getRemoteUrl());
            if (userAccessEntity != null) {
                userAccessEntity.setAccessCount(userAccessEntity.getAccessCount() + 1);
                userAccessEntity.setUp(userAccessEntity.getUp() + value.getUp());
                userAccessEntity.setDown(userAccessEntity.getDown() + value.getDown());
                userAccessEntity.setDurationTime(userAccessEntity.getDurationTime() + value.getDurationTime());
                if (value.getStartTime() > userAccessEntity.getLastAccessTime()) {
                    userAccessEntity.setLastAccessTime(value.getStartTime());
                }
            } else {
                userAccessEntity = new UserAccessEntity();
                userAccessEntity.setAccessCount(1);
                userAccessEntity.setUp(value.getUp());
                userAccessEntity.setDown(value.getDown());
                userAccessEntity.setDurationTime(value.getDurationTime());
                userAccessEntity.setLastAccessTime(value.getStartTime());
                userAccessEntity.setRemoteUrl(value.getRemoteUrl());
                userAccessEntity.setClientIp(value.getClientIp());
                result.put(value.getRemoteUrl(), userAccessEntity);
            }
        }

        // 按访问次数降序排序并转换为 LinkedHashMap
        return result.values().stream().sorted(new Comparator<UserAccessEntity>() {
            @Override
            public int compare(UserAccessEntity o1, UserAccessEntity o2) {
                return (int) (-o1.getDown() + o2.getDown());
            }
        }).collect(Collectors.toList());
    }
}
