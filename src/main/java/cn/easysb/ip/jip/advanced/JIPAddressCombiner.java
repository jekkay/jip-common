package cn.easysb.ip.jip.advanced;

import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import cn.easysb.ip.jip.iprange.JIPv4AddressRange;
import cn.easysb.ip.jip.iprange.JIPv6AddressRange;
import com.google.common.collect.Lists;
import com.googlecode.ipv6.IPv6Address;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 将IP聚合输出
 * 比如 1-3, 2, 4-6 ==> 1-6
 * Created by hzhujiankang on 2018/12/14.
 */
@Slf4j
public class JIPAddressCombiner {
    /**
     * 将IP聚合输出
     * 比如 1-3, 2, 4-6 ==> 1-6
     */
    public static List<JIPAddress> combine(List<JIPAddress> jipAddressList) {
        if (!CollectionUtils.isNotEmpty(jipAddressList) || jipAddressList.size() <= 1) {
            return jipAddressList;
        }
        Collections.sort(jipAddressList, new Comparator<JIPAddress>() {
            @Override
            public int compare(JIPAddress o1, JIPAddress o2) {
                return JIPAddressComparator.compareTo(o1, o2);
            }
        });

        List<JIPAddress> result = Lists.newArrayList();
        int size = jipAddressList.size();
        JIPAddress curAddress = jipAddressList.get(0);
        JIPAddress nextAddress = null;
        for (int i = 1; i < size; i++) {
            nextAddress = jipAddressList.get(i);

            List<JIPAddress> tmpList = combine(curAddress, nextAddress);
            if (!CollectionUtils.isNotEmpty(tmpList) || tmpList.size() != 1) {
                // log.debug(String.format(" %s can not be combined with %s.",
                //        curAddress.toString(), nextAddress.toString()));
                result.add(curAddress);
                curAddress = nextAddress;
            } else {
                curAddress = tmpList.get(0);
            }
        }
        result.add(curAddress);
        return result;
    }

    public static List<JIPAddress> combine(JIPAddress jipAddress1, JIPAddress jipAddress2) {
        boolean hasNull = false;
        if (jipAddress1 == null || jipAddress2 == null) {
            hasNull = true;
        }
        if (!hasNull) {
            // 同类型的就直接比较
            if ((IPType.isIPV4(jipAddress1.getIpType()) && IPType.isIPV4(jipAddress2.getIpType())) ||
                    (IPType.isIPV6(jipAddress1.getIpType()) && IPType.isIPV6(jipAddress2.getIpType()))) {
                return doCombineInner(jipAddress1, jipAddress2);
            }
        }
        List<JIPAddress> addressList = Lists.newArrayList();
        if (jipAddress1 != null) {
            addressList.add(jipAddress1);
        }
        if (jipAddress2 != null) {
            addressList.add(jipAddress2);
        }
        return addressList;
    }

