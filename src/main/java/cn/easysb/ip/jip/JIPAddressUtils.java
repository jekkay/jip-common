package cn.easysb.ip.jip;

import com.google.common.collect.Lists;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import cn.easysb.ip.jip.iprange.JIPv4AddressRange;
import cn.easysb.ip.jip.iprange.JIPv6AddressRange;
import cn.easysb.ip.jip.ipset.JIPAddressRBTreeSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jekkay on 2018/11/22.
 */
@Slf4j
public class JIPAddressUtils {
    final private static String[] SEPARATORS = {
            "\r\n", "\r", "\n", ",", "，", ";"
    };

    public static boolean isIpV4(String ip) {
        return IPType.isIPV4(checkIpType(ip));
    }

    public static boolean isIpV6(String ip) {
        return IPType.isIPV6(checkIpType(ip));
    }

    public static boolean isValidIPAddress(String ip) {
        return toIpObject(ip) != null;
    }

    public static IPType checkIpType(String ip) {
        JIPAddress address = toIpObject(ip);
        return address != null ? address.getIpType() : IPType.UNKNOWN;
    }

    public static JIPv4Address toIpObject(long ipv4) {
        try {
            return new JIPv4Address(ipv4);
        } catch (Exception e) {
            //log.debug(e);
        }
        return null;
    }

    public static JIPAddress toIpObject(String ip) {
        ip = StringUtils.trim(ip);
        if (StringUtils.isBlank(ip) || ip.startsWith("#")) {
            return null;
        }
        JIPAddress result = null;
        // ipv6转换
        if (ip.indexOf(":") >= 0) {
            if (containSegmentChar(ip)) {
                result = toIPv6AddressRange(ip);
            } else {
                result = toIpv6Address(ip);
            }
        } else if (ip.indexOf(".") >= 0) { // ipv4转换
            if (containSegmentChar(ip)) {
                result = toIPv4AddressRange(ip);
            } else {
                result = toIpv4Address(ip);
            }
        }
        return result;
    }

    private static boolean containSegmentChar(String ip) {
        int index = ip.indexOf("#");
        if (index >= 0) {
            ip = ip.substring(0, index);
        }
        return StringUtils.contains(ip, "-") || StringUtils.contains(ip, "/");
    }

    public static JIPv4Address toIpv4Address(String ip) {
        try {
            return new JIPv4Address(ip);
        } catch (Exception e) {
            // log.debug("fail to ipv4 address", e);
        }
        return null;
    }

    public static JIPv4AddressRange toIPv4AddressRange(String ip) {
        try {
            return new JIPv4AddressRange(ip);
        } catch (Exception e) {
            // log.debug("fail to ipv4 address range", e);
        }
        return null;
    }

    public static JIPv6Address toIpv6Address(String ip) {
        try {
            return new JIPv6Address(ip);
        } catch (Exception e) {
            // log.debug("fail to ipv6 address", e);
        }
        return null;
    }

    public static JIPv6AddressRange toIPv6AddressRange(String ip) {
        try {
            return new JIPv6AddressRange(ip);
        } catch (Exception e) {
            // log.debug("fail to ipv6 address range", e);
        }
        return null;
    }

    public static JIPAddressSet buildAddressSet(String ipList) {
        return buildAddressSet(ipList, null);
    }

    public static JIPAddressSet buildAddressSet(String ipList, Object data) {
        if (StringUtils.isBlank(ipList)) {
            return null;
        }

        JIPAddressSet treeSet = buildEmptyAddressSet();
        return addIpList(treeSet, ipList, data);
    }

    public static JIPAddressSet buildEmptyAddressSet() {
        return new JIPAddressRBTreeSet();
    }

    public static JIPAddressSet addIpList(JIPAddressSet addressSet, String ipList, Object data) {
        List<String> ips = splitIpList(ipList);
        if (!CollectionUtils.isNotEmpty(ips)) {
            return addressSet;
        }
        return addIpList(addressSet, ips, data);
    }

    public static JIPAddressSet addIpList(JIPAddressSet addressSet, List<String> ipList, Object data) {
        if (!CollectionUtils.isNotEmpty(ipList)) {
            return addressSet;
        }
        for (String ip : ipList) {
            ip = StringUtils.trim(ip);
            if (StringUtils.isBlank(ip) || ip.startsWith("#")) {
                continue;
            }
            JIPAddress address = toIpObject(StringUtils.trim(ip));
            if (address == null) {
                log.error(String.format("parse ip %s fail.", ip));
                continue;
            }
            // 设置外带数据
            address.setData(data);
            // 插入树中
            addressSet.insertIp(address);
        }
        return addressSet;
    }

