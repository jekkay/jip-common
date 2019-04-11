package cn.easysb.ip.jip.advanced;

import com.google.common.collect.Lists;
import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.JIPAddressUtils;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import cn.easysb.ip.jip.iprange.JIPv4AddressRange;
import cn.easysb.ip.jip.iprange.JIPv6AddressRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 实现集合的差集功能
 * <p>
 * [1,2,3] - [2,4] = [1,3]
 * [1-10,20] - [3-4,20] = [1-2,5-10]
 * Created by hzhujiankang on 2018/12/14.
 */
@Slf4j
public class JIPAddressSubtracter {
    public static List<JIPAddress> subtract(JIPAddress src, JIPAddress dst) {
        if (src == null) {
            return null;
        }
        if (dst == null) {
            List<JIPAddress> addressList = Lists.newArrayList();
            addressList.add(src);
            return addressList;
        }
        // 同类型的就直接比较
        if ((IPType.isIPV4(src.getIpType()) && IPType.isIPV4(dst.getIpType())) ||
                (IPType.isIPV6(src.getIpType()) && IPType.isIPV6(dst.getIpType()))) {
            return doSubtractInner(src, dst);
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    public static List<JIPAddress> subtract(JIPAddress src, List<JIPAddress> dst) {
        if (src == null) {
            return null;
        }

        if (!CollectionUtils.isNotEmpty(dst)) {
            List<JIPAddress> addressList = Lists.newArrayList();
            addressList.add(src);
            return addressList;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        // 不停地求差集
        for (JIPAddress dstAddress : dst) {
            addressList = JIPAddressIntersecter.intersect(addressList, subtract(src, dstAddress));
            if (!CollectionUtils.isNotEmpty(addressList)) {
                break;
            }
        }
        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressCombiner.combine(addressList) : null;
    }

    public static List<JIPAddress> subtract(List<JIPAddress> src, JIPAddress dst) {
        if (dst == null) {
            return src;
        }
        if (!CollectionUtils.isNotEmpty(src)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        // 不停地求差集
        for (JIPAddress srcAddress : src) {
            List<JIPAddress> tmpList = subtract(srcAddress, dst);
            if (CollectionUtils.isNotEmpty(tmpList)) {
                addressList.addAll(tmpList);
            }
        }

        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressCombiner.combine(addressList) : null;
    }

    public static List<JIPAddress> subtract(List<JIPAddress> src, List<JIPAddress> dst) {
        if (!CollectionUtils.isNotEmpty(src)) {
            return null;
        }
        if (!CollectionUtils.isNotEmpty(dst)) {
            return src;
        }

        List<JIPAddress> result = Lists.newArrayList();
        for (JIPAddress address1 : src) {
            List<JIPAddress> tmpResult = subtract(address1, dst);
            if (CollectionUtils.isNotEmpty(tmpResult)) {
                result.addAll(tmpResult);
            }
        }

        return CollectionUtils.isNotEmpty(result) ? JIPAddressCombiner.combine(result) : null;
    }

    // IPv4和IPv6还是分开处理吧，逻辑简单点，避免混乱
    private static List<JIPAddress> doSubtractInner(JIPAddress o1, JIPAddress o2) {
        if (!JIPAddressComparator.hasIntersection(o1, o2)) {
            List<JIPAddress> addressList = Lists.newArrayList();
            addressList.add(o1);
            return addressList;
        }
        switch (o1.getIpType()) {
            case IPV4:
                switch (o2.getIpType()) {
                    case IPV4:
                        return subtractIPv4((JIPv4Address) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return subtractIPv4((JIPv4Address) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV4_SEGMENT:
            case IPV4_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV4:
                        return subtractIPv4((JIPv4AddressRange) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return subtractIPv4((JIPv4AddressRange) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV6:
                switch (o2.getIpType()) {
                    case IPV6:
                        return subtractIPv6((JIPv6Address) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return subtractIPv6((JIPv6Address) o1, (JIPv6AddressRange) o2);
                }
                break;
            case IPV6_SEGMENT:
            case IPV6_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV6:
                        return subtractIPv6((JIPv6AddressRange) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return subtractIPv6((JIPv6AddressRange) o1, (JIPv6AddressRange) o2);
                }
                break;
        }
        log.error(String.format("doRetainInner: YOU SHOULD NOT SEE THIS LOG, %s subtract %s",
                o1.toString(), o2.toString()));
        return null;
    }

    private static List<JIPAddress> subtractIPv4(JIPv4Address src, JIPv4Address dst) {
        if (src.equals(dst)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);

        return addressList;
    }


    private static List<JIPAddress> subtractIPv4(JIPv4Address src, JIPv4AddressRange dst) {
        if (dst.contains(src)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);

        return addressList;
    }

    private static List<JIPAddress> subtractIPv4(JIPv4AddressRange src, JIPv4Address dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (!src.contains(dst)) {
            addressList.add(src);

            return addressList;
        }

        if (src.getStart().compareTo(dst) < 0) {
            JIPAddress dstLeft = buildIpAddress(src.getStart(),
                    JIPAddressUtils.toIpObject(dst.getIpAddress() - 1));
            if (dstLeft != null) {
                addressList.add(dstLeft);
            }
        }
        if (src.getEnd().compareTo(dst) > 0) {
            JIPAddress dstRight = buildIpAddress(JIPAddressUtils.toIpObject(dst.getIpAddress() + 1),
                    src.getEnd());
            if (dstRight != null) {
                addressList.add(dstRight);
            }
        }

        return addressList;
    }

    private static List<JIPAddress> subtractIPv4(JIPv4AddressRange src, JIPv4AddressRange dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        JIPv4Address minStart = src.getStart().compareTo(dst.getStart()) > 0 ? dst.getStart() : src.getStart();
        JIPv4Address maxEnd = src.getEnd().compareTo(dst.getEnd()) > 0 ? src.getEnd() : dst.getEnd();

        if (minStart.compareTo(dst.getStart()) < 0) {
            JIPAddress dstLeft = buildIpAddress(minStart,
                    JIPAddressUtils.toIpObject(dst.getStart().getIpAddress() - 1));
            if (dstLeft != null) {
                addressList.add(dstLeft);
            }
        }

        if (maxEnd.compareTo(dst.getEnd()) > 0) {
            JIPAddress dstRight = buildIpAddress(JIPAddressUtils.toIpObject(dst.getEnd().getIpAddress() + 1),
                    maxEnd);
            if (dstRight != null) {
                addressList.add(dstRight);
            }
        }
        return addressList;
    }

    private static JIPAddress buildIpAddress(JIPAddress start, JIPAddress end) {
        if (start == null || end == null) {
            return null;
        }
        int cmp = start.compareTo(end);
        JIPAddress result = null;
        if (cmp == 0) {
            result = JIPAddressUtils.toIpObject(start.toString());
        } else if (cmp < 0) {
            result = JIPAddressUtils.toIpObject(String.format("%s-%s",
                    start.toString(), end.toString()));
        }
        return result;
    }

    private static List<JIPAddress> subtractIPv6(JIPv6Address src, JIPv6Address dst) {
        if (src.equals(dst)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    private static List<JIPAddress> subtractIPv6(JIPv6Address src, JIPv6AddressRange dst) {
        if (dst.contains(src)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    private static List<JIPAddress> subtractIPv6(JIPv6AddressRange src, JIPv6Address dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (!src.contains(dst)) {
            addressList.add(src);

            return addressList;
        }

        if (src.getStart().compareTo(dst) < 0) {
            JIPAddress dstLeft = buildIpAddress(src.getStart(), JIPAddressUtils.toIpObject(dst.getAddress().add(-1).toString()));
            if (dstLeft != null) {
                addressList.add(dstLeft);
            }
        }

        if (src.getEnd().compareTo(dst) > 0) {
            JIPAddress dstRight = buildIpAddress(JIPAddressUtils.toIpObject(dst.getAddress().add(1).toString()),
                    src.getEnd());
            if (dstRight != null) {
                addressList.add(dstRight);
            }
        }

        return addressList;
    }

    private static List<JIPAddress> subtractIPv6(JIPv6AddressRange src, JIPv6AddressRange dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        JIPv6Address minStart = src.getStart().compareTo(dst.getStart()) > 0 ? dst.getStart() : src.getStart();
        JIPv6Address maxEnd = src.getEnd().compareTo(dst.getEnd()) > 0 ? src.getEnd() : dst.getEnd();

        if (minStart.compareTo(dst.getStart()) < 0) {
            JIPAddress dstLeft = buildIpAddress(minStart,
                    JIPAddressUtils.toIpObject(dst.getStart().getAddress().add(-1).toString()));
            if (dstLeft != null) {
                addressList.add(dstLeft);
            }
        }

        if (maxEnd.compareTo(dst.getEnd()) > 0) {
            JIPAddress dstRight = buildIpAddress(
                    JIPAddressUtils.toIpObject(dst.getEnd().getAddress().add(1).toString()),
                    maxEnd);
            if (dstRight != null) {
                addressList.add(dstRight);
            }
        }
        return addressList;
    }
}
