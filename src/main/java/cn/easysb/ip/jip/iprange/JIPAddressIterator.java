package cn.easysb.ip.jip.iprange;

import cn.easysb.ip.jip.JIPAddress;
import cn.easysb.ip.jip.JIPAddressRange;
import cn.easysb.ip.jip.JIPAddressUtils;
import cn.easysb.ip.jip.ip.JIPv4Address;
import cn.easysb.ip.jip.ip.JIPv6Address;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;

/**
 * Created by jekkay on 2019/1/11.
 */
@Slf4j
public class JIPAddressIterator implements Iterator<JIPAddress> {
    private JIPAddress next;
    private boolean pop;
    private JIPAddress end;

    public JIPAddressIterator(JIPAddress next, JIPAddress end) {
        if (next == null || end == null) {
            throw new IllegalArgumentException("invalid values");
        }
        // 复制一份
        this.next = JIPAddressUtils.toIpObject(next.toString()) ;
        this.end = JIPAddressUtils.toIpObject(end.toString());
        this.pop = false;
    }

    @Override
    public boolean hasNext() {
        if (pop) {
            next = calculateNext();
            pop = false;
        }
        return next != null;
    }

    @Override
    public JIPAddress next() {
        if (!hasNext()) {
            return null;
        }
        pop = true;
        return next;
    }

    private JIPAddress calculateNext() {
        try {
            if (next == null || next.compareTo(end) >= 0) {
                return null;
            }
            if (next instanceof JIPAddressRange) {
                throw new Exception("no support yet!");
            } else if (next instanceof JIPAddress) {
                if (next instanceof JIPv4Address) {
                    return JIPAddressUtils.toIpObject(((JIPv4Address)next).getIpAddress() + 1);
                } else if (next instanceof JIPv6Address) {
                    return JIPAddressUtils.toIpObject(((JIPv6Address)next).getAddress().add(1).toString());
                } else {
                    throw new Exception("unSupport type " + next.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.error("calculateNext", e);
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}