    public static JIPAddressSet buildAddressSet(List<JIPAddress> addressList) {
        JIPAddressSet addressSet = buildEmptyAddressSet();
        if (CollectionUtils.isNotEmpty(addressList)) {
            for (JIPAddress address : addressList) {
                addressSet.insertIp(address);
            }
        }
        return addressSet;
    }

    // 将IP字符串拆分成列表
    public static List<String> splitIpList(String ipList) {
        ipList = StringUtils.trim(ipList);
        if (StringUtils.isBlank(ipList)) {
            return null;
        }
        String placeholder = "\n";
        for (String s : SEPARATORS) {
            if (StringUtils.equals(s, placeholder)) {
                continue;
            }
            ipList = ipList.replace(s, placeholder);
        }
        return splitItemList(ipList, placeholder);
    }

    public static List<String> splitItemList(String lines, String separator) {
        if (StringUtils.isBlank(lines)) {
            return null;
        }
        String[] tmpList = lines.split(separator);
        List<String> result = Lists.newArrayListWithCapacity(tmpList.length);
        for (String tmp : tmpList) {
            tmp = StringUtils.trim(tmp);
            if (StringUtils.isBlank(tmp) || StringUtils.startsWith(tmp, "#")) {
                continue;
            }
            result.add(tmp);
        }
        return result;
    }

    public static List<JIPAddress> convertIpObjectList(String ipList) {
        List<String> ips = splitIpList(ipList);
        if (!CollectionUtils.isNotEmpty(ips)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        for (String ip : ips) {
            JIPAddress address = toIpObject(ip);
            if (address != null) {
                addressList.add(address);
            }
        }
        return addressList;
    }

    public static List<String> formatIpList(String ipList) {
        List<JIPAddress> addressList = JIPAddressUtils.convertIpObjectList(ipList);
        List<String> tmpList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(addressList)) {
            return tmpList;
        }
        for (JIPAddress jipAddress : addressList) {
            tmpList.add(jipAddress.toString());
        }
        return tmpList;
    }

    public static List<String> formatIpList(String ipList, String separator) {
        if (StringUtils.isBlank(ipList)) {
            return null;
        }
        return formatIpList(ipList.replace(separator, "\n"));
    }

    //  获取ip的数量
    public static long calculateIpCount(List<JIPAddress> addressList) {
        long count = 0;
        if (!CollectionUtils.isNotEmpty(addressList)) {
            return count;
        }
        for (JIPAddress address : addressList) {
            count += address.ipCount();
        }
        return count;
    }

    //  尽量转化成ip掩码的形式
    public static List<JIPAddress> tryConvertMask(List<JIPAddress> addressList) {
        if (!CollectionUtils.isNotEmpty(addressList)) {
            return addressList;
        }
        for (JIPAddress jipAddress : addressList) {
            tryConvertMask(jipAddress);
        }
        return addressList;
    }

    public static JIPAddress tryConvertMask(JIPAddress address) {
        if (address == null) {
            return null;
        }
        if (address instanceof JIPAddressRange) {
            ((JIPAddressRange) address).tryConvertMask();
        }
        return address;
    }

    public static List<String> toIpStringList(List<JIPAddress> addressList, IPFormatType ipFormatType) {
        if (CollectionUtils.isEmpty(addressList)) {
            return null;
        }
        List<String> ipList = Lists.newArrayList();
        for (JIPAddress address : addressList) {
            ipList.add(toIpString(address, ipFormatType));
        }
        return ipList;
    }

    public static String toIpListString(List<JIPAddress> addressList, IPFormatType ipFormatType) {
        List<String> ipList = toIpStringList(addressList, ipFormatType);
        return CollectionUtils.isNotEmpty(ipList) ? StringUtils.join(ipList, "\n") : "";
    }

    public static String toIpString(JIPAddress address, IPFormatType ipFormatType) {
        String text;
        if (address instanceof JIPAddressRange) {
            switch (ipFormatType) {
                case SEGMENT_WITH_MASK_FIRST:
                    tryConvertMask(address);
                    text = address.toString();
                    break;
                case SEGMENT_SIMPLE_FIRST:
                    text = toIpObject(String.format("%s-%s",
                            ((JIPAddressRange) address).getStart().toString(),
                            ((JIPAddressRange) address).getEnd().toString())).toString();
                    break;
                case SEGMENT_FULL_FIRST:
                    text = String.format("%s-%s", ((JIPAddressRange) address).getStart().toString(),
                            ((JIPAddressRange) address).getEnd().toString());
                    break;
                default:
                    text = address.toString();
                    break;
            }
        } else {
            text = address.toString();
        }
        return text;
    }

