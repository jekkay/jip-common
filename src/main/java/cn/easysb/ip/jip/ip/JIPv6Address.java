package cn.easysb.ip.jip.ip;


import com.googlecode.ipv6.IPv6Address;
import cn.easysb.ip.jip.IPType;
import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressComparator;
import cn.easysb.ip.jip.iprange.JIPAddressIterator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

/**
 * * 封装一层吧，以后内置换库也方便一点
 * 临时： 内置实现暂时用com.googlecode.java-ipv6
 * Created by hzhujiankang on 2018/11/22.
 */
@Data
public class JIPv6Address implements JIPAddress {
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
     * 用一个com.googlecode.java-ipv6来存储,以后可以用**其他的库**替代
     * https://github.com/janvanbesien/java-ipv6
     */
    private IPv6Address address;

    public JIPv6Address() {
        reset();
    }

    public JIPv6Address(String ip) {
        reset();
        // 提取注释
        int index = ip.indexOf("#");
        if (index >= 0) {
            remark = StringUtils.trim(StringUtils.substring(ip, index + 1));
            ip = StringUtils.trim(StringUtils.substring(ip, 0, index));
            if (StringUtils.isBlank(ip)) {
                throw new IllegalArgumentException();
            }
        }
        this.address = IPv6Address.fromString(ip);
    }

    public void reset() {
        ipType = IPType.IPV6;
        remark = "";
        data = null;
        address = null;
    }

    @Override
    public int compareTo(JIPAddress o) {
        return JIPAddressComparator.compareTo(this, o);
    }

    @Override
    public String toString() {
        return address != null ? address.toString() : "invalid address";
    }

    // 重写一下，只比较ip值，其他的type，data等字段不比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        // if (!super.equals(o)) return false;

        JIPv6Address that = (JIPv6Address) o;

        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        // int result = super.hashCode();
        int result = 472;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public long ipCount() {
        // TODO: fix me later
        return 0;
    }

    @Override
    public Iterator<JIPAddress> iterator() {
        return new JIPAddressIterator(this, this);
    }
}
