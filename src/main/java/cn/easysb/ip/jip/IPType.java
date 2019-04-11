package cn.easysb.ip.jip;

import lombok.Getter;

/**
 * Created by hzhujiankang on 2018/11/19.
 */
public enum IPType {
    UNKNOWN(0, "未知"),

    IPV4(1, "IPV4"),
    IPV6(2, "IPV6"),

    // 10.1.1.1-244, 10.1.1.1-10.1.1.244
    IPV4_SEGMENT(3, "IPV4段"),
    // 10.1.1.1/24
    IPV4_SEGMENT_WITH_MASK(4, "IPV4段带掩码"),

    IPV6_SEGMENT(5, "IPV6段"),
    IPV6_SEGMENT_WITH_MASK(6, "IPV6段带掩码"),;

    @Getter
    private int code;
    @Getter
    private String name;

    IPType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static IPType getByCode(int code) {
        if (code < -1) {
            return null;
        }
        for (IPType ipType : IPType.values()) {
            if (ipType.getCode() == code) {
                return ipType;
            }
        }
        return null;
    }

    public static boolean isIPV4(IPType ipType) {
        return IPV4.equals(ipType) || IPV4_SEGMENT.equals(ipType) || IPV4_SEGMENT_WITH_MASK.equals(ipType);
    }

    public static boolean isIPV6(IPType ipType) {
        return IPV6.equals(ipType) || IPV6_SEGMENT.equals(ipType) || IPV6_SEGMENT_WITH_MASK.equals(ipType);
    }

    public static boolean isIPSegment(IPType ipType) {
        return IPV4_SEGMENT.equals(ipType) || IPV4_SEGMENT_WITH_MASK.equals(ipType)
                || IPV6_SEGMENT.equals(ipType) || IPV6_SEGMENT_WITH_MASK.equals(ipType);
    }
}
