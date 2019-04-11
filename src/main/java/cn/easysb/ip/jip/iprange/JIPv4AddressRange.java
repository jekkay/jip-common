package cn.easysb.ip.jip.iprange;

import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.JIPAddressRange;
import cn.easysb.ip.jip.ip.JIPv4Address;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

import static java.lang.Integer.parseInt;

/**
 * Created by hzhujiankang on 2018/11/22.
 */
@Data
@Slf4j
public class JIPv4AddressRange implements JIPAddressRange {
    /**
     * 类型
     */
    private IPType ipType;

    /**
     * 备注，比如解析 "10.1.1.2-240 # IT专用" ==> 那remark将存储 "IT专用"
     */
    private String remark;

    /**
     * 额外带数据
     */
    private Object data;

    /**
     * 起止IP，左闭右闭[start, end]
     */
    private JIPv4Address start, end;

    /**
     * 子网掩码
     */
    private JIPv4Address subNetMask;
    private int mask;

    /**
     * 子网
     */
    private JIPv4Address subNet;

    /**
     * 原始字符串
     */
    private String rawString;

    public JIPv4AddressRange() {
        reset();
    }

    public JIPv4AddressRange(String ipRange) {
        reset();
        parseIPRange(ipRange);
    }

    public void reset() {
        ipType = IPType.IPV4_SEGMENT;
        remark = "";
        data = null;
        start = end = null;
        subNetMask = subNet = null;
        mask = 0;
        rawString = null;
    }

    /**
     * 分析IP地址，格式有4种，1.2.3.4,  1.2.3.4-12, 1.2.3.4-1.2.3.12, 1.2.3.4/32
     */
    protected boolean parseIPRange(String ipRange) {
        rawString = ipRange;
        ipRange = StringUtils.trim(ipRange);
        if (StringUtils.isBlank(ipRange)) {
            throw new IllegalArgumentException("illegal ip ranges: " + rawString);
        }
        // 提取注释
        int s = ipRange.indexOf("#");
        if (s >= 0) {
            remark = StringUtils.trim(StringUtils.substring(ipRange, s + 1));
            ipRange = StringUtils.trim(StringUtils.substring(ipRange, 0, s));
            if (StringUtils.isBlank(ipRange)) {
                throw new IllegalArgumentException("illegal ip ranges: " + rawString);
            }
        }
        s = ipRange.indexOf('-');
        if (s >= 0) {
            start = new JIPv4Address(StringUtils.trim(StringUtils.substring(ipRange, 0, s)));
            ipRange = StringUtils.trim(StringUtils.substring(ipRange, s + 1));
            if (ipRange.indexOf(".") < 0) {
                int tmpEnd = parseInt(ipRange);
                if (tmpEnd < 0 || tmpEnd > 255) {
                    throw new IllegalArgumentException("illegal ip ranges: " + rawString);
                }
                end = new JIPv4Address((start.getIpAddress() & 0x0FFFFFF00) + tmpEnd);
            } else {
                end = new JIPv4Address(ipRange);
            }

            return true;
        }

        s = ipRange.indexOf("/");
        if (s > 0) {
            int prefix = parseInt(StringUtils.trim(StringUtils.substring(ipRange, s + 1)));
            if (prefix < 0 || prefix > 32) {
                throw new IllegalArgumentException("illegal arguments");
            }
            // 带有子网掩码
            mask = prefix;
            ipType = IPType.IPV4_SEGMENT_WITH_MASK;
            // 子网掩码
            subNetMask = computeMaskFromNetworkPrefix(prefix);
            JIPv4Address tmp = new JIPv4Address(StringUtils.trim(StringUtils.substring(ipRange, 0, s)));
            // 子网
            subNet = new JIPv4Address(subNetMask.getIpAddress() & tmp.getIpAddress());
            // 设置起止地址
            start = new JIPv4Address(subNet.getIpAddress());
            end = new JIPv4Address(subNet.getIpAddress() + (1L << (32 - prefix)) - 1);
            return true;
        }

        start = new JIPv4Address(ipRange);
        end = new JIPv4Address(ipRange);

        return true;
    }

    protected JIPv4Address computeMaskFromNetworkPrefix(int prefix) {
        StringBuilder str = new StringBuilder();

        for (int decimalString = 0; decimalString < 32; ++decimalString) {
            if (decimalString < prefix) {
                str.append("1");
            } else {
                str.append("0");
            }
        }

        return new JIPv4Address(toDecimalString(str.toString()));
    }

