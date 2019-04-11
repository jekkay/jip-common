package cn.easysb.ip.jip.ipset;

import com.google.common.collect.Lists;
import cn.easysb.ip.jip.*;
import cn.easysb.ip.jip.advanced.JIPAddressIntersecter;
import cn.easysb.ip.jip.advanced.JIPAddressSubtracter;
import cn.easysb.ip.jip.advanced.JIPAddressUnioner;
import cn.easysb.ip.redblacktree.RedBlackNode;
import cn.easysb.ip.redblacktree.RedBlackTree;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by jekkay on 2018/11/30.
 */
@Slf4j
public class JIPAddressRBTreeSet extends RedBlackTree<JIPAddress> implements JIPAddressSet {
    @Override
    public JIPAddress findIp(JIPAddress jipAddress) {
        RedBlackNode<JIPAddress> node = findIpNode(jipAddress);
        return node != null ? node.key : null;
    }

    // 需要重写search函数，因为ip有段，段可以包含单个ip
    protected RedBlackNode<JIPAddress> findIpNode(JIPAddress jipAddress) {
        // 调用一下，确保已经计算过最小最大值
        this.fixMinMaxValue();
        // 避免堆栈过深，使用队列来查找
        Queue<RedBlackNode<JIPAddress>> doCompareQueue = new ConcurrentLinkedDeque<>();
        doCompareQueue.add(this.getRoot());

        RedBlackNode<JIPAddress> current;
        RedBlackNode<JIPAddress> find = null;
        while (doCompareQueue.size() > 0) {
            current = doCompareQueue.poll();
            if (isNil(current)) {
                continue;
            }
            if (JIPAddressComparator.hasIntersection(current.key, jipAddress)) {
                find = current;
                break;
            }
            // 如果没交集，则没必要往下检测子节点了
            if (!checkMinMaxValue(current, jipAddress)) {
                continue;
            }
            if (current.key.compareTo(jipAddress) < 0) {
                // 因为是按照先比较开始，再比较结束的方式来比较的，所以左边一定要比较
                if (!isNil(current.getLeft())) {
                    doCompareQueue.add(current.getLeft());
                }
                if (!isNil(current.getRight())) {
                    doCompareQueue.add(current.getRight());
                }
            } else {
                if (!isNil(current.getLeft())) {
                    doCompareQueue.add(current.getLeft());
                }
            }
        }
        doCompareQueue.clear();
        return find;
    }

    protected boolean checkMinMaxValue(RedBlackNode<JIPAddress> node, JIPAddress jipAddress) {
        //  min max有可能不属于同一个ip类型
        if (node.minValue == null || node.maxValue == null) {
            return false;
        }
        JIPAddress tmpMin2 = minValue(jipAddress);
        JIPAddress tmpMax2 = maxValue(jipAddress);

        return (node.minValue.compareTo(tmpMin2) <= 0 && node.maxValue.compareTo(tmpMin2) >= 0) ||
                (tmpMin2.compareTo(node.minValue) <= 0 && tmpMax2.compareTo(node.minValue) >= 0);

    }

    @Override
    protected JIPAddress minValue(JIPAddress value) {
        if (value != null && value instanceof JIPAddressRange) {
            return ((JIPAddressRange) value).getStart();
        }
        return super.minValue(value);
    }

    @Override
    protected JIPAddress maxValue(JIPAddress value) {
        if (value != null && value instanceof JIPAddressRange) {
            return ((JIPAddressRange) value).getEnd();
        }
        return super.maxValue(value);
    }

    @Override
    public JIPAddress findIp(String ip) {
        JIPAddress address = JIPAddressUtils.toIpObject(ip);
        return address != null ? findIp(address) : null;
    }

    @Override
    public JIPAddress insertIp(JIPAddress jipAddress) {
        RedBlackNode<JIPAddress> node = super.insert(jipAddress);
        return node != null ? node.getKey() : null;
    }


    @Override
    public JIPAddress insertIp(String ip) {
        JIPAddress address = JIPAddressUtils.toIpObject(ip);
        return address != null ? insertIp(address) : null;
    }


    @Override
    public boolean deleteIp(JIPAddress jipAddress) {
        if (jipAddress == null) {
            return false;
        }
        return super.remove(new RedBlackNode<>(jipAddress));
    }

    @Override
    public boolean deleteIp(String ip) {
        JIPAddress address = JIPAddressUtils.toIpObject(ip);
        return address != null ? deleteIp(address) : false;
    }