    // IPv4和IPv6还是分开处理吧，逻辑简单点，避免混乱
    private static List<JIPAddress> doCombineInner(JIPAddress o1, JIPAddress o2) {
        switch (o1.getIpType()) {
            case IPV4:
                switch (o2.getIpType()) {
                    case IPV4:
                        return combineIPv4((JIPv4Address) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return combineIPv4((JIPv4Address) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV4_SEGMENT:
            case IPV4_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV4:
                        return combineIPv4((JIPv4AddressRange) o1, (JIPv4Address) o2);
                    case IPV4_SEGMENT:
                    case IPV4_SEGMENT_WITH_MASK:
                        return combineIPv4((JIPv4AddressRange) o1, (JIPv4AddressRange) o2);
                }
                break;
            case IPV6:
                switch (o2.getIpType()) {
                    case IPV6:
                        return combineIPv6((JIPv6Address) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return combineIPv6((JIPv6Address) o1, (JIPv6AddressRange) o2);
                }
                break;
            case IPV6_SEGMENT:
            case IPV6_SEGMENT_WITH_MASK:
                switch (o2.getIpType()) {
                    case IPV6:
                        return combineIPv6((JIPv6AddressRange) o1, (JIPv6Address) o2);
                    case IPV6_SEGMENT:
                    case IPV6_SEGMENT_WITH_MASK:
                        return combineIPv6((JIPv6AddressRange) o1, (JIPv6AddressRange) o2);
                }
                break;
        }
        log.error(String.format("doCombineInner: YOU SHOULD NOT SEE THIS LOG, %s combine %s",
                o1.toString(), o2.toString()));
        return null;
    }

    private static List<JIPAddress> combineIPv4(JIPv4Address o1, JIPv4Address o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (o1.equals(o2)) {
            addressList.add(o1);
        } else if (isNeighbor(o1, o2)) {
            String ipRangeText;
            if (o1.compareTo(o2) > 0) {
                ipRangeText = String.format("%s-%s", o2.toString(), o1.toString());
            } else {
                ipRangeText = String.format("%s-%s", o1.toString(), o2.toString());
            }
            addressList.add(new JIPv4AddressRange(ipRangeText));
        } else {
            addressList.add(o1);
            addressList.add(o2);
        }
        return addressList;
    }

    private static boolean isNeighbor(JIPv4Address o1, JIPv4Address o2) {
        return Math.abs(o1.getIpAddress() - o2.getIpAddress()) == 1;
    }

    private static List<JIPAddress> combineIPv4(JIPv4AddressRange o1, JIPv4Address o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (o1.contains(o2)) {
            addressList.add(o1);
        } else {
            JIPv4Address start = o1.getStart();
            JIPv4Address end = o1.getEnd();
            if (isNeighbor(start, o2) || isNeighbor(end, o2)) {
                String ipRangeText;
                if (isNeighbor(start, o2)) {
                    ipRangeText = String.format("%s-%s", o2.toString(), end.toString());
                } else {
                    ipRangeText = String.format("%s-%s", start.toString(), o2.toString());
                }
                addressList.add(new JIPv4AddressRange(ipRangeText));
            } else {
                addressList.add(o1);
                addressList.add(o2);
            }
        }
        return addressList;
    }

    private static List<JIPAddress> combineIPv4(JIPv4Address o1, JIPv4AddressRange o2) {
        return combineIPv4(o2, o1);
    }

    private static List<JIPAddress> combineIPv4(JIPv4AddressRange o1, JIPv4AddressRange o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        boolean flag = JIPAddressComparator.hasIntersection(o1, o2)
                || isNeighbor(o1.getStart(), o2.getEnd())
                || isNeighbor(o1.getEnd(), o2.getStart());
        if (flag) {
            long min = Math.min(o1.getStart().getIpAddress(), o2.getStart().getIpAddress());
            long max = Math.max(o1.getEnd().getIpAddress(), o2.getEnd().getIpAddress());
            addressList.add(new JIPv4AddressRange(String.format("%s-%s",
                    JIPv4Address.convertString(min),
                    JIPv4Address.convertString(max))));
        } else {
            addressList.add(o1);
            addressList.add(o2);
        }
        return addressList;
    }

    private static boolean isNeighbor(JIPv6Address o1, JIPv6Address o2) {
        IPv6Address inner1 = o1.getAddress();
        IPv6Address inner2 = o2.getAddress();
        return inner1.getHighBits() == inner2.getHighBits() &&
                Math.abs(inner1.getLowBits() - inner2.getLowBits()) == 1;
    }

    private static List<JIPAddress> combineIPv6(JIPv6Address o1, JIPv6Address o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (o1.equals(o2)) {
            addressList.add(o1);
        } else if (isNeighbor(o1, o2)) {
            String ipRangeText;
            if (o1.compareTo(o2) > 0) {
                ipRangeText = String.format("%s-%s", o2.toString(), o1.toString());
            } else {
                ipRangeText = String.format("%s-%s", o1.toString(), o2.toString());
            }
            addressList.add(new JIPv6AddressRange(ipRangeText));
        } else {
            addressList.add(o1);
            addressList.add(o2);
        }
        return addressList;
    }

    private static List<JIPAddress> combineIPv6(JIPv6AddressRange o1, JIPv6Address o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (o1.contains(o2)) {
            addressList.add(o1);
        } else {
            JIPv6Address start = o1.getStart();
            JIPv6Address end = o1.getEnd();
            if (isNeighbor(start, o2) || isNeighbor(end, o2)) {
                String ipRangeText;
                if (isNeighbor(start, o2)) {
                    ipRangeText = String.format("%s-%s", o2.toString(), end.toString());
                } else {
                    ipRangeText = String.format("%s-%s", start.toString(), o2.toString());
                }
                addressList.add(new JIPv6AddressRange(ipRangeText));
            } else {
                addressList.add(o1);
                addressList.add(o2);
            }
        }
        return addressList;
    }

    private static List<JIPAddress> combineIPv6(JIPv6Address o1, JIPv6AddressRange o2) {
        return combineIPv6(o2, o1);
    }

    private static List<JIPAddress> combineIPv6(JIPv6AddressRange o1, JIPv6AddressRange o2) {
        List<JIPAddress> addressList = Lists.newArrayList();
        boolean flag = JIPAddressComparator.hasIntersection(o1, o2)
                || isNeighbor(o1.getStart(), o2.getEnd())
                || isNeighbor(o1.getEnd(), o2.getStart());
        if (flag) {
            JIPv6Address newStart = o1.getStart().compareTo(o2.getStart()) > 0 ?
                    o2.getStart() : o1.getStart();
            JIPAddress newEnd = o1.getEnd().compareTo(o2.getEnd()) > 0 ?
                    o1.getEnd() : o2.getEnd();
            addressList.add(new JIPv6AddressRange(String.format("%s-%s",
                    newStart.toString(), newEnd.toString())));
        } else {
            addressList.add(o1);
            addressList.add(o2);
        }
        return addressList;
    }
}
