package cn.easysb.ip.jip;

import java.util.Collection;
import java.util.List;

/**
 * 注意: 虽然实现Collection的接口，但是尽量用本JIPAddressSet声明的接口，部分用注释的方法写上了，比如clear(), size()
 * Created by jekkay on 2018/11/30.
 */
public interface JIPAddressSet extends Collection<JIPAddress> {
    /**
     * 查找包含该ip的节点
     *
     * @param jipAddress 如果是单个ip，则返回第一个包含或者等于该ip的节点
     *                   如果是ip段，则返回和这个ip段有交集的节点
     */
    JIPAddress findIp(JIPAddress jipAddress);

    JIPAddress findIp(String ip);

    /**
     * 获取IP对应外带数据
     */
    <T> T findIpData(String ip, Class<T> tClass);

    /**
     * 插入节点
     */
    JIPAddress insertIp(JIPAddress jipAddress);

    JIPAddress insertIp(String ip);

    /**
     * 删除节点
     */

    boolean deleteIp(JIPAddress jipAddress);

    boolean deleteIp(String ip);

    /**
     * 清除
     */
    // void clear();

    /**
     * 大小
     */
    // int size();

    /**
     * 收集
     */
    List<JIPAddress> all();

    /**
     * 获取ip的数量, 目前只支持IPv4， 因为IPV6段的数量无法用long表示
     */

    long ipCount();

    /**
     * 集合交叉运算
     */
    JIPAddressSet intersect(JIPAddressSet dstSet);

    /**
     * 集合差集
     */
    JIPAddressSet subtract(JIPAddressSet dstSet);

    /**
     * 集合并集
     */
    JIPAddressSet union(JIPAddressSet dstSet);

    // 调试用
    void outputTree();
}
