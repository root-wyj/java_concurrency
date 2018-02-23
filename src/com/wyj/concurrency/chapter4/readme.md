# chapter4 组合对象

[TOC]

### 测试例子

##### Test1

`为了验证存储在静态公共区域里面的状态 也是线程不安全的`

对静态变量a开启3000线程，每个线程里面循环10次 执行a++操作，如果**线程安全，结果是 30000**，**线程不安全，结果 小于 30000**
最后的结果是 `29969`。出现了31次线程冲突。

<br>

##### Test2 -- 机动车追踪器

`问题描述：`
每一辆机动车都有一个String标识，并有一个对应的位置(x,y)。对象VehicleTracker封装了这些信息。**视图线程**和**多个更新线程**可能会共享数据模型。视图线程会获取机动车的名称和位置，将他们显示在显示器上。

1. 包`test2.a`中，演示了基于Java监视器模式(对象封装了所有状态，并由对象自己的内部锁保护)的同步方式。该例子用这种方式实现有这么几个问题，`getLocations`方法也是同步的，这时候会阻塞很多的update操作，更重要的是，`getLocation`方法也被阻塞了，会有很严重的用户体验上的问题。而且类`MutablePoint`是非线程安全的（其实仔细想想这里并没有什么问题）.

2. https://www.cnblogs.com/jxldjsn/p/6115764.html




> `tips`--`Collections.synchronizedList(list:List)` 通过该静态方法，重新返回一个经过`SynchronizedList`装饰的List，这样所有List的操作，都会被`SynchronizedList`中的同步方法同步，做到线程安全。

> `tips` -- `Collections.unmodifiableMap(locations)` 通过该静态方法，重新返回一个经过 `UnmodifiableMap`装饰的Map，内部只是创建了一个新的指向原空间的指针，并且隔离了map的增删操作，所以通过该方法返回的map是绝对不会被修改的。（但是，如果这个map还有其他指针，那么该map的修改也会体现在unmodify方法返回的map指针中）下面给出示例：

```java
	public static void main(String[] args) {
		Map<String, String> map1 = new HashMap<>();
		map1.put("1", "haha");
		System.out.println("map1:"+map1);
		// Map<String, String> map2 = Collections.unmodifiableMap(map1); //注释1
		// Map<String, String> map2 = Collections.unmodifiableMap(new HashMap(map1)); //注释2
		System.out.println("map2:"+map2);
		map1.put("2", "xixi");
		System.out.println("map1:"+map1);
		System.out.println("map2:"+map2);
	}

//输出 打开注释1
map1:{1=haha}
map2:{1=haha}
map1:{1=haha, 2=xixi}
map2:{1=haha, 2=xixi}
//打开注释2
map1:{1=haha}
map2:{1=haha}
map1:{1=haha, 2=xixi}
map2:{1=haha}
```


为什么List、Map这么复杂的结构，但是这么简单就做到了从线程不安全到线程安全的转变？ 因为系统对List系列的良好封装，只暴露了List接口，扩展修改都非常方便，不会影响到内部的其他类。