    @Override
    public long ipCount() {
        return JIPAddressUtils.calculateIpCount(all());
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof JIPAddress) {
            return search((JIPAddress) o) != null;
        }
        /*
        if (o instanceof JIPAddress) {
            return findIp((JIPAddress) o) != null;
        }
        if (o instanceof String) {
            return findIp(o.toString()) != null;
        }*/
        return false;
    }

    @Override
    public Iterator<JIPAddress> iterator() {
        List<JIPAddress> addressList = all();
        if (addressList == null) {
            addressList = Lists.newArrayList();
        }
        return addressList.iterator();
    }

    @Override
    public Object[] toArray() {
        List<JIPAddress> addressList = all();
        if (addressList == null) {
            addressList = Lists.newArrayList();
        }
        return addressList.toArray(new Object[addressList.size()]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Class<?> cla = a.getClass();
        boolean success = false;
        // 其他的暂时不支持
        if (StringUtils.equalsIgnoreCase(cla.getSimpleName(), "String[]")) {
            String[] result = (String[]) a;
            List<JIPAddress> addressList = all();
            int minSize = Math.min(size(), a.length);
            for (int i = 0; i < minSize; i++) {
                result[i] = addressList.get(i).toString();
            }
            success = true;
        } else if (StringUtils.equalsIgnoreCase(cla.getSimpleName(), "JIPAddress[]")) {
            JIPAddress[] result = (JIPAddress[]) a;
            List<JIPAddress> addressList = all();
            int minSize = Math.min(size(), a.length);
            for (int i = 0; i < minSize; i++) {
                result[i] = addressList.get(i);
            }
            success = true;
        }

        return success ? a : null;
    }

    @Override
    public boolean add(JIPAddress jipAddress) {
        return insertIp(jipAddress) != null;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof JIPAddress) {
            return deleteIp((JIPAddress) o);
        }
        if (o instanceof String) {
            return deleteIp(o.toString());
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (CollectionUtils.isNotEmpty(c)) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends JIPAddress> c) {
        if (CollectionUtils.isNotEmpty(c)) {
            for (JIPAddress o : c) {
                add(o);
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (CollectionUtils.isNotEmpty(c)) {
            for (Object o : c) {
                remove(o);
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (!CollectionUtils.isNotEmpty(c)) {
            clear();
            return true;
        }
        List<JIPAddress> addressList = all();
        if (!CollectionUtils.isNotEmpty(addressList)) {
            return true;
        }
        List<JIPAddress> dstList = Lists.newArrayList();
        for (Object o : c) {
            if (o instanceof JIPAddress) {
                dstList.add((JIPAddress) o);
            } else if (o instanceof String) {
                JIPAddress tmp = JIPAddressUtils.toIpObject(o.toString());
                if (tmp != null) {
                    dstList.add(tmp);
                }
            }
        }
        List<JIPAddress> tmpResult = JIPAddressIntersecter.intersect(addressList, dstList);
        clear();
        return addAll(tmpResult);
    }

    @Override
    public JIPAddressSet intersect(JIPAddressSet dstSet) {
        List<JIPAddress> addressList = JIPAddressIntersecter.intersect(all(), dstSet != null ? dstSet.all() : null);

        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressUtils.buildAddressSet(addressList) : null;
    }

    @Override
    public JIPAddressSet subtract(JIPAddressSet dstSet) {
        List<JIPAddress> addressList = JIPAddressSubtracter.subtract(all(), dstSet != null ? dstSet.all() : null);

        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressUtils.buildAddressSet(addressList) : null;
    }

    @Override
    public JIPAddressSet union(JIPAddressSet dstSet) {
        List<JIPAddress> addressList = JIPAddressUnioner.union(all(), dstSet != null ? dstSet.all() : null);
        
        return CollectionUtils.isNotEmpty(addressList) ? JIPAddressUtils.buildAddressSet(addressList) : null;
    }

    // 调试用
    @Override
    public void outputTree() {
        if (isEmpty()) {
            System.out.print("tree is empty");
        } else {
            System.out.print("tree size " + size());
        }
        outputTree(this.getRoot());
    }

    private void outputTree(RedBlackNode<JIPAddress> node) {
        if (isNil(node)) {
            return;
        }
        System.out.println(node.toString());
        outputTree(node.getLeft());
        outputTree(node.getRight());
    }
}
