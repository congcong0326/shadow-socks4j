package org.congcong.multi.proxy.monitor.connect;

import org.congcong.multi.proxy.entity.AttackLog;

public interface IConnectionReport {

    void reportAccessIp(String ip);

    void reportSuspiciousIp(String ip);

    void reportAttackLog(AttackLog attackLog);

}
