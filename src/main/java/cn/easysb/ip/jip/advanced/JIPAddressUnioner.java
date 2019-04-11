package cn.easysb.ip.jip.advanced;

import com.google.common.collect.Lists;
import cn.easysb.ip.jip.JIPAddress;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * 并集
 * Created by hzhujiankang on 2018/12/18.
 */
public class JIPAddressUnioner {
    public static List<JIPAddress> union(JIPAddress src, JIPAddress dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (src != null) {
            addressList.add(src);
        }
        if (dst != null) {
            addressList.add(dst);
        }
        return JIPAddressCombiner.combine(addressList);
    }

    public static List<JIPAddress> union(JIPAddress src, List<JIPAddress> dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (src != null) {
            addressList.add(src);
        }
        if (CollectionUtils.isNotEmpty(dst)) {
            addressList.addAll(dst);
        }
        return JIPAddressCombiner.combine(addressList);
    }

    public static List<JIPAddress> union(List<JIPAddress> src, JIPAddress dst) {
        return union(dst, src);
    }

    public static List<JIPAddress> union(List<JIPAddress> src, List<JIPAddress> dst) {
        List<JIPAddress> addressList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(src)) {
            addressList.addAll(src);
        }
        if (CollectionUtils.isNotEmpty(dst)) {
            addressList.addAll(dst);
        }
        return JIPAddressCombiner.combine(addressList);
    }
}