    public static List<JIPAddress> getIpListByHostname(String host) {
        List<JIPAddress> ipList = Lists.newArrayList();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses != null && addresses.length > 0) {
                for (int i = 0; i < addresses.length; i++) {
                    JIPAddress tmp = toIpObject(addresses[i].getHostAddress());
                    if (tmp != null) {
                        ipList.add(tmp);
                    }
                }
            }
        } catch (Exception e) {
            log.error("getIpListByHostname", e.getMessage());
        }
        return ipList;
    }

    public static boolean isInternalIp(JIPAddress jipAddress) {
        if (jipAddress == null) {
            return false;
        }
        if (IPType.isIPSegment(jipAddress.getIpType())) {
            JIPAddressRange range = (JIPAddressRange) jipAddress;
            if (IPType.isIPV4(jipAddress.getIpType())) {
                return isInternalIpV4((JIPv4Address) range.getStart()) && isInternalIpV4((JIPv4Address) range.getEnd())
                        && isSameType((JIPv4Address) range.getStart(), (JIPv4Address) range.getEnd());
            } else if (IPType.isIPV6(jipAddress.getIpType())) {
                return isInternalIpV6((JIPv6Address) range.getStart()) && isInternalIpV6((JIPv6Address) range.getEnd())
                        && isSameType((JIPv6Address) range.getStart(), (JIPv6Address) range.getEnd());
            }
        } else if (IPType.isIPV4(jipAddress.getIpType())) {
            return isInternalIpV4((JIPv4Address) jipAddress);
        } else if (IPType.isIPV6(jipAddress.getIpType())) {
            return isInternalIpV6((JIPv6Address) jipAddress);
        }
        return false;
    }

    private static boolean isInternalIpV4(JIPv4Address jiPv4Address) {
        String ip = jiPv4Address.toString();
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }

        long value = jiPv4Address.getIpAddress();
        // 172.16.0.0 ~ 172.31.255.255
        if (value >= 2886729728L && value <= 2887778303L) {
            return true;
        }
        return false;
    }

    private static boolean isSameType(JIPv4Address address1, JIPv4Address address2) {
        return (address1.isClassA() && address2.isClassA())
                || (address1.isClassB() && address2.isClassB())
                || (address1.isClassC() && address2.isClassC());
    }

    private static boolean isSameType(JIPv6Address address1, JIPv6Address address2) {
        return (address1.getAddress().isSiteLocal() && address2.getAddress().isSiteLocal())
                || (address1.getAddress().isLinkLocal() && address2.getAddress().isLinkLocal());
    }

    private static boolean isInternalIpV6(JIPv6Address jiPv6Address) {
        return jiPv6Address.getAddress().isSiteLocal() || jiPv6Address.getAddress().isLinkLocal();
    }

    // 展开ip，最多maxSize个ip，0表示不限制
    public static List<JIPAddress> expandIpList(JIPAddress jipAddress, int maxSize) {
        if (jipAddress == null) {
            return null;
        }
        Iterator<JIPAddress> iter = jipAddress.iterator();
        List<JIPAddress> result = Lists.newArrayList();
        while (iter.hasNext()) {
            JIPAddress tmp = iter.next();
            if (tmp != null) {
                result.add(tmp);
                if (maxSize > 0 && result.size() >= maxSize) {
                    break;
                }
            }
        }
        return result;
    }

    // 展开ip，最多maxSize个ip，0表示不限制
    public static List<JIPAddress> expandIpList(Collection<JIPAddress> jipAddressList, int maxSize) {
        if (CollectionUtils.isEmpty(jipAddressList)) {
            return null;
        }

        List<JIPAddress> result = Lists.newArrayList();
        int leftSize = maxSize;
        for (JIPAddress jipAddress : jipAddressList) {
            List<JIPAddress> tmpList = expandIpList(jipAddress, leftSize);
            if (CollectionUtils.isEmpty(tmpList)) {
                continue;
            }
            result.addAll(tmpList);
            if (maxSize > 0) {
                leftSize -= tmpList.size();
                if (leftSize <= 0) {
                    break;
                }
            }
        }
        return result;
    }
}
