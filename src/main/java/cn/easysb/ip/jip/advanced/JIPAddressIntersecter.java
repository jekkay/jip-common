package cn.easysb.ip.jip.advanced;

import com.google.common.collect.Lists;
import cn.easysb.ip.jip.*;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import cn.easysb.ip.jip.iprange.JIPv4AddressRange;
import cn.easysb.ip.jip.iprange.JIPv6AddressRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 实现集合的Retainer功能，就是交集
 * intersect([1,2,3], [2,4]) = [2]
 * intersect([1,2],1) = null
 * intersect(1,1) = [1]
 * intersect([1, [1]) = [1]
 * intersect(1,2) = null
 * intersect([1,2], 3) = null
 * <p>
 * Created by jekkay on 2018/12/14.
 */

@Slf4j
public class JIPAddressIntersecter {
    public static List<JIPAddress> intersect(JIPAddress src, JIPAddress dst) {
        if (src == null || dst == null) {
            return null;
        }
        // 同类型的就直接比较
        return ((IPType.isIPV4(src.getIpType()) && IPType.isIPV4(dst.getIpType())) ||
                (IPType.isIPV6(src.getIpType()) && IPType.isIPV6(dst.getIpType()))) ?
                doRetainInner(src, dst) : null;
    }

    public static List<JIPAddress> intersect(JIPAddress src, List<JIPAddress> dst) {
        if (src == null || !CollectionUtils.isNotEmpty(dst)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        JIPAddressSet treeSet = JIPAddressUtils.buildAddressSet(dst);
        while (CollectionUtils.isNotEmpty(treeSet)) {
            JIPAddress find = treeSet.findIp(src);
            if (find == null) {
                break;
            }
            List<JIPAddress> tmpList = intersect(src, find);
            if (CollectionUtils.isNotEmpty(tmpList)) {
                // 特殊优化
                if (tmpList.size() == 1 && tmpList.get(0).equals(src)) {
                    addressList.clear();
                    addressList.add(src);
                    break;
                } else {
                    addressList.addAll(tmpList);
                }
            }

            treeSet.deleteIp(find);
        }
        if (CollectionUtils.isNotEmpty(treeSet)) {
            treeSet.clear();
        }

        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressCombiner.combine(addressList) : null;
    }

    public static List<JIPAddress> intersect(List<JIPAddress> src, JIPAddress dst) {
        return intersect(dst, src);
    }

    public static List<JIPAddress> intersect(List<JIPAddress> src, List<JIPAddress> dst) {
        if (!CollectionUtils.isNotEmpty(src) || !CollectionUtils.isNotEmpty(dst)) {
            return null;
        }
        List<JIPAddress> result = Lists.newArrayList();
        for (JIPAddress address1 : src) {
            List<JIPAddress> tmpList = Lists.newLinkedList();

            // 过滤一遍
            for (JIPAddress address2 : dst) {
                if (JIPAddressComparator.hasIntersection(address1, address2)) {
                    tmpList.add(address2);
                }
            }
            if (!CollectionUtils.isNotEmpty(tmpList)) {
                continue;
            }

            List<JIPAddress> tmpResult = intersect(address1, tmpList);
            if (CollectionUtils.isNotEmpty(tmpResult)) {
                result.addAll(tmpResult);
            }
        }

        return CollectionUtils.isNotEmpty(result) ? JIPAddressCombiner.combine(result) : null;
    }

    // IPv4和IPv6还是分开处理吧，逻辑简单点，避免混乱
    private static List<JIPAddress> doRetainInner(JIPAddress o1, JIPAddress o2) {
        switch (o1.getIpType()) {
            case IPV4:
                switch (o2.getIpType()) {
                    case IPV4:
                        return intersectIPv4((JIPv4Address) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return intersectIPv4((JIPv4Address) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV4_SEGMENT:
            case IPV4_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV4:
                        return intersectIPv4((JIPv4AddressRange) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return intersectIPv4((JIPv4AddressRange) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV6:
                switch (o2.getIpType()) {
                    case IPV6:
                        return intersectIPv6((JIPv6Address) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return intersectIPv6((JIPv6Address) o1, (JIPv6AddressRange) o2);
                }
                break;
            case IPV6_SEGMENT:
            case IPV6_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV6:
                        return intersectIPv6((JIPv6AddressRange) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return intersectIPv6((JIPv6AddressRange) o1, (JIPv6AddressRange) o2);
                }
                break;
        }
        log.error(String.format("doRetainInner: YOU SHOULD NOT SEE THIS LOG, %s intersect %s",
                o1.toString(), o2.toString()));
        return null;
    }

    private static List<JIPAddress> intersectIPv4(JIPv4Address src, JIPv4Address dst) {
        if (!src.equals(dst)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }


    private static List<JIPAddress> intersectIPv4(JIPv4Address src, JIPv4AddressRange dst) {
        if (!dst.contains(src)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    private static List<JIPAddress> intersectIPv4(JIPv4AddressRange src, JIPv4Address dst) {
        return intersectIPv4(dst, src);
    }

    private static List<JIPAddress> intersectIPv4(JIPv4AddressRange src, JIPv4AddressRange dst) {
        if (!JIPAddressComparator.hasIntersection(src, dst)) {
            return null;
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        if (src.equals(dst)) {
            addressList.add(src);
            return addressList;
        }

        JIPv4Address start = src.getStart().compareTo(dst.getStart()) > 0 ? src.getStart() : dst.getStart();
        JIPv4Address end = src.getEnd().compareTo(dst.getEnd()) > 0 ? dst.getEnd() : src.getEnd();

        if (start.equals(end)) {
            // 简单复制一份
            addressList.add(JIPAddressUtils.toIpObject(start.toString()));
        } else {
            addressList.add(JIPAddressUtils.toIpObject(String.format("%s-%s", start.toString(), end.toString())));
        }
        return addressList;
    }

    private static List<JIPAddress> intersectIPv6(JIPv6Address src, JIPv6Address dst) {
        if (!src.equals(dst)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    private static List<JIPAddress> intersectIPv6(JIPv6Address src, JIPv6AddressRange dst) {
        if (!dst.contains(src)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(src);
        return addressList;
    }

    private static List<JIPAddress> intersectIPv6(JIPv6AddressRange src, JIPv6Address dst) {
        return intersectIPv6(dst, src);
    }

    private static List<JIPAddress> intersectIPv6(JIPv6AddressRange src, JIPv6AddressRange dst) {
        if (!JIPAddressComparator.hasIntersection(src, dst)) {
            return null;
        }

        List<JIPAddress> addressList = Lists.newArrayList();
        if (src.equals(dst)) {
            addressList.add(src);
            return addressList;
        }

        JIPv6Address start = src.getStart().compareTo(dst.getStart()) > 0 ? src.getStart() : dst.getStart();
        JIPv6Address end = src.getEnd().compareTo(dst.getEnd()) > 0 ? dst.getEnd() : src.getEnd();


        if (start.equals(end)) {
            // 简单复制一份
            addressList.add(JIPAddressUtils.toIpObject(start.toString()));
        } else {
            addressList.add(JIPAddressUtils.toIpObject(String.format("%s-%s", start.toString(), end.toString())));
        }
        return addressList;
    }
}
