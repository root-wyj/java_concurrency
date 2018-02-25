# Building Block

该章主要讲构建同步容器、并发容器所遇到的问题。

## 并发容器

我们都知道Vector是线程安全的，继承了List接口，拥有List的所有方法。但是，如果我们用到了一些复合操作，比如说线程池中用来存放线程，但是线程池是有大小的，add操作的时候需要先通过size方法获取长度，如果没有超过最大长度，则添加，这时候，添加操作就变成了复合操作，在多线程的时候就会出现竞争，导致数据不一致。

大致的操作可以有，迭代、导航（根据一定的顺序寻找下一个元素）、条件运算（如，缺少就加入，检查再运行等）。

### 自己构建并发容器

简单点，可以通过下列方式加锁：
```java
public static Object getLast(Vector list) {
    synchronized(list) {
        int lastIndex = list.size() - 1;
        return list.get(lastIndex);
    }
}

public static void traverse(Vector list, Hock h) {
    synchronized(list) {
        for (int i=0; i<list.size(); i++){
            h.doSomething(list.get(i));
        }
    }
}
```

> 注意：这里的加的同步锁一定要是`list`，而不能是类似`this`这样的类，因为`Vector`内部通过的时候，使用的锁就是本身，如果这里加的锁和`Vector`中使用的不是一个锁，那么还是不能保证线程安全。

#### 迭代器 与 ConcurrentModificationException

对Collection的标准迭代方式是`Iterator`，不管是显示的用`collection.iterator()`还是隐式的用`foreach`，都是使用的迭代器方式来遍历的。

下面这种方式，并没有使用迭代器，所以也不会抛出异常：
```java
for (int i=0; i<list.size(); i++){
    if (i % 2 == 0) {
        list.remove(i);
    }
}
```


> 也要注意方法中隐藏的使用了迭代器，如toString、hasCode、equals、containsAll、removeAll、retainAll方法，或者把容器作为构造函数的参数，都会对容器进行迭代。

当容器正在使用迭代器的时候，容器被修改，那么就会抛出`ConcurrentModificationException`异常。容器被修改包括单线程的在**迭代过程中修改容器**（如Test1.test1）；也包括其他线程对容器的修改。Java中，在设计同步容器返回的迭代器时，并没有考虑到并发修改的问题，也是会抛出该异常的。

怎么解决这个问题呢，简单点，就是在迭代期间对容器加锁，但是，当容器比较大的时候，又比较影响性能。替代方法是复制容器（复制期间，也需要加锁），但是这样也有内存上的开销。

### java中的并发容器

java提供了同步容器，如`Collections.synchronizedList(list)`返回的`SynchronizedList`(该类是内部包类)等。

另外，java还提供了复合操作的并发容器，如`ConcurrentHashMap`, `CopyOnWriteArrayList`, 传统的FIFO队列`ConcurrentLinkedQueue`, 用来代替SortedSet 和 SortedMao 的`ConcurrentSkipListSet`和`ConcurrentSkipListMap`等等。

#### ConcurrentHashMap

通常的同步容器类，比如`Collection.synchronizedMap`返回的类，每个操作的执行期间都会持有一个锁，有一些耗时操作，比如contains，equals等，如果该map中的元素的hashcode并没有很好的分布，最极端的就是这个map其实是一个链表，遍历所有的元素并调用equals方法，会花费很长的时间，而在这期间，其他线程都不能访问这个容器。

`ConcurrentHashMap`一样是一个哈希表，但是使用了完全不同的锁策略，可以通过更好的并发性和可伸缩性。他使用了更加细化的锁机制，叫**`分离锁`**。这个机制允许更深层次的共享访问。**任意数量的读线程可以并发访问map，读者和写者也可以并发访问map，并且有限数量的写线程还可以并发修改map**。

而且，**提供了不会抛出ConcurrentModificationException的迭代器，不需要再容器迭代中加锁。ConcurrentHashMap返回的迭代器具有`弱一致性`。**
> **弱一致性的迭代器**，容许并发修改，当迭代器被创建时(xxx.iterator())，会遍历以后的元素，并且可以（但是不保证）感应到在迭代器被创建后，对容器的修改。

