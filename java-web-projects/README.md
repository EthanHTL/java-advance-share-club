# tomcat 多个应用程序共享同一个 jvm

能够发现在tomcat webapps目录下面部署的应用使用的是同一个jvm ..

那么也就是真的会存在例如jvm 级别上的缓存(在 jdk 1.6及其以下,Introspector 使用jvm级别的缓存,这将导致 例如一个应用退出的情况下, 它所使用的 bean info 缓存)
将会被一直驻留在jvm的缓存中,这将导致内存泄露 ..

具体可以查看 spring的  CachedIntrospectionResults 了解更多信息 ..

当然这两个示例只证明了,它们可以使用共享类加载器,并没有能够直接获取到另一个应用的类 ..

原因是 对tomcat的了解不够深入, 暂留 ..