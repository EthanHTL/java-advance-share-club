## HTTP caching
http 缓存能够提高web应用的性能,http 缓存完全根据cache-control进行解析,因此,条件性请求头(例如 Last-Modified 以及 ETag). Cache-Control 建议私有（例如，浏览器）和公共（例如，代理）缓存如何缓存和重用响应,一个ETag请求头将用于确保一个条件请求何时返回304(NOT_MODIFIED)响应(不需要响应体)-如果内容未改变,ETag可以看作是 Last-Modified 标头的更复杂的继承者
#### CacheControl
CacheControl 提供了配置Cache-Control相关的配置:
* WebContentInterceptor

* WebContentGenerator

* Controllers

* Static Resources \
[RFC 7234](https://tools.ietf.org/html/rfc7234#section-5.2.2)描述了所有对于Cache-Control响应头相关的指令,CacheControl类型使用了一种面向案例的方式侧重于常见场景:
```java
// Cache for an hour - "Cache-Control: max-age=3600"
CacheControl ccCacheOneHour = CacheControl.maxAge(1, TimeUnit.HOURS);

// Prevent caching - "Cache-Control: no-store"
CacheControl ccNoStore = CacheControl.noStore();

// Cache for ten days in public and private caches,
// public caches should not transform the response
// "Cache-Control: max-age=864000, public, no-transform"
CacheControl ccCustom = CacheControl.maxAge(10, TimeUnit.DAYS).noTransform().cachePublic(); 
```
WebContentGenerator支持接受一个类似的cachePeriod属性(以秒为单位):
* A -1 标识不生成Cache-Control响应头
* A 0 标识不缓存,生成'Cache-Control: no-store'指令
* An n > 0标识对指定的缓存进行n秒缓存(使用'Cache-Control: max-age=n')指令
####  控制器
控制器能够显式支持http 缓存,我们也推荐这样做,因此lastModified或者ETag值(对于资源)需要进行计算(在比较条件请求头之前),控制器能够增加一个ETag响应头以及Cache-Control(设置到ResponseEntity中):
```java
@GetMapping("/book/{id}")
public ResponseEntity<Book> showBook(@PathVariable Long id) {

    Book book = findBook(id);
    String version = book.getVersion();

    return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
            .eTag(version) // lastModified is also available
            .body(book);
}
```
上面的例子将发送一个304(Not_Modified) 响应(如果比较请求头一致,表示内容没有改变),否则"ETag"以及"Cache-Control" 请求头会增加到响应头中 \
你能够在控制器中检测请求头,例如:
```java

@RequestMapping
public String myHandleMethod(WebRequest request, Model model) {

    long eTag = ... 

    if (request.checkNotModified(eTag)) {
        return null; 
    }

    model.addAttribute(...); 
    return "myViewName";
}
```
* 计算etag(根据应用设置)
* 比较(如果没有修改返回null)
* 继续请求响应处理 \
这有三个变种去针对eTag,lastModified值进行条件检测,对于其他GET、HEAD请求来说,你也能够设置304响应,对于POST,PUT,DELETE,能够设置为412(PRECONDITION_FAILED),阻止并发修改;
#### 静态资源
手动设置cacheControl即可;
#### ETag 过滤器
能够使用ShallowEtagHeaderFilter 去增加一个"shallow" eTag值(根据响应内容进行计算),节省了带宽但是增加了cpu时间;
