package cn.easysb.ip.jip.iprange;

import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.JIPAddressRange;
import cn.easysb.ip.jip.ip.JIPv6Address;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Iterator;

/**
 * * 封装一层吧，以后内置换库也方便一点
 * 临时： 内置实现暂时用com.googlecode.java-ipv6
 * Created by hzhujiankang on 2018/11/22.
 */

@Slf4j
@Data
public class JIPv6AddressRange implements JIPAddressRange {
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
    private JIPv6Address start, end;

    /**
     * 如果是ipType=IPV6_SEGMENT，则字段addressRange有效
     * 如果是ipType=IPV6_SEGMENT_WITH_MASK，network
     */
    private IPv6AddressRange range;
    private IPv6Network network;

    /**
     * 原始字符串
     */
    private String rawString;

    public JIPv6AddressRange() {
        reset();
    }

    public JIPv6AddressRange(String ipRange) {
        reset();
        parseIPRange(ipRange);
    }

    public void reset() {
        ipType = IPType.IPV6_SEGMENT;
        remark = "";
        data = null;
        start = end = null;
        range = null;
        network = null;
    }

    protected boolean parseIPRange(String ipRange) {
        rawString = ipRange;
        ipRange = StringUtils.trim(ipRange);
        if (StringUtils.isBlank(ipRange)) {
            throw new IllegalArgumentException("invalid ipv6 segment:" + ipRange);
        }
        // 提取注释
        int s = ipRange.indexOf("#");
        if (s >= 0) {
            remark = StringUtils.trim(StringUtils.substring(ipRange, s + 1));
            ipRange = StringUtils.trim(StringUtils.substring(ipRange, 0, s));
            if (StringUtils.isBlank(ipRange)) {
                throw new IllegalArgumentException("illegal ipv6 ranges: " + rawString);
            }
        }
        s = ipRange.indexOf('-');
        if (s >= 0) {
            String startString = StringUtils.trim(StringUtils.substring(ipRange, 0, s));
            String endString = StringUtils.trim(StringUtils.substring(ipRange, s + 1));
            if (StringUtils.isBlank(startString) || StringUtils.isBlank(endString)) {
                throw new IllegalArgumentException("illegal ipv6 ranges: " + rawString);
            }
            // 原生不支持 fe80::226:2dff:fefa:3-10，需要转换下
            if (endString.indexOf(":") < 0 && NumberUtils.isNumber("0x" + endString)) {
                int lastIndex = startString.lastIndexOf(":");
                if (lastIndex <= 0) {
                    throw new IllegalArgumentException("illegal ipv6 ranges: " + rawString);
                }
                endString = startString.substring(0, lastIndex + 1) + endString;
            }
            setRange(IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(startString),
                    IPv6Address.fromString(endString)));
            return true;
        }

        s = ipRange.indexOf("/");
        if (s > 0) {
            setNetwork(IPv6Network.fromString(ipRange));
            return true;
        }
        throw new IllegalArgumentException("illegal ipv6 ranges: " + rawString);
        // return true;
    }

    public void setRange(IPv6AddressRange range) {
        this.range = range;
        if (this.range != null) {
            this.ipType = IPType.IPV6_SEGMENT;
            this.network = null;
        }
        this.calculateStartEnd();
    }

    public void setNetwork(IPv6Network network) {
        this.network = network;
        if (this.network != null) {
            this.ipType = IPType.IPV6_SEGMENT_WITH_MASK;
            this.range = null;
        }
        this.calculateStartEnd();
    }

    public void calculateStartEnd() {
        IPv6AddressRange parsed = this.getParsedRange();
        if (parsed != null) {
            start = new JIPv6Address(parsed.getFirst().toString());
            end = new JIPv6Address(parsed.getLast().toString());
        } else {
            start = end = null;
        }
    }

    public IPv6AddressRange getParsedRange() {
        if (IPType.IPV6_SEGMENT.equals(this.ipType)) {
            return range;
        }
        if (IPType.IPV6_SEGMENT_WITH_MASK.equals(this.ipType)) {
            return network;
        }
        return null;
    }

    @Override
    public boolean contains(JIPAddress jipAddress) {
        if (jipAddress == null) {
            return false;
        }
        IPv6AddressRange parsed = this.getParsedRange();
        if (parsed == null) {
            return false;
        }
        boolean result = false;
        IPv6Address tmp;
        switch (jipAddress.getIpType()) {
            case IPV6:
                tmp = ((JIPv6Address) jipAddress).getAddress();
                if (tmp == null) {
                    break;
                }
                result = parsed.contains(tmp);
                break;
            case IPV6_SEGMENT:
            case IPV6_SEGMENT_WITH_MASK:
                JIPv6AddressRange jiPv6AddressRangeTmp = ((JIPv6AddressRange) jipAddress);
                result = contains(parsed, jiPv6AddressRangeTmp.getStart()) ||
                        contains(jiPv6AddressRangeTmp.getParsedRange(), this.getStart());
                break;
        }
        return result;
    }

    private static boolean contains(IPv6AddressRange iPv6AddressRange, JIPv6Address jiPv6Address) {
        if (iPv6AddressRange == null || jiPv6Address == null || jiPv6Address.getAddress() == null) {
            return false;
        }
        return iPv6AddressRange.contains(jiPv6Address.getAddress());
    }

    @Override
    public int compareTo(JIPAddress o) {
        return JIPAddressComparator.compareTo(this, o);
    }

    @Override
    public String toString() {
        String result;
        switch (ipType) {
            case IPV6_SEGMENT:
                if (start.compareTo(end) == 0) {
                    result = start.toString();
                    break;
                }
                String s = start.toString();
                String e = end.toString();
                int index1 = s.lastIndexOf(":");
                int index2 = e.lastIndexOf(":");
                if (StringUtils.equals(s.substring(0, index1), e.substring(0, index2))) {
                    result = String.format("%s-%s", s, e.substring(index2 + 1));
                    break;
                }
                result = String.format("%s-%s", start.toString(), end.toString());
                break;
            case IPV6_SEGMENT_WITH_MASK:
                result = getParsedRange().toString();
                break;
            default:
                result = "invalid ip v6 range";
                break;
        }
        return result;
    }

    // 重写一下，只比较起止ip值，data等字段不比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // if (!super.equals(o)) return false;

        IPv6AddressRange t1 = this.getParsedRange();
        IPv6AddressRange t2 = ((JIPv6AddressRange) o).getParsedRange();
        if (t1 == null || t2 == null) {
            return t1 == t2;
        }
        return t1.equals(t2);
    }

    @Override
    public int hashCode() {
        // int result = super.hashCode();
        int result = 982;
        result = 31 * result + (ipType != null ? ipType.hashCode() : 0);
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (network != null ? network.hashCode() : 0);
        return result;
    }

    @Override
    public long ipCount() {
        // TODO: fix me later
        return 0;
    }

    @Override
    public boolean tryConvertMask() {
        if (IPType.IPV6_SEGMENT_WITH_MASK.equals(ipType)) {
            // 已经是
            return true;
        }
        try {
            IPv6Network tmpNetwork = IPv6Network.fromTwoAddresses(start.getAddress(), end.getAddress());
            if (tmpNetwork != null && tmpNetwork.compareTo(getParsedRange()) == 0) {
                parseIPRange(tmpNetwork.toString());
                return true;
            }
        } catch (Exception e) {
            // 吞掉异常
            log.error("tryConvertMask", e);
        }
        return false;
    }

    @Override
    public Iterator<JIPAddress> iterator() {
        return new JIPAddressIterator(start, end);
    }
}
