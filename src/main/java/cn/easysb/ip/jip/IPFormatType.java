package cn.easysb.ip.jip;

import lombok.Getter;

/**
 * Created by hzhujiankang on 2018/12/18.
 */
public enum IPFormatType {
    SEGMENT_WITH_MASK_FIRST(1, "优先转化成带掩码的IP格式"), // 1.1.1.0/24
    SEGMENT_SIMPLE_FIRST(2, "优先转化成带简化起止IP格式"), // 1.1.1.0-255
    SEGMENT_FULL_FIRST(3, "优先转化成带完全起止IP格式"),  // 1.1.1.0-1.1.1.255
    ;

    @Getter
    private int code;
    @Getter
    private String name;

    IPFormatType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static IPFormatType getByCode(int code) {
        if (code < -1) {
            return null;
        }
        for (IPFormatType ipFormatType : IPFormatType.values()) {
            if (ipFormatType.getCode() == code) {
                return ipFormatType;
            }
        }
        return null;
    }
}