    protected static String toDecimalString(String inBinaryIpAddress) {
        StringBuilder decimalIp = new StringBuilder();

        for (int c = 0; c < 4; ++c) {
            String binary = inBinaryIpAddress.substring(c * 8, c * 8 + 8);
            int octet = parseInt(binary, 2);
            decimalIp.append(octet);
            if (c < 3) {
                decimalIp.append('.');
            }
        }

        return decimalIp.toString();
    }

    @Override
    public boolean contains(JIPAddress ipAddress) {
        boolean result = false;
        switch (ipAddress.getIpType()) {
            case IPV4:
                result = contains(start, end, (JIPv4Address) ipAddress);
                break;
            case IPV4_SEGMENT:
            case IPV4_SEGMENT_WITH_MASK:
                result = contains(start, end, (JIPv4AddressRange) ipAddress);
                break;
        }
        return result;
    }

    private boolean contains(JIPv4Address s, JIPv4Address e, JIPv4Address item) {
        if (item == null) {
            return false;
        }
        long min = Math.min(s != null ? s.getIpAddress() : -1L, e != null ? e.getIpAddress() : -1L);
        long max = Math.max(s != null ? s.getIpAddress() : -1L, e != null ? e.getIpAddress() : -1L);
        return item.getIpAddress() >= min && item.getIpAddress() <= max;
    }

    private boolean contains(JIPv4Address s, JIPv4Address e, JIPv4AddressRange jiPv4AddressRange) {
        if (jiPv4AddressRange == null) {
            return false;
        }
        return contains(s, e, jiPv4AddressRange.getStart())
                || contains(jiPv4AddressRange.getStart(), jiPv4AddressRange.getEnd(), s);
    }

    // 重写一下，只比较起止ip值，其他的data等字段不比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // if (!super.equals(o)) return false;

        JIPv4AddressRange range = (JIPv4AddressRange) o;

        if (start != null ? !start.equals(range.start) : range.start != null) return false;
        return end != null ? end.equals(range.end) : range.end == null;
    }

    @Override
    public int hashCode() {
        // int result = super.hashCode();
        int result = 666;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(JIPAddress o) {
        return JIPAddressComparator.compareTo(this, o);
    }

    @Override
    public String toString() {
        String result;
        switch (ipType) {
            case IPV4_SEGMENT:
                if (start.compareTo(end) == 0) {
                    result = start.toString();
                    break;
                }
                String s = start.toString();
                String e = end.toString();
                int index1 = s.lastIndexOf(".");
                int index2 = e.lastIndexOf(".");
                if (StringUtils.equals(s.substring(0, index1), e.substring(0, index2))) {
                    result = String.format("%s-%s", s, e.substring(index2 + 1));
                    break;
                }
                result = String.format("%s-%s", start.toString(), end.toString());
                break;
            case IPV4_SEGMENT_WITH_MASK:
                result = String.format("%s/%s", start.toString(), mask);
                break;
            default:
                result = "invalid ip v4 range";
                break;
        }
        return result;
    }

    @Override
    public long ipCount() {
        long min = Math.min(start != null ? start.getIpAddress() : 0, end != null ? end.getIpAddress() : 0);
        long max = Math.max(start != null ? start.getIpAddress() : 0, end != null ? end.getIpAddress() : 0);
        return max - min + 1;
    }

    @Override
    public boolean tryConvertMask() {
        if (IPType.IPV4_SEGMENT_WITH_MASK.equals(this.ipType)) {
            return true;
        }
        long count = ipCount();
        if ((count & (count - 1)) != 0L) {
            return false;
        }
        int bitCount = 0;
        while (count > 0 && bitCount <= 32) {
            bitCount++;
            count >>= 1;
        }
        bitCount = 32 - bitCount + 1;
        if (bitCount > 32) {
            return false;
        }

        JIPv4Address tmpMask = computeMaskFromNetworkPrefix(bitCount);
        if ((start.getIpAddress() & tmpMask.getIpAddress()) != (end.getIpAddress() & tmpMask.getIpAddress())) {
            return false;
        }

        return parseIPRange(String.format("%s/%s", start.toString(), bitCount));
    }

    @Override
    public Iterator<JIPAddress> iterator() {
        return new JIPAddressIterator(start, end);
    }
}
