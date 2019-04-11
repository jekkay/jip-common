package cn.easysb.ip.jip.ip;

import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.iprange.JIPAddressIterator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Iterator;


/**
 * 封装一层吧，以后内置换库也方便一点
 * Created by hzhujiankang on 2018/11/22.
 */
@Data
public class JIPv4Address implements JIPAddress {
    /**
     * 类型
     */
    private IPType ipType;

    /**
     * 备注 比如解析 "10.1.1.2 # IT专用" ==> 那remark将存储 "IT专用"
     */
    private String remark;

    /**
     * 额外带数据
     */
    private Object data;

    /**
     * 用一个long来存储,以后可以用**其他的库**替代
     */
    private long ipAddress;

    public JIPv4Address() {
        reset();
    }

    public JIPv4Address(String ipAddressStr) {
        reset();
        this.ipAddress = this.parseIPAddress(ipAddressStr);
    }

    public JIPv4Address(long address) {
        reset();
        // 如果超过一定范围就认为是错误的
        if ((address & 0x0FFFFFFFF) != address) {
            throw new IllegalArgumentException("address is invalid " + address);
        }
        this.ipAddress = address;
    }

    @Override
    public void reset() {
        ipType = IPType.IPV4;
        remark = "";
        data = null;
        ipAddress = 0;
    }

    public final boolean isClassA() {
        return (this.ipAddress >> 31) == 0;
    }

    public final boolean isClassB() {
        return (this.ipAddress >> 30) == 2;
    }

    public final boolean isClassC() {
        return (this.ipAddress >> 29) == 6;
    }

    /*
    public final boolean isClassD() {
        return (this.ipAddress >> 28) == 14;
    }

    public final boolean isClassE() {
        return (this.ipAddress >> 28) == 15;
    }*/

    final long parseIPAddress(String ipAddressStr) {
        long result = 0;
        if (StringUtils.isBlank(ipAddressStr)) {
            throw new IllegalArgumentException();
        }
        ipAddressStr = StringUtils.trim(ipAddressStr);

        // 提取注释
        int index = ipAddressStr.indexOf("#");
        if (index >= 0) {
            remark = StringUtils.trim(StringUtils.substring(ipAddressStr, index + 1));
            ipAddressStr = StringUtils.trim(StringUtils.substring(ipAddressStr, 0, index));
            if (StringUtils.isBlank(ipAddressStr)) {
                throw new IllegalArgumentException();
            }
        }

        String ex = ipAddressStr;
        long offset = 24;

        long number;
        for (number = 0; number < 3; ++number) {
            index = ex.indexOf('.');
            if (index == -1) {
                throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
            }

            String numberStr = ex.substring(0, index);
            long number1 = Integer.parseInt(numberStr);
            if (number1 < 0 || number1 > 255) {
                throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
            }

            result += number1 << offset;
            offset -= 8;
            ex = ex.substring(index + 1);
        }

        if (StringUtils.isBlank(ex)) {
            throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
        }
        if (!NumberUtils.isNumber(ex)) {
            throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
        }
        number = Integer.parseInt(ex);
        if (number >= 0 && number <= 255) {
            result += number << offset;
            this.ipAddress = result;
        } else {
            throw new IllegalArgumentException("Invalid IP Address [" + ipAddressStr + "]");
        }

        return result;
    }

    @Override
    public int compareTo(JIPAddress o) {
        return JIPAddressComparator.compareTo(this, o);
    }

    // 重写一下，只比较ip值，其他的type，data等字段不比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // if (!super.equals(o)) return false;

        JIPv4Address that = (JIPv4Address) o;

        return ipAddress == that.ipAddress;
    }

    @Override
    public int hashCode() {
        // int result = super.hashCode();
        int result = 271;
        result = 31 * result + (int) (ipAddress ^ (ipAddress >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return convertString(this.ipAddress);
    }

    public static String convertString(long ipAddress) {
        StringBuilder result = new StringBuilder();
        long temp;
        temp = ipAddress >> 24 & 255;
        result.append(temp);
        result.append(".");
        temp = ipAddress >> 16 & 255;
        result.append(temp);
        result.append(".");
        temp = ipAddress >> 8 & 255;
        result.append(temp);
        result.append(".");
        temp = ipAddress & 255;
        result.append(temp);
        return result.toString();
    }

    @Override
    public long ipCount() {
        return 1;
    }

    @Override
    public Iterator<JIPAddress> iterator() {
        return new JIPAddressIterator(this, this);
    }
}
