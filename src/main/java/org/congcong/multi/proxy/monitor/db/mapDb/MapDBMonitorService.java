//package org.congcong.multi.proxy.monitor.impl.mapDb;
//
//import org.congcong.multi.proxy.entity.Pair;
//import org.congcong.multi.proxy.monitor.DbType;
//import org.congcong.multi.proxy.monitor.IMonitorService;
//import org.congcong.multi.proxy.monitor.UserTrafficType;
//import org.mapdb.DB;
//import org.mapdb.DBMaker;
//import org.mapdb.Serializer;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentMap;
//
//public class MapDBMonitorService implements IMonitorService {
//
//    private final DB db;
//    private Map<String, Long> userVisitedUrls;
//    private Map<String, Long> userTrafficUp;
//    private Map<String, Long> userTrafficDown;
//
//    private Map<String, Long> userTotalTrafficMap;
//
//    public MapDBMonitorService() {
//        // 使用内存数据库，如果需要持久化则替换为持久化路径
//        this.db = DBMaker.fileDB("traffic-monitor.db").make();
//
//        // 创建用于存储访问记录的 Map
//        this.userVisitedUrls = db.treeMap("sortedUserVisitedUrls").keySerializer(Serializer.STRING) // 键序列化器
//                .valueSerializer(Serializer.LONG) // 值序列化器
//                .createOrOpen();
//
//        // 创建用于存储用户流量的 Map
//        this.userTrafficUp = db.treeMap("sortedUserTrafficUp").keySerializer(Serializer.STRING) // 键序列化器
//                .valueSerializer(Serializer.LONG) // 值序列化器
//                .createOrOpen();
//        this.userTrafficDown = db.treeMap("sortedUserTrafficDown").keySerializer(Serializer.STRING) // 键序列化器
//                .valueSerializer(Serializer.LONG) // 值序列化器
//                .createOrOpen();
//
//        this.userTotalTrafficMap = db.treeMap("sortedTrafficMap").keySerializer(Serializer.STRING) // 键序列化器
//                .valueSerializer(Serializer.LONG) // 值序列化器
//                .createOrOpen();
//
//
//    }
//
//    @Override
//    public DbType getDataStoreType() {
//        return DbType.MapDb;  // 假设 MapDB 是唯一的存储类型
//    }
//
//    @Override
//    public Map<String, Long> getUserVisitedUrls(String userId) {
//        Set<String> keySet = userVisitedUrls.keySet();
//        Map<String, Long> result = new HashMap<>();
//        for (String key : keySet) {
//            Pair<String, String> split = split(key);
//            if (Objects.equals(split.getFst(), userId)) {
//                result.put(split.getSnd(), userVisitedUrls.get(key));
//            }
//        }
//        return result;
//    }
//
//
//
//    @Override
//    public Long getUrlTraffic(String userId, String url, UserTrafficType userTrafficType) {
//        if (userTrafficType == UserTrafficType.UP) {
//            return userTrafficUp.get(buildKey(userId, url));
//        } else {
//            return userTrafficDown.get(buildKey(userId, url));
//        }
//    }
//
//    @Override
//    public Map<String, Long> getAllTraffic() {
//        return userTotalTrafficMap;
//    }
//
//    @Override
//    public Long getUserTraffic(String userId, UserTrafficType trafficType) {
//        // 返回用户的上行或下行流量
//        Map<String,Long> map = trafficType == UserTrafficType.UP ? userTrafficUp : userTrafficDown;
//        Long result = 0L;
//        for (Map.Entry<String, Long> stringLongEntry : map.entrySet()) {
//            result += stringLongEntry.getValue();
//        }
//        return result;
//    }
//
//
//
//    @Override
//    public void reportUserVisit(String userId, String url, long visitCount, long traffic, UserTrafficType trafficType) {
//        // 更新用户访问 URL 的记录
//        boolean needCommit = false;
//        if (visitCount != 0) {
//            userVisitedUrls.put(buildKey(userId, url), userVisitedUrls.getOrDefault(buildKey(userId, url), 0L) + 1);
//            needCommit = true;
//        }
//
//        // 更新 URL 的流量记录
//        if (traffic != 0) {
//            needCommit = true;
//            if (trafficType == UserTrafficType.UP) {
//                userTrafficUp.merge(buildKey(userId, url), traffic, Long::sum);
//            } else {
//                userTrafficDown.merge(buildKey(userId, url), traffic, Long::sum);
//            }
//            userTotalTrafficMap.merge(userId, traffic, Long::sum);
//        }
//        if (needCommit) {
//            db.commit();
//        }
//    }
//
//    private String buildKey(String fst, String snd) {
//        return fst + "%#%" + snd;
//    }
//
//    private Pair<String, String> split(String key) {
//        String[] split = key.split("%#%");
//        return new Pair<>(split[0], split[1]);
//    }
//
//}
