# X.509 Authentication
X.509证书的认证大多数常见的使用 是使用SSL验证服务器的身份 .. 最普遍的是从浏览器使用HTTPS .. 浏览器会自动检查服务器提供的证书是否由其维护的受信任证书颁发机构列表之一颁发（数字签名）。

你也能够使用SSL 进行相互认证 .. 这个服务器请求一个来自客户端的有效证书(作为SSL 握手的一部分) .. 服务器认证客户端(通过检查它的证书是有一个可信任机构签名的) ..
如果一个有效的证书已经提供,它能够在应用中通过servlet api获取 .. spring security x.509模块通过使用过滤器抓取这个证书 ... 它映射证书到一个应用用户 ..
并加载用户的授权的权限集合 与标准的spring security 基础设施使用 ..

你能够使用SSL 进行相互认证(双向认证),这个服务器将会从客户端请求有效的证书(作为SSL握手的一部分) .. 例如,如果我们使用tomcat,我们应该阅读[tomcat ssl 指令](https://tomcat.apache.org/tomcat-9.0-doc/ssl-howto.html) ..
你应该在尝试与spring security之前先尝试让它工作 ..

## 增加X.509 认证到你的Web应用中
启用X.509客户端认证是非常直接了当的,为了这样做,增加<x509 /> 元素到你的http security 命名空间配置中
```xml
<http>
...
	<x509 subject-principal-regex="CN=(.*?)," user-service-ref="userService"/>;
</http>
```
这个元素有两个可选属性:
- subject-principal-regex
    
    普通的表达式被用来从证书的subject 名称中抓取用户名 .. 默认值是前面所列出的示例值 .. 这个抓取的名称将被用来传递到UserDetailsService加载用户的权限 ..
- user-service-ref
    
    表示被使用的UserDetailsService的bean id(将会被X509 所使用的),它的默认值是根据 类型从容器中抓取一个(意思是如果容器中只有一个) ..

subject-principal-regex 应该包含单个组,例如默认表达式(CN=(.*?)) 匹配常见的名称字段 .. 因此如果在证书的subject的名称是`CN=jimi Hendrix,OU=...`,
那么用户名称是Jimi Hendrix,匹配是大小写无感知的.. 因此`emailAddress=(.*?),` 将匹配 `EMAILADDRESS=jimi@hendrix.org,CN=..` ,那么用户名是
`jimi@hendrix.org`,如果客户端提供了一个证书以及一个有效的用户名被抓取,那么应该有一个有效的Authentication 对象将出现在security Context中 ..

如果证书发现但是没有相关的用户发现,那么security context仍然是空的 .. 这意味着你仍然能够使用X.509认证以及其他选项(例如基于表单的登录) ...


## 在Tomcat中设置SSL
这里有一些在spring security示例仓库中生成好的证书,你能够使用这些来启用SSL 测试(如果你不想要自己生成).. `server.jks` 文件中包含了服务端证书 ..
以及私钥 以及颁发机构证书 .. 这里也有一些客户端证书文件(为用户所准备的) - 包含在示例应用的 .. 你能够安装这些在你的浏览器中启用SSL 客户端认证 ..

为了运行具有SSL支持的tomcat,将`server.jks` 文件放入 tomcat `conf` 目录中并在`server.xml` 中增加以下的connector ..

```xml
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" scheme="https" secure="true"
			clientAuth="true" sslProtocol="TLS"
			keystoreFile="${catalina.home}/conf/server.jks"
			keystoreType="JKS" keystorePass="password"
			truststoreFile="${catalina.home}/conf/server.jks"
			truststoreType="JKS" truststorePass="password"
/>
```
`clientAuth` 能够设置为 `want`,如果你仍然想要使用SSL 连接成功(即使客户端没有提供证书) .. Clients 如果没有提供一个证书那么不能访问由spring security保护的任何事物 ..
除非你使用了一个非X.509认证机制,例如表单认证登录 ..








