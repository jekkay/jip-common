package cn.easysb.ip;


import cn.easysb.ip.jip.*;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jekkay on 2018/11/29.
 */
public class JIpAddressTest {
    private String ipList[] = {
            "172.255.255.255  /  24 # hello world",
            "172.172.172.173 - 255  # 运维部",
            "0.0.0.0-23   # IT专用",
            " 172.172.172.1728  # IT专用",
            "10.1.1.1  # 123 ",
            "0.0.0.0 #",
            " 255.255.255.255 #",
            "172.255.255.255",
            "172.172.255.255",
            "172.172.172.255",
            "172.172.172.172",
            "1172.172.172.1728",
            "10.1.1.8-32",
            "255.255.255.255-11123",
            "172.172.255.255/32",
            "172.172.172.255/12",
            "172.172.172.255/78",
            "172.172.172.172-255",
            "172.172.172.173-254",
            "171.171.171.171-171.171.171.254 # 信息安全部",
            "10.1.1.9-10.1.1.9.255",
    };

    private String ipv6List[] = {
            "8080::226:2dff:fefa:0/112 # 123123",
            "8080::226:2dff:fefa:cd1f #运维部",
            "::ffff:192.168.0.1    #  运维部  ",
            "8080::226:2dff:fefa:cd1f-8080::226:2dff:fefa:ffff #  # 信息安全部",
            "8080::226:2dff:fefa:0-8080::226:2dff:fefa:ffff # 运维部123 ",
    };

    private String[] findIpList = {
            "0.0.0.0",
            "0.0.0.13",
            "12.0.0.13",
            "12.0.0.13/24",
            "172.172.255.255/24",
            "172.172.172.172/32",
            "::ffff:192.168.0.1# 运维部123 ",
            "8080::226:2dff:fefa:222  # 运维部123 ",
            "1111::226:2dff:fefa:222 # # 运维部123  #",
            "8080::226:2dff:fefa:0/118# 运维部123 "
    };

    // copy from package
    public static String join(Collection<?> input, String sep) {
        if (input == null || input.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int size = input.size();
        Iterator<?> it = input.iterator();// 遍历
        while (it.hasNext()) {
            if (index == size - 1) {
                break;
            }
            Object o = it.next();
            sb.append(o).append(sep);
            index++;
        }
        sb.append(it.next());
        return sb.toString();
    }

    @Test
    public void testIt() {
        JIPAddress address = null;
        List<JIPAddress> addressList = Lists.newArrayList();

        for (String ip : ipList) {
            address = JIPAddressUtils.toIpObject(ip);
            System.out.println(String.format("%s => %s", ip, address != null ? JSON.toJSONString(address) : "null"));
            if (address != null) {
                addressList.add(address);
            }
        }

        Collections.sort(addressList);
        output(addressList);
    }

    private void output(List<JIPAddress> addressList) {
        if (!CollectionUtils.isNotEmpty(addressList)) {
            return;
        }
        for (JIPAddress jipAddress : addressList) {
            System.out.println(String.format("%s  => %s", jipAddress.toString(), JSON.toJSONString(jipAddress)));
        }
    }

    @Test
    public void testCompareV4() {
        JIPAddress a1 = JIPAddressUtils.toIpObject("10.0.0.5/24");
        JIPAddress a2 = JIPAddressUtils.toIpObject("10.0.0.0-255");
        System.out.println(String.format("%s == %s = %s, %s",
                a1.toString(), a2.toString(), a1.compareTo(a2), a1.equals(a2)));

    }

    @Test
    public void testSortV4() {
        List<JIPAddress> addressList = Lists.newArrayList();
        addressList.add(JIPAddressUtils.toIpObject("172.172.255.255"));
        addressList.add(JIPAddressUtils.toIpObject("172.172.255.255/32"));
        addressList.add(JIPAddressUtils.toIpObject("172.172.255.255/24"));
        Collections.sort(addressList);
        output(addressList);
    }

    @Test
    public void testBrief() {
        JIPAddress address = JIPAddressUtils.toIpObject("172.172.255.5-172.172.255.44");
        System.out.println(address.toString());
    }


    @Test
    public void testParseIpV6() {
        List<JIPAddress> addressList = Lists.newArrayList();

        for (String s : ipv6List) {
            JIPAddress tmp = JIPAddressUtils.toIpObject(s);
            if (tmp != null) {
                addressList.add(tmp);
            }
        }

        Collections.sort(addressList);
        output(addressList);
    }


    @Test
    public void testBuildIpSet() {
        List<String> tmpIpList = Lists.newArrayList();
        tmpIpList.addAll(Lists.newArrayList(ipList));
        tmpIpList.addAll(Lists.newArrayList(ipv6List));
        JIPAddressSet addressSet = JIPAddressUtils.buildAddressSet(join(tmpIpList, "\n"));
        if (addressSet == null) {
            return;
        }
        System.out.println(String.format("Tree size %s, ip size %s", addressSet.size(), tmpIpList.size()));
        List<JIPAddress> all = addressSet.all();
        System.out.println(String.format("all size %s", all.size()));
        output(all);
        // find
        for (String fip : findIpList) {
            JIPAddress findAddress = addressSet.findIp(fip);
            System.out.println(String.format("find %s in %s", fip, findAddress != null ? findAddress.toString() : "null"));
        }
        // 删除
        for (String tmp : findIpList) {
            boolean res = addressSet.deleteIp(tmp);
            System.out.println(String.format("tree size %s, delete %s, %s", addressSet.size(), tmp, res));
        }
        for (String tmp : tmpIpList) {
            boolean res = addressSet.deleteIp(tmp);
            System.out.println(String.format("tree size %s, delete %s, %s", addressSet.size(), tmp, res));
        }

        List<JIPAddress> leftArray = addressSet.all();
        output(leftArray);
        if (CollectionUtils.isNotEmpty(leftArray)) {
            for (JIPAddress address : leftArray) {
                boolean res = addressSet.deleteIp(address.toString());
                System.out.println(String.format("tree size %s, delete %s, %s", addressSet.size(), address.toString(), res));
            }
        }
    }

    @Test
    public void testSetToArray() {
        JIPAddressSet addressSet = buildSet();
        if (addressSet == null) {
            return;
        }

        String tmp1[] = addressSet.toArray(new String[addressSet.size()]);
        System.out.println(tmp1 != null ? tmp1.length : -1);
        JIPAddress tmp2[] = addressSet.toArray(new JIPAddress[addressSet.size()]);
        System.out.println(tmp2 != null ? tmp2.length : -1);
    }

    private JIPAddressSet buildSet() {
        List<String> tmpIpList = Lists.newArrayList();
        tmpIpList.addAll(Lists.newArrayList(ipList));
        return JIPAddressUtils.buildAddressSet(join(tmpIpList, "\n"));
    }

    @Test
    public void testSetRetainAll() {
        JIPAddressSet addressSet = buildSet();
        if (addressSet == null) {
            return;
        }
        List<JIPAddress> tmpIpList  = Lists.newArrayList();
        List<JIPAddress> all = addressSet.all();
        for (int i = 0;i < addressSet.size(); i ++) {
            if (i % 2 == 0) {
                tmpIpList.add(all.get(i));
            }
        }
        addressSet.retainAll(tmpIpList);
        System.out.println(String.format("all size %s", addressSet.size()));
        addressSet.retainAll(Lists.newArrayList(ipv6List));
        System.out.println(String.format("all size %s", addressSet.size()));
    }

    @Test
    public void testIPv4RangeContains() {
        JIPAddressRange a1 = (JIPAddressRange) JIPAddressUtils.toIpObject("127.0.0.10-20");
        JIPAddressRange a2 = (JIPAddressRange) JIPAddressUtils.toIpObject("127.0.0.3-30");
        JIPAddressRange a3 = (JIPAddressRange) JIPAddressUtils.toIpObject("127.0.0.15-16");
        System.out.println(String.format("%s contains %s : %s", a1.toString(), a2.toString(), a1.contains(a2)));
        System.out.println(String.format("%s contains %s : %s", a1.toString(), a3.toString(), a1.contains(a3)));
        System.out.println(String.format("%s contains %s : %s", a2.toString(), a3.toString(), a2.contains(a3)));
    }

    @Test
    public void testIPv6RangeContains() {
        JIPAddressRange a1 = (JIPAddressRange) JIPAddressUtils.toIpObject("8080::226:2dff:fefa:10-8080::226:2dff:fefa:20");
        JIPAddressRange a2 = (JIPAddressRange) JIPAddressUtils.toIpObject("8080::226:2dff:fefa:3-8080::226:2dff:fefa:30");
        JIPAddressRange a3 = (JIPAddressRange) JIPAddressUtils.toIpObject("8080::226:2dff:fefa:15-8080::226:2dff:fefa:16");
        JIPAddressRange a4 = (JIPAddressRange) JIPAddressUtils.toIpObject("8080::226:2dff:fefa:21-8080::226:2dff:fefa:25");
        System.out.println(String.format("%s contains %s : %s", a1.toString(), a2.toString(), a1.contains(a2)));
        System.out.println(String.format("%s contains %s : %s", a1.toString(), a3.toString(), a1.contains(a3)));
        System.out.println(String.format("%s contains %s : %s", a2.toString(), a3.toString(), a2.contains(a3)));
        System.out.println(String.format("%s contains %s : %s", a1.toString(), a4.toString(), a1.contains(a4)));
    }

    @Test
    public void testConvertIpMask() {
        String [] ips = {
                "2.3.4.0-255",
                "2.3.4.5-12",
                "2.3.4.5-8",
                "2.3.4.0-3",
                "2.3.4.1-5",
                "2.3.4.2-6",
                "2.3.4.0-1",
                "2.3.4.1-1",
        };
        for (String ip : ips) {
            JIPAddressRange range = (JIPAddressRange)JIPAddressUtils.toIpObject(ip);
            System.out.print(range.toString());
            System.out.print(" convert " + range.tryConvertMask() + " : ");
            System.out.println(range.toString());
        }

        for ( int i = 0; i <= 32 ; i ++) {
            String s1 = String.format("2.3.4.5/%d",i);
            JIPAddressRange tmp1 = (JIPAddressRange)JIPAddressUtils.toIpObject(s1);
            String ip = String.format("%s-%s", tmp1.getStart().toString(), tmp1.getEnd().toString());
            JIPAddressRange range = (JIPAddressRange)JIPAddressUtils.toIpObject(ip);
            System.out.print(s1 + " ");
            System.out.print(range.toString());
            System.out.print(" convert " + range.tryConvertMask() + " : ");
            System.out.println(range.toString());
        }

    }

    @Test
    public void testRetainAll() {
        String [] ips = {
                "2.3.4.0-255",
                "2.3.5.5-12",
                "2.3.6.5-8",
                "2.3.7.0-3",
        };
        JIPAddressSet addressSet = JIPAddressUtils.buildAddressSet(StringUtils.join(ips, ","));
        List<String> tmpList = Lists.newLinkedList();
        tmpList.add("2.3.4.10-20");
        tmpList.add("2.3.7.2-10");
        addressSet.retainAll(tmpList);
        output(addressSet.all());
    }

    @Test
    public void testContain2() {
        JIPAddressSet addressSet = JIPAddressUtils.buildAddressSet("101.111.129.0/24;101.111.132.0/24;101.111.133.0/24;101.111.134.0/24;101.111.135.0/24;101.111.136.0/21");
        JIPAddress address2 = JIPAddressUtils.toIpObject("101.111.141.247");
        Object o = addressSet.findIp(address2);
        if (o != null) {
            System.out.println(o.toString());
        }
    }

    @Test
    public void testCompare12() {
        JIPAddress address1 = JIPAddressUtils.toIpObject("101.111.135.0/24");
        JIPAddress address2 = JIPAddressUtils.toIpObject("101.111.136.0/21");
        System.out.println(address1.compareTo(address2));
    }

    @Test
    public void testOutput() {
        JIPAddress address = JIPAddressUtils.toIpObject("1.2.3.1-3");
        System.out.println(address.toString());
        System.out.println(JIPAddressUtils.toIpString(address, IPFormatType.SEGMENT_WITH_MASK_FIRST));
        System.out.println(JIPAddressUtils.toIpString(address, IPFormatType.SEGMENT_SIMPLE_FIRST));
        System.out.println(JIPAddressUtils.toIpString(address, IPFormatType.SEGMENT_FULL_FIRST));
    }

    @Test
    public void testIterV4() {
        String [] ipList = {
                "1.2.3.1",
                "1.2.3.21-23",
                "1.2.3.100/30",
                "1.2.3.200-205",
        };
        for (String ip : ipList) {
            testInterInner(ip);
        }
    }

    private void testInterInner(String ip) {
        JIPAddress address = JIPAddressUtils.toIpObject(ip);
        Iterator<JIPAddress> iterator = address.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next().toString());
        }
    }

    @Test
    public void testIterV6() {
        String [] ipList = {
                "8080::226:2dff:fefa:100",
                "8080::226:2dff:fefa:210-8080::226:2dff:fefa:220",
                "8080::226:2dff:fefa:310/127",
        };
        for (String ip : ipList) {
            testInterInner(ip);
        }
    }

    @Test
    public void testInternal() {
        JIPAddress ip =  JIPAddressUtils.toIpObject("10.0.0.1-172.16.0.0");
        System.out.println(JIPAddressUtils.isInternalIp(ip));
        ip =  JIPAddressUtils.toIpObject("10.0.0.1/24");
        System.out.println(JIPAddressUtils.isInternalIp(ip));
        ip =  JIPAddressUtils.toIpObject("192.168.0.0/24");
        System.out.println(JIPAddressUtils.isInternalIp(ip));
    }
}