缺点，一些对整个map的操作，如size、isEmpty，它们的语义在反应容器并发特性上并弱化了。因为size的结果相对于在计算的时刻可能已经过期了，它仅仅是个估算值。而且，像size、isEmpty这样的方法在并发环境下几乎没有什么用处，因为map一直是运动的。所以对这些操作的需求就被弱化了，并且对重要的操作进行了性能调优，包括get、put、containsKey、remove等

缺点，同步map比如`Collection.synchronizedMap`返回的类，提供的一个特性是独占访问锁（我访问的时候，其他任何人都不能访问），在`ConcurrentHashMap`中并没有实现。但是他的其他任何方面都远比同步map有巨大优势，所以只有程序需要独占访问时，`ConcurrentHashMap`才无法胜任。

也因为`ConcurrentHashMap`不能够独占访问，所以也不适合为其创建新的原子复合操作。比如之前说道的`Vector`的缺少即加入等操作，而且`ConcurrentHashMap`也提供了常见的复合操作。

#### CopyOnWriteArrayList

`CopyOnWriteArrayList` 是同步List的一个并发替代品，**通常情况下提供了更好的并发性，并避免了在迭代期间对容器加锁和复制**

> `CopyOnWriteArraySet` 是同步Set的一个并发替代品。

**写入时复制（copy）**，容器的线程安全性来源于此，只有有效的不可变对象被正确发布，那么访问将不再需要更多的同步（读）。每次修改时，会创建并重新发布一个新的容器拷贝，以此来实现可变性（写，可以想想Elixir）。

写入时复制的容器，会保留一个底层基础数组的引用。这个数组作为迭代器的起点，永远不会被修改，`因此对他的同步只是为了确保数组内容的可见性（及时发布？）`。因此多个线程可以对这个容器进行迭代，而且不会受到另一个或多个想要修改的线程带啦IDE干涉。返回的迭代器也不会抛出`ConcurrentModificationException`，而且`返回的元素严格与迭代器创建时相一致，不会烤炉后续的修改`。

显而易见，每次修改容器是复制基础数组需要一定的开销，特别是容器比较大的时候。所以也需要我们权衡。

### 阻塞队列 和 生产者-消费者模式

`阻塞队列 (BlockingQueue)`-- 增加了可阻塞的插入和获取操作。如果队列是空的，一个获取操作会一直阻塞到队列中存在可用的元素，如果队列是满的（对于有界队列），插入操作会一直阻塞到队列中存在可用空间。

阻塞队列支持`生产者消费者模式`。一个生产者-消费者设计，分离了派发任务者、消费任务者和任务之间的耦合关系，当有新的任务产生是，生产者就将任务发布到消息队列中，不需要管理任务是否被处理还是怎么了，而消息队列仅仅是接收和分发任务，不关系谁给的也不关系给谁，而消费者仅仅是从任务队列中拿到并处理任务，没有任务的时候，自己就歇着。这种设计模式围绕着阻塞队列展开。

java类库中有很多`BlockingQueue`的实现。
- `LinkedBlockingQueue`和`ArrayBlockingQueue`是FIFO队列。一个是链表的实现方式 ，一个是数组的实现方式。
- `LinkedBlockingDeque` 是双向链表，可在头部尾部操作。
- `PriorityBlockQueue` 是一个按优先级排序的队列（如果不希望是FIFO的）。可以按照元素的自然顺序（如实现了`Comparable`的类），也可以使用`Comparator`进行排序。
- `SynchronousQueue` 可以说不是一个真正意义上的队列。因为它不会为队列元素维护任何存储空间。和其他的相比，就像是吧文件直接递给你的同事，还是把文件直接发送到他的邮箱期待他一会可以得到文件之间的不同。`SynchronousQueue`没有存储能力，除非有另外一个消费者线程已经准备好处理他了，否则put和take会一直阻塞。这种队列只有在消费者充足的时候比较合适。

`连续的线程限制`：`java.util.concurrent`包中的阻塞队列，全部都包含充分的内部同步，从而能安全的将对象从生产者线程发布至消费者线程。生产者-消费者模式和阻塞队列一起，为生产者和消费者之间移交的对象所有权提供了**连续的线程限制**。在整个生产消费过程当中，对象完全由单一线程所拥有，这个线程可以任意修改，因为它具有独占访问权。

对象池扩展了连续的线程限制。
