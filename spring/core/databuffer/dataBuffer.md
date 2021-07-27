## Data Buffer
Java NIO 提供了 ByteBuffer，但许多库在此基础上构建了自己的字节缓冲区 API，特别是对于重用缓冲区和/或使用直接缓冲区有利于提高性能的网络操作。例如，Netty 具有 ByteBuf 层次结构，Undertow 使用 XNIO，Jetty 使用带有回调的池化字节缓冲区，等等。 spring-core 模块提供了一组抽象来处理各种字节缓冲区 API，如下所示：
* DataBufferFactory 创建一个数据buffer的抽象
* DataBuffer 表示一个字节buffer,他也可能被池化
* DataBufferUtils 提供了数据buffer的一些工具方法
* Codecs 解码和编码流数据buffer 流到高可用对象
##### DataBufferFactory
创建data buffer: 
* 分配一个新的data buffer,可以指定容量,即使 DataBuffer 的实现可以按需增长和缩小，这也会更有效
* 包装一个已经存在byte[] 或者java.nio.ByteBuffer,它能够装饰一个给定DataBuffer实现的数据并且不执行任何分配 \
请注意，WebFlux 应用程序不会直接创建 DataBufferFactory，而是通过客户端的 ServerHttpResponse 或 ClientHttpRequest 访问它。工厂的类型取决于底层客户端或服务器，例如Reactor Netty 的 NettyDataBufferFactory，其他的 DefaultDataBufferFactory
#### DataBuffer
此DataBuffer接口提供了类似于java.nio.ByteBuffer的操作,但是带来了一些好处(受Netty ByteBuf的启发):
* 读取和写入具有独立的位置，即不需要调用 flip() 来在读取和写入之间交替。
* 与 java.lang.StringBuilder 一样按需扩展容量
* 通过 PooledDataBuffer 池化缓冲区和引用计数
* 将缓冲区视为 java.nio.ByteBuffer、InputStream 或 OutputStream
* 给定字节的index判断或者最后一个索引判断

#### PooledDataBuffer
如 ByteBuffer 的 Javadoc 中所述，字节缓冲区可以是直接的或非直接的。直接缓冲区可能驻留在 Java 堆之外，这消除了对本地 I/O 操作进行复制的需要(这就是直接IO(零拷贝操作)),这使得直接缓冲区对于通过套接字接收和发送数据特别有用，但它们的创建和释放成本也更高，这导致了缓冲池的想法 \
PooledDataBuffer 是 DataBuffer 的扩展，它有助于引用计数，这对于字节缓冲池至关重要。它是如何工作的？当 PooledDataBuffer 被分配时，引用计数为 1。调用 retain() 增加计数，而调用 release() 减少它。只要计数大于 0，就保证不会释放缓冲区。当计数减少到 0 时，可以释放缓冲池，这在实践中可能意味着为缓冲区保留的内存返回到内存池。 \
请注意，与其直接操作 PooledDataBuffer，在大多数情况下，最好使用 DataBufferUtils 中的便利方法，这些方法仅在 DataBuffer 是 PooledDataBuffer 的实例时才将释放或保留应用于该 DataBuffer
#### DataBufferUtils
* 将数据缓冲区流连接到可能具有零拷贝的单个缓冲区中，例如通过复合缓冲区，如果底层字节缓冲区 API 支持
* 将 InputStream 或 NIO Channel 转换为 Flux<DataBuffer>，反之亦然，将 Publisher<DataBuffer> 转换为 OutputStream 或 NIO Channel。
* 如果缓冲区是 PooledDataBuffer 的实例，则释放或保留 DataBuffer 的方法
* 跳过或从字节流中取出，直到达到特定的字节数
#### Codecs
* Encoder 编码 Publisher<T>到一个数据buffer的流
* Decoder 解码Publisher<DataBuffer> 到一个高可用对象的流中 \
spring-core 模块提供了byte[],ByteBuffer,DataBuffer,Resource以及String 编码、解码器实现,这个Spring-web 模块增加了Jackson Json,Jackson Smile,JaXB2,Protocol Buffer 以及其他编码器 以及解码器(查看webFlux部分的Codecs)
#### 使用DataBuffer
使用数据缓冲区时，必须特别小心以确保缓冲区被释放，因为它们可能被池化,我们将使用编解码器来说明它是如何工作的，但这些概念更广泛地适用。让我们看看编解码器必须在内部做什么来管理数据缓冲区 \
在创建更高级别的对象之前，解码器是最后读取输入数据缓冲区的，因此它必须按如下方式释放它们:
* 如果解码器只是读取每个输入缓冲区并准备立即释放它，它可以通过 DataBufferUtils.release(dataBuffer) 来实现
* 如果解码器正在使用 Flux 或 Mono 运算符，例如 flatMap、reduce 和其他在内部预取和缓存数据项的运算符，或者正在使用诸如 filter、skip 和其他省略项目的运算符，则 doOnDiscard(PooledDataBuffer.class, DataBufferUtils ::release) 必须添加到组合链中以确保这些缓冲区在被丢弃之前被释放，也可能作为一个错误或取消信号的结果。 \
* 如果解码器以任何其他方式保留一个或多个数据缓冲区，则必须确保它们在完全读取时被释放，或者在缓存数据缓冲区被读取和释放之前发生错误或取消信号的情况下 \
请注意，DataBufferUtils#join 提供了一种安全有效的方式将数据缓冲区流聚合到单个数据缓冲区中 \
编码器分配能够被其他人读取（和释放）的数据缓冲区。所以编码器没有什么可做的。然而，如果在用数据填充缓冲区时发生序列化错误，编码器必须小心释放数据缓冲区。例如
```java
DataBuffer buffer = factory.allocateBuffer();
boolean release = true;
try {
    // serialize and populate buffer..
    release = false;
}
finally {
    if (release) {
        DataBufferUtils.release(buffer);
    }
}
return buffer;
```
Encoder 的消费者负责释放它接收到的数据缓冲区。在 WebFlux 应用程序中，编码器的输出用于写入 HTTP 服务器响应或客户端 HTTP 请求，在这种情况下释放数据缓冲区是写入服务器响应或客户端的代码的责任要求 \
请注意，在 Netty 上运行时，有用于[排除缓冲区泄漏](https://github.com/netty/netty/wiki/Reference-counted-objects#troubleshooting-buffer-leaks)的调试选项