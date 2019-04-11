package cn.easysb.ip.jip;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by jekkay on 2018/12/4.
 */
public class JIPAddressCheckUtils {
    // 检测ip地址是否有问题，碰到第一个错误就返回
    public static String checkConvertIpObject(List<JIPAddress> addressList, String ips) {
        if (StringUtils.isBlank(ips)) {
            return null;
        }
        return checkConvertIpObject(addressList, JIPAddressUtils.splitIpList(ips));
    }

    public static String checkConvertIpObject(List<JIPAddress> addressList, List<String> ips) {
        if (!CollectionUtils.isNotEmpty(ips)) {
            return null;
        }
        for (String ip : ips) {
            JIPAddress address = JIPAddressUtils.toIpObject(ip);
            if (address == null) {
                return String.format("ip格式错误 %s", ip);
            }
            addressList.add(address);
        }
        return null;
    }
}
