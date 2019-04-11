package cn.easysb.ip.jip;


/**
 * IP地址范围
 * Created by jekkay on 2018/11/29.
 */

public interface JIPAddressRange extends JIPAddress {
    // 开始ip
    JIPAddress getStart();

    // 结束ip
    JIPAddress getEnd();

    // 是否包含ip
    boolean contains(JIPAddress ipAddress);

    // 尝试转化成带mask的形式
    boolean tryConvertMask();
}
