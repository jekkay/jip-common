package cn.easysb.ip.jip;

import java.util.Iterator;

/**
 * IP地址的接口
 * Created by hzhujiankang on 2018/11/29.
 */

public interface JIPAddress extends Comparable<JIPAddress> {
    // 返回数据类型
    IPType getIpType();

    void setIpType(IPType ipType);

    // 携带额外数据
    Object getData();

    void setData(Object data);

    // 备注 比如解析 "10.1.1.2 # IT专用" ==> 那remark将存储 "IT专用"
    String getRemark();

    void setRemark(String remark);

    // 清除数据
    void reset();

    // 格式化输出
    String toString();

    // 获取ip的数量, 目前只支持IPv4， 因为IPV6段的数量无法用long表示
    long ipCount();

    // 枚举ip
    Iterator<JIPAddress> iterator();
}
