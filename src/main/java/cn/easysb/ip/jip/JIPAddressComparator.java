package cn.easysb.ip.jip;

import com.googlecode.ipv6.IPv6AddressRange;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import cn.easysb.ip.jip.iprange.JIPv4AddressRange;
import cn.easysb.ip.jip.iprange.JIPv6AddressRange;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by hzhujiankang on 2018/11/29.
 */
@Slf4j
public class JIPAddressComparator {
    // IPV4排在前面
    public static int compareTo(JIPAddress o1, JIPAddress o2) {
        // 空值判断
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        // 同类型的就直接比较
        if ((IPType.isIPV4(o1.getIpType()) && IPType.isIPV4(o2.getIpType())) ||
                (IPType.isIPV6(o1.getIpType()) && IPType.isIPV6(o2.getIpType()))) {
            return compareInner(o1, o2);
        }
        // 如果是ipv6就大一点，放后面，ipv4就小一点，放前面
        if (IPType.isIPV6(o1.getIpType())) {
            return 1;
        }
        if (IPType.isIPV6(o2.getIpType())) {
            return -1;
        }
        // 如果是ipv4 和 未知类型，就比较类型值大小就好
        return (o1.getIpType() != null ? o1.getIpType().getCode() : -1)
                - (o2 != null ? o2.getIpType().getCode() : -1);
    }

    private static int compareInner(JIPAddress o1, JIPAddress o2) {
        IPType ipType1 = o1.getIpType();
        IPType ipType2 = o2.getIpType();
        int result = 0;
        switch (ipType1) {
            case IPV4:
                if (IPType.IPV4.equals(ipType2)) {
                    result = doCompareInner((JIPv4Address) o1, (JIPv4Address) o2);
                    break;
                }
                if (IPType.IPV4_SEGMENT.equals(ipType2) || IPType.IPV4_SEGMENT_WITH_MASK.equals(ipType2)) {
                    result = doCompareInner((JIPv4Address) o1, (JIPv4AddressRange) o2);
                    break;
                }
                log.error("SHOULD NOT SEE THIS LOG IPV4");
                break;
            case IPV4_SEGMENT:
            case IPV4_SEGMENT_WITH_MASK:
                if (IPType.IPV4.equals(ipType2)) {
                    result = doCompareInner((JIPv4AddressRange) o1, (JIPv4Address) o2);
                    break;
                }
                if (IPType.IPV4_SEGMENT.equals(ipType2) || IPType.IPV4_SEGMENT_WITH_MASK.equals(ipType2)) {
                    result = doCompareInner((JIPv4AddressRange) o1, (JIPv4AddressRange) o2);
                    break;
                }
                log.error("SHOULD NOT SEE THIS LOG IPV4 SEGMENT");
                break;
            case IPV6:
                if (IPType.IPV6.equals(ipType2)) {
                    result = doCompareInner((JIPv6Address) o1, (JIPv6Address) o2);
                    break;
                }
                if (IPType.IPV6_SEGMENT.equals(ipType2) || IPType.IPV6_SEGMENT_WITH_MASK.equals(ipType2)) {
                    result = doCompareInner((JIPv6Address) o1, (JIPv6AddressRange) o2);
                    break;
                }
                log.error("SHOULD NOT SEE THIS LOG IPV6");
                break;
            case IPV6_SEGMENT:
            case IPV6_SEGMENT_WITH_MASK:
                if (IPType.IPV6.equals(ipType2)) {
                    result = doCompareInner((JIPv6AddressRange) o1, (JIPv6Address) o2);
                    break;
                }
                if (IPType.IPV6_SEGMENT.equals(ipType2) || IPType.IPV6_SEGMENT_WITH_MASK.equals(ipType2)) {
                    result = doCompareInner((JIPv6AddressRange) o1, (JIPv6AddressRange) o2);
                    break;
                }
                log.error("SHOULD NOT SEE THIS LOG IPV6 SEGMENT");
                break;
        }
        return result;

    }

    private static int doCompareInner(JIPv4Address o1, JIPv4Address o2) {
        // 都是long，转换下
        if (o1.getIpAddress() != o2.getIpAddress()) {
            return o1.getIpAddress() - o2.getIpAddress() > 0 ? 1 : -1;
        }
        return 0;
    }

    private static int doCompareInner(JIPv4AddressRange o1, JIPv4Address o2) {
        JIPv4Address start = o1.getStart();
        int result = start.compareTo(o2);
        if (result == 0) {
            // 如果是起始相同的话，那么单个ip的权重要小一点放前面,而区间放后面
            result = 1;
        }
        return result;
    }

    private static int doCompareInner(JIPv4Address o1, JIPv4AddressRange o2) {
        return -doCompareInner(o2, o1);
    }

    private static int doCompareInner(JIPv4AddressRange o1, JIPv4AddressRange o2) {
        int result = o1.getStart().compareTo(o2.getStart());
        if (result == 0) {
            result = o1.getEnd().compareTo(o2.getEnd());
        }
        return result;
    }

    private static int doCompareInner(JIPv6Address o1, JIPv6Address o2) {
        if (o1.getAddress() == null || o2.getAddress() == null) {
            if (o1.getAddress() == o2.getAddress()) {
                return 0;
            }
            return o1.getAddress() != null ? 1 : -1;
        }
        return o1.getAddress().compareTo(o2.getAddress());
    }

    private static int doCompareInner(JIPv6AddressRange o1, JIPv6Address o2) {
        int result = doCompareInner(o1.getStart(), o2);
        if (result == 0) {
            return 1;
        }
        return result;
    }

    private static int doCompareInner(JIPv6Address o1, JIPv6AddressRange o2) {
        return -doCompareInner(o2, o1);
    }

    private static int doCompareInner(JIPv6AddressRange o1, JIPv6AddressRange o2) {
        IPv6AddressRange rang1 = o1.getParsedRange();
        IPv6AddressRange rang2 = o2.getParsedRange();
        if (rang1 == null || rang2 == null) {
            if (rang1 == rang2) {
                return 0;
            }
            return rang1 != null ? 1 : -1;
        }
        return rang1.compareTo(rang2);
    }


    /**
     * 是否存在交集
     */
    public static boolean hasIntersection(JIPAddress o1, JIPAddress o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        // 同类型的就直接比较
        if ((IPType.isIPV4(o1.getIpType()) && IPType.isIPV4(o2.getIpType())) ||
                (IPType.isIPV6(o1.getIpType()) && IPType.isIPV6(o2.getIpType()))) {
            return hasIntersectionInner(o1, o2);
        }
        // 其他就直接忽略
        return false;
    }

    private static boolean hasIntersectionInner(JIPAddress o1, JIPAddress o2) {
        if (IPType.isIPSegment(o1.getIpType())) {
            return ((JIPAddressRange) o1).contains(o2);
        }
        if (IPType.isIPSegment(o2.getIpType())) {
            return ((JIPAddressRange) o2).contains(o1);
        }
        return o1.equals(o2);
    }

}
