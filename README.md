# 超级好用jip-common，同时支持IPv4&IPv6，性能简直逆天
----

# 一· 概述

<p>随着IPv6在慢慢应用普及，很多公司内部应用都需要逐渐支持IPv6。但由于之前大部分开发人员都只考虑了IPv4，导致升级支持IPv6时工作量较大，甚至会出现大量的if else的臃肿代码。</p>

<p>本ip解析模块jip-common，它对上层应用直接屏蔽IP类型以及IP段等底层概念，使得上层应用切换升级更加方便。</p>

<p>更多详情通过以下链接了解：</p>

1. gitee：[https://gitee.com/toktok](https://gitee.com/toktok)
2. blog: [http://www.easysb.cn](http://www.easysb.cn)

# 二· jip-common简介
 
  <p>jip-common不仅仅支持IPv4和IPv6，还支持多种IP格式的解析（参考下一节），可以对上层应用直接屏蔽IP的类型，甚至单个IP还是IP段，使得应用上层代码更加简洁。考虑到大部分业务都需要IP的集合操作，JIP库还提供了IP集合的操作，比如合并、交集、并集和差集功能，最大限度地满足业务的需求。</p>
 
  <p>通常，我们为了实现查找某个IP是属于哪个产品时，很多开发人员的做法就直接将IP段散列成单个IP，然后通过Map的方式一一对应起来，从而实现快速查找。这对于ip数量不是很多的情况下，没有太多的问题。倘若IP数量较大，或者说了到IPv6这个层面，那么这种方法简直就是要被开除的节奏了。为此，JIP模块基于线段树和红黑树的特性，不再散列IP段，实现了一种快速搜索查找的功能，可以大大节省内存和搜索时间。</p>
  
# 三·支持格式

<p>对于# 开头的一行，JIP会默认为注释，跳过解析。</p>
<p>IP之间的分隔符可以是  "\r\n",  " \r",  "\n", ";"四种。</p>
 
## 3.1 IP解析支持格式

* <p>IPV4支持的格式有四种，分别如下：</p>

| 格式 | 举例说明 |备注 |
| -----| ------| ------|
|单个IP格式	| <code>1.1.1.1 </code><br/><code>1.1.1.1</code> # 还可以支持注释哦 | 注释中不要带分号，会被认为多个ip的分割符号|
|ip段带掩码	|<code>1.1.1.1/24 </code><br/><code>1.1.1.1/24</code> # 还可以支持注释哦|注释中不要带分号，会被认为多个ip的分割符号|
|IP段简单起始样式 |<code>1.1.1.1-20</code><br/><code>1.1.1.1-20</code> # 还可以支持注释哦|注释中不要带分号，会被认为多个ip的分割符号|
| IP段完全起止样式| <code>1.1.1.1-1.1.1.20 </code><br/><code>1.1.1.1-1.1.1.20</code> # 还可以支持注释哦 | 注释中不要带分号，会被认为多个ip的分割符号 |
 
* <p>IPV6支持的格式有四种，分别如下：</p>

| 格式 | 举例说明 |备注 |
|-----|------|-----|
|单个IP格式	| <code>8888::226:2dff:fefa:0 </code><br/><code>8888::226:2dff:fefa:0</code> # 还可以支持注释哦 |注释中不要带分号，会被认为多个ip的分割符号 |
| ip段带掩码|<code>8888::226:2dff:fefa:0/24</code><br/><code>8888::226:2dff:fefa:0/24</code> # 还可以支持注释哦 |注释中不要带分号，会被认为多个ip的分割符号 |
| IP段简单起始样式|<code>8888::226:2dff:fefa:0-20</code><br/><code>8888::226:2dff:fefa:0-20 </code># 还可以支持注释哦|注释中不要带分号，会被认为多个ip的分割符号|
|IP段完全起止样式|<code>8888::226:2dff:fefa:0-20</code><br/><code>8888::226:2dff:fefa:0-8888::226:2dff:fefa:20</code> # 还可以支持注释哦|注释中不要带分号，会被认为多个ip的分割符号|

## 3.2 IP集合

  <p>JIP对上层提供了统一个IP接口JIPAddress，并实现了一个IP集合JIPAddressSet的功能，它的底层就是基于线段树和红黑树特性实现的，完成实现集合常规的业务操作。值得说明的是，这个集合可以同时包含上述的3.1所支持的格式，也就说是上层应用已经不用去关心该IP是IPv4还是IPv6，到底是单个IP还是IP段。</p>
 
# 四·快速使用

 <p>一般详细的说明文档，看的人会比较少，那么我就直接按照使用场景来说明JIP模块能为咱带来什么便利，一起来看吧。</p>

## 4.1 场景1： IP地址解析

* 单个IP或单个IP段

<p>解析单个IP或者IP段时，可以直接使用</p>

```
JIPAddress a1 = JIPAddressUtils.toIpObject("10.0.0.5/24 # 这是注释");
System.out.println(a1.toString()); // 10.0.0.5/24
JIPAddress a2 = JIPAddressUtils.toIpObject("10.0.0.0-255");
JIPAddress a3 = JIPAddressUtils.toIpObject("8888::226:2dff:fefa:10-8888::226:2dff:fefa:20")
```

<p>如果要想知道解析的结果是什么类型，可以调用 getIpType()函数， 它会返回一个枚举的类型，关于枚举的定义请参考源码。</p>

```
System.out.println(a1.getIpType())
```

* 多个IP或多个IP段

<p>假如要对多个IP或者多个IP段解析时，可以直接使用如下方法。</p>

```
JIPAddressSet addressSet = JIPAddressUtils.buildAddressSet(
  "101.35.129.0;101.35.132.0/24;101.35.133.0/24;101.35.134.0/24;101.35.135.0/24;");
```

<p>就可以直接构造出一个IP集合，多个IP可以用四种符号隔开[<code>\r\n, \r, \n以及分号(；)</code>]，IPv4和IPv6可以并存，在其中也可以增加注释也不会有任何问题。</p>

* 判断是否有效IP地址

```
JIPAddressUtils.isValidIPAddress("1.1.1.1")
```

## 4.2 场景2：IP地址合并
 
<p>IP地址合并，主要是为了减少IP条目数的数量，比如1.1.1.0-100，和1.1.1.100-255就可以合并成1.1.1.0/24。JIP可以提供单个IP（段）的合并，也提供了多个IP（段）的合并功能，主要由<code> JIPAddressCombiner </code> 完成。</p>
 
<p>下面以单个IP（段）合并为例</p>

```
JIPAddress jipAddress1 = JIPAddressUtils.toIpObject("1.1.1.0");
JIPAddress jipAddress2 = JIPAddressUtils.toIpObject("1.1.1.1-255");
List<JIPAddress> result = JIPAddressCombiner.combine(jipAddress1, jipAddress2); 
// 输出就是一个1.1.1.0/24
```
<p>同样的道理，多个IP(段)的合并也是类似的。</p>


## 4.3 场景3：IP地址交集

<p>IP地址的交集功能，不仅仅是求单IP(段），也可以就多个IP(段)之间的交集，该功能主要由 <code>JIPAddressIntersecter </code>完成。</p>
 
<p>以下以单个IP（段）的交集为例</p>

```
JIPAddress jipAddress1 = JIPAddressUtils.toIpObject("1.1.1.15-30");
JIPAddress jipAddress2 = JIPAddressUtils.toIpObject("1.1.1.10-20");
List<JIPAddress> result = JIPAddressIntersecter.intersect(jipAddress1, jipAddress2); 
// 输出就是一个 1.1.1.15-20
```

<p>同样的道理，多个IP(段)的交集也是类似的。</p>

```
JIPAddressSet addressSet1 = JIPAddressUtils.buildAddressSet(
  "101.35.129.0;101.35.132.0/24;101.35.133.0/24;101.35.134.0/24;101.35.135.0/24;");
JIPAddressSet addressSet2 = JIPAddressUtils.buildAddressSet("101.35.129.0;101.35.132.0/24;");
List<JIPAddress> addressList = JIPAddressIntersecter.intersect(
    addressSet1.all(), addressSet2.all()); 
```

## 4.4 场景4：IP地址并集

<p>IP地址的并集功能，不仅仅是求单IP(段），也可以就多个IP(段)之间的交集，该功能主要由 <code> JIPAddressUnioner</code> 完成。</p>
 
<p>以下以单个IP（段）的交集为例</p>

```
JIPAddress address1 = JIPAddressUtils.toIpObject("101.35.135.0/24");
JIPAddress address2 = JIPAddressUtils.toIpObject("101.35.136.0/21");
List<JIPAddress> addressList = JIPAddressUnioner.union(address1, address2);
```

<p>同样的道理，多个IP(段)的并集也是类似的。</p>

## 4.5 场景5：IP地址差集

<p>IP地址的差集功能，不仅仅是求单IP(段），也可以就多个IP(段)之间的差集，该功能主要由 <code>JIPAddressSubtracter</code>完成。</p>
 
<p>以下以单个IP（段）的交集为例</p>

```
JIPAddress address1 = JIPAddressUtils.toIpObject("101.35.135.0/24");
JIPAddress address2 = JIPAddressUtils.toIpObject("101.35.135.0/28");
List<JIPAddress> addressList = JIPAddressSubtracter.subtract(address1, address2);
```

<p>同样的道理，多个IP(段)的差集也是类似的。</p>

## 4.6 场景6：IP一一映射群组

* 查找

<p>
该场景应该是最常见的，就是一个群组包含了很多IP（段），如何快速地找到IP对应的群组。通常，我们的做法是把IP段散列成一个一个，然后放在HashMap中。这种方法的问题在于IP的数量必须在一定的范围之内。倘若有个A段，甚至有个0.0.0.0/0，估计程序就吃掉大量的内存，很有可能引起程序的崩溃。</p>
 
 <p>推荐的做法，就是使用JIP库的集合模块，可以快速构建一个占用内存很小的红黑树集合，如下：</p>

```
// 创建一个空集合
JIPAddressSet tmpJipAddressSet = JIPAddressUtils.buildEmptyAddressSet();
    for (IdcIpSegment idcIpSegment : tmpIdcIpSegmentList) {
        // 将该群组对应的ip列表，全部装载在集合中，并设置外带数据为群组即可
        // getIpLlist() 为带分隔符号的ip列表，允许ipv4和ipv6并存，比如1.1.1.0/24\n2.2.2.-100....
        // 外带数据idcIpSegement，对应的IP信息中会自动存储映射关系
     JIPAddressUtils.addIpList(tmpJipAddressSet, idcIpSegment.getIpList(), idcIpSegment);
    }
```

<p>那么如何查找IP对应的群组，比如群组中有个ip是1.1.1.0/24，那么我们就可以以下方法查询</p>

```
JIPAddress find = jipAddressSet.findIp("1.1.1.23")
```

<p>那么查询的结果find就是1.1.10/24，然后我们就可以取出它对应的群组是哪个:</p>

```
IdcIpSegment findGroup = ((IdcIpSegment) find.getData())
```

<p>是不是很简单？！</p>
 
1. 注意事项①： 如果有多个满足条件的话，findIp只返回满足条件的第一个。
2. 注意事项②： 集合转换成List，只需要调用all()方法，会通过前序方法遍历收集。

* 更新

<p>如果删除集合中的IP(段），只需要调用<code>JIPAddressSet.deleteIp()</code>即可，值得注意的是，删除是全量匹配，和查找是模糊匹配（有交集就行），比如：</p>

```
JIPAddressSet tmpJipAddressSet = JIPAddressUtils.buildEmptyAddressSet();
JIPAddressUtils.addIpList(tmpJipAddressSet,“1.1.1.0/24”, idcIpSegment);
tmpJipAddressSet.deleteIp("1.1.1.0"); // 无法删除
tmpJipAddressSet.deleteIp("1.1.1.0/24"); // 可以删除
tmpJipAddressSet.deleteIp("1.1.1.0-255"); // 可以删除
```

## 4.7 场景7：检测添加的IP是否有效

* 有效检测

<p>再添加IP群组的时候，通常会去检测输入的IP地址是否有效，JIP库提供了一个简单的检测分析类，它碰到第一个错误就会停止解析，并且返回错误信息：</p>

```
String check = JIPAddressCheckUtils.checkConvertIpObject(addressList, departmentIPSegment.getIpList());
if (StringUtils.isNotBlank(check)) { // 如果有错误就会显示出来
    return check;
}
```

* 冲突检测

<p>在更新IP群组的时候，需要检测更新添加会不会和现有的冲突，常用的方法是：</p>

1. 对于所有群组构建一个大的集合树T，参考4.6。

2. 对更新群组中的每个IP（段），都去集合树T中查找是否已经存在于别的群组中。

## 4.8 场景8：格式化输出

<p>格式化输出只影响ip段的格式输出，而对于单个ip则不受影响，永远只输出单个ip的样式。</p>

* 格式化

```
JIPAddress address = JIPAddressUtils.toIpObject("1.2.3.0-255");
System.out.println(address.toString());   // ===> 1.2.3.0-255
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_WITH_MASK_FIRST)); // ==> 1.2.3.0/24
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_SIMPLE_FIRST)); // ===> 1.2.3.0-255
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_FULL_FIRST)); // 1.2.3.0-1.2.3.255
```

<p>格式化化输出，是只优先使用哪种格式输出，倘若无法按照指定样式输出，则自动回退使用本身的格式。比如 <code>1.2.3.1-3</code> ，这个ip段无法使用带掩码的形式输出，所以即便指定了mask格式，也是无效的，比如：</p>

```
JIPAddress address = JIPAddressUtils.toIpObject("1.2.3.1-3");
System.out.println(address.toString()); // ====> 1.2.3.1-3
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_WITH_MASK_FIRST)); // ====> 1.2.3.1-3
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_SIMPLE_FIRST)); // ====> 1.2.3.1-3
System.out.println(JIPAddressUtils.toIpListString(address, IPFormatType.SEGMENT_FULL_FIRST)); // ====> 1.2.3.1-1.2.3.3
```
 

## 4.9 场景9：展开ip段

<p>倘若需要将IP段散列展开成一个一个的ip，那么常用的方法是 JIPAddressUtils.expandIpList，如下</p>

```
List<JIPAddress> ipList = JIPAddressUtils.expandIpList(
    JIPAddressUtils.toIpObject("1.1.1.1/24"), 10);
```

<p>内部通过不断调用iterator枚举，最终得到的就是前10个单IP（第二个参数表示散列出来最大ip数量，0表示没有限制）。</p>

<p>除了针对一个IP段，也可以对单个ip和多个IP（段）进行展开散列，如下就是获取所有的散列单个ip：</p>

```
List<JIPAddress> ipList = JIPAddressUtils.expandIpList(JIPAddressUtils.buildAddressSet("1.1.1.1/30\n2.2.2.2/30"), 0);
```


# 五· 参与贡献

1. Fork 本仓库
2. 在我的博客留言: [http://www.easysb.cn](http://www.easysb.cn) 


 