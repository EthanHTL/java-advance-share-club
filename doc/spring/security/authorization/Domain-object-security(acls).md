# 域对象安全(acls)

这部分描述了spring security 怎样提供了域对象安全和访问控制列表(acl)

复杂的应用经常定义除了web请求和方法执行级别的访问权限 .除此之外, 安全决定需要包含
(谁是Authentication), 这是MethodInvocation,以及哪一个SomeDomainObject ...

换句话说，授权决策还需要考虑方法调用的实际域对象实例主体。

想象你正在设计一个应用为了 宠物诊所, 这里有对于基于spring应用的用户的两个分组: 医生和顾客 ..

医生应该可以访问所有数据,然而客户应该只能看见它自己的记录 ..

为了让它变得稍微更加有趣, 你的顾客能够让其他人看见他们顾客的记录 ..

例如他们的(小狗幼儿园或者本地的小马俱乐部的主席) ..

当你使用spring security 作为根基,你有各种可能的方式:

- 编写业务方法强制安全 .. 你能够考虑在Customer 域对象实例中的集合实例去决定那些用户可以访问 .. 通过使用SecurityContextHolder.getContext().getAuthentication(),
你能够访问Authentication 对象 ..
- 编写一个AccessDecisionVoter 去强制根据存储在Authentication 对象中的GrantedAuthority[] 实例去强制安全 ..
这意味着你的AuthenticationManager 需要使用自定义的GrantedAuthority[] 对象填充Authentication 来代表每一个Customer 域对象实例去指示哪一个身份(委托人)可以访问 ..

- 编写一个 AccessDecisionVoter 去强制 安全并直接开放目标 Customer 域对象 .. 这意味着你的voter 需要访问Dao 并让它抓取Customer 对象 ..
它能够访问赞成用户的Customer对象集合 并做出合适的决定 ..


每一个方式都是非常的合理, 然而第一种耦合了你的授权到业务代码, 主要问题是它包含了增加了单元测试的复杂性 ..  并且事实上它可能会很难重用Customer 授权逻辑(在其他地方) ..

从Authentication对象中获取GrantedAuthority[] 实例 是非常合适的, 但是无法缩放到大量的Customer 对象上 ..

如果一个用户能够访问 5000 Customer 对象(不可能有这样的情况,但是想象如何对于大型的小马俱乐部的受欢迎的兽医) 这么这个内存的消耗 以及需要构造Authentication对象的时间 可能是不理想的 ..

这最后的方式,直接在外部代码中开放Customer,这可能是最好的(这三种中). 它实现了概念分离并且不会滥用内存或者cpu 周期 ..

但是 这仍然是不高效的(在AccessDecisionVoter 以及 最终业务方法它自己 都执行一个对Dao的调用 - 负责抓取Customer对象), 每个方法调用两次访问显然是不可取的。

除此之外, 此外，对于列出的每种方法，您需要从头开始编写自己的访问控制列表 (ACL) 持久性和业务逻辑。

幸运的是,这里有替代方法 ..

## 关键概念
spring security的acl 服务放置在 `spring-security-acl-xxx.jar`中, 你需要增加这个jar到类路径中去使用spring security的域对象实例安全能力 ..

spring security的域对象实例能力重心是 访问控制列表(acl)的概念 .. 在系统中的每一个域对象实例有自己的acl, 并且 acl 记录了谁能够或者不能够与域对象工作的详细 ..
使用这种需要记住,spring security 为应用提供了三种主要的acl相关的能力 ..

- 一种能够有效的抓取acl 实体(针对所有域对象 并且能够修改那些acl)的方式
- 一种能够确保给定的委托人能够允许和这些对象进行工作(在方法调用之前) ..
- 一种能够确保给定的委托人允许与这些对象工作(在方法调用之后)

第一个要点是: spring security acl模块的主要能力之一是 提供一个抓取acl的高性能的方式..
这个acl仓库能力是非常重要的,因为在系统中的每一个域对象实例可能有各种访问控制项 ..

并且每一个acl 可能继承来自其他的acl(处于一个类似于树的结构) - 这是由spring security 支持的,并且非常普遍的使用 ..
spring security acl 能力已经十分小心的设计去提供acl的高性能抓取 .. 集合可插拔的缓存, 最小化死锁数据库更新,与orm 框架的独立(直接使用 jdbc),
适当的包装(封装)  以及透明的数据库更新 ..

给定的数据库的是acl模块的操作的核心, 我们需要暴露4张在默认实现中主要使用的表 ..
按照在一个典型的Spring Security ACL部署中，这些表格按大小顺序列出,具有最多行的表最后列出:
- `ACL_SID` 让我们独一无二的表示任何一个主体或者在系统中的权限(`SID` 是Security IDentity的标准) ..
它仅有的字段是ID,是SID的文本呈现, 并且有一个标志指示是否这个文本呈现指的是 主体名称 或者GrantedAuthority ...
那就是对于一个独一无二的实体或者GrantedAuthority 都有一个单独的行,当我们使用在抓取权限的上下文中,SID通常叫做 `recipient`(接受者)
- `ACL_CLASS` 让我们唯一标识在系统中的任何域对象类. 这里仅仅只有一个ID 和 Java class名称的列 .. 因此对于每一个唯一的类(希望存储acl权限)都有一个单行 ..
- `ACL_OBJECT_IDENTITY` 存储了每一个在系统中唯一域对象实例的信息,列包括ID,一个有关ACL_CLASS表的外键 ..
唯一的标识符能够让我们知道ACL_CLASS 实例,它的父母(也就是ACL_CLASS) 也包含了一个对ACL_SID 表的外键呈现了域对象实例的拥有者 ..
,此表并且是否允许acl 项去继承任何父亲acl .. 针对每一个域对象实例(为此存储acl 权限)都有一个单独的行..
- 最终,`ACL_ENTRY`  存储了分配给每一个接受者的 单独的权限列表(列包括了对ACL_OBJECT_IDENTITY表的外键), 接受者(例如,对ACL_SID的外键),无论我们是否审查, 有一个字节掩码呈现实际被授予或者拒绝的权限 ..
针对每一个接受者都有一个单独的行 能够接受一个与域对象工作的权限 ... \
总的来说,这是一个权限表,它是域对象实例和 接受者的关联关系的权限展示 ..


正如上一个段落提到,acl系统使用整形字节掩码, 然而你不需要知道在acl系统中使用的字节的详细内部细节 ..
这里正如有32位可以开启或者关闭 对它来说已经足够, 这里的每一个字节都呈现了一个权限 .. 默认来说,读权限是(0),写是(1),创建是(2),删除是(3),管理者是(4) ..

你能够实现自己的`Permission` 实例(如果你希望使用其他权限,acl框架操作的其余部分不需要知道你的扩展) ..

你应该理解在你系统中的域对象的数量 与 这里使用整形字节掩码没有任何关系 ..

虽然只有32位可用的权限时,你可能拥有大量的域对象实例(在ACL_OBJECT_IDENTITY中可能存在上十亿行,并且,可能 ACL_ENTRY同样也是如此). 
我们提出这一点是因为我们发现人们有时会错误的相信他们需要为每一个潜在的域对象分配一个bit, 但是这并非如此 ..

现在我们提供了ACL系统所做的一些基本概述,并且我们将在表级别上查看它们,我们需要探索一些关键接口:

- Acl

    每一个域对象都有且只有一个Acl 对象,它内部持有了AccessControlEntry对象并且知道Acl的拥有者 .. 一个Acl 并不直接涉及域对象, 相反是ObjectIdentity ..
    此对象存储在ACL_OBJECT_IDENTITY ..
- AccessControlEntry

    一个Acl 持有多个AccessControlEntry 对象,这经常简写为ACE(在框架中) .. 每一个ACE 指的是一个特定的三元组(Permission / Side / Acl) .. 每一个ACE
    能够授予或者拒绝 并且包含审查配置 .. ACE存储在ACL_ENTRY 表中 ..
- Permission

    一个权限表现了一个特殊的不可变的位掩码并且提供了便利的函数(为位掩码 和 输出信息). 这个基本权限(0 - 4) 并且包含在BasePermission 类中 ..
- Sid

    ACL模块需要引用主体(身份或者委托人) 以及多个GrantedAuthority 实例 ..
间接的层(通过Sid 接口提供),SID 是 'Security IDentity'的缩写 .. 常见类包含`PrincipalSid`(
用来表现在Authentication对象中的 principal) 以及 GrantedAuthoritySid,
这些安全身份信息 存储在ACL_SID 表中(也就是说SID 可能是主体信息 也可能是一个权限主体信息)

- ObjectIdentity

  每一个域对象在ACL模块中通过ObjectIdentity呈现, 默认实现是 ObjectIdentityImpl ..
- AclService

    为给定的ObjectIdentity抓取ACL, 在包括的实现(JdbcAclService), 抓取操作代理到 LookupStrategy ..

此策略接口提供了一个高度优化的策略(对抓取ACL信息), 使用批量抓取(BasicLookupStrategy) 支持使用物化视图、分层查询和类似的以性能为中心的非 ANSI(American National Standards Institute) SQL 功能的自定义实现。
- MutableAclService

    让一个修改的Acl 能够呈现并持久化 ,此接口使用是可选的 ..


注意到我们的 AclService 以及所有相关的数据库类使用ANSI SQL.. 这能够在所有主流数据库中工作 ..
目前应该可以支持 Hypersonic SQL, PostgreSQL, Microsoft SQL Server, and Oracle..

这里有两个示例说明了 spring security 如何解释acl 模块.. 第一个是[Contacts Sample](https://github.com/spring-projects/spring-security-samples/tree/main/servlet/xml/java/contacts) ..
另一个是 [Document Management System(DMS) Sample](https://github.com/spring-projects/spring-security-samples/tree/main/servlet/xml/java/dms) ..


## 开始

为了使用spring security的 acl 能力,你需要存储你的acl 信息(在某些地方). 在spring中DataSource的实例化是必要的 ..
数据源然后会注入到 `JdbcMutableAclService` 以及 `BasicLookStrategy` 实例中 ..
前者提供修改能力, 后者提供高性能的acl 抓取能力 .. 查看 示例了解配置 ...

你也需要去使用在前面部分列出的 四种ACL 特定的表来填充数据库 ..

一旦你创建了需要的schema 并实例化了 JdbcMutableAclService.. 你需要确保你的域模型支持 与  spring security acl包具有互操作性 ..

期望的是,ObjectIdentityImpl 提供是最够的... 正如它提供了大量的方式能够被使用 ..

大多数用户具有的域对象都包含了`public Serializable getId()` 方法 .. 如果返回的类是`long` 或者 与 `long` 兼容(例如 `int` ) ...
你也许会发现你不需要对ObjectIdentity 疑问做出进一步的考虑 .. ACL 模块的许多部分依赖于long 标识符 ..
    
如果你不使用`long` 或者`int` / `byte` 以及其他,那么你可能需要重新实现大量的类  ..

我们并不打算去支持非long 标识符(在spring security的acl模块中), 正如`long` 已经和所有的数据库序列兼容 ..
并且是最常见的标识符数据类型,并且具有足够的长度去 容纳所有常见使用场景 ..

以下的代码碎片展示了如何创建ACL 或者修改存在的ACL ..
```java
// Prepare the information we'd like in our access control entry (ACE)
ObjectIdentity oi = new ObjectIdentityImpl(Foo.class, new Long(44));
Sid sid = new PrincipalSid("Samantha");
Permission p = BasePermission.ADMINISTRATION;

// Create or update the relevant ACL
MutableAcl acl = null;
try {
acl = (MutableAcl) aclService.readAclById(oi);
} catch (NotFoundException nfe) {
acl = aclService.createAcl(oi);
}

// Now grant some permissions via an access control entry (ACE)
acl.insertAce(acl.getEntries().length, p, sid, true);
aclService.updateAcl(acl);
```
在前面的示例中,我们抓取和域标识符为44的Foo域对象关联的 ACL.. 我们然后增加了一个ACE(主体为 'Samantha' 能够管理 这个对象) ..
这个代码碎片是相对自我说明的, 除了 `insertAce` 方法.. 此方法的第一个参数是 确定 新项插入的位置 .. 在前面的示例中,我们放入ACE到已经存在的ACE列表末尾 。。
最后的参数是一个 boolean 指定ACE 是授予还是拒绝 .. 大多数情况下是 授予 .. 然而,如果false,则拒绝(那么权限将有效的被黑掉) ..

spring security并没有提供任何特定的集成去自动创建 / 更新 / 删除ACL 作为你的DAo或者仓库操作的一部分 。。
相反你需要编写类似于在前面示例中展示的代码 来为你单独的域对象进行适配 .. 

你应该考虑使用Aop在服务层去自动的集成ACL 信息到服务层操作上 .. 我们已经发现这种方式非常有效 ..

一旦你使用了这种技术去存储某些acl 信息到数据库中,下一步就是实际使用acl 信息作为授权决定逻辑的一部分 ..
你有大量的选择可以决定 .. 你可以编写自己的AccessDecisionVoter 或者 AfterInvocationProvider(单独的) 触发方法之前或者之后调用 ..
这些类可以使用AclService 去抓取相关的ACL 并调用`Acl.isGranted(Permission[] permission, Sid[] sids, boolean administrativeMode) ` 去决定
是否权限是授予或者拒绝 .. 除此之外,你能够使用AclEntryVoter,AclEntryAfterInvocationProvider 或者 AclEntryAfterInvocationCollectionFilteringProvider 类 ..

所有的这些类提供了基于声明式的方式去在运行时评估ACL 信息,无需编写任何代码 ..


## 总结
acl 用来做出一个数据集下的,需要敏感数据的进一步隔离时,可以做的事情  ..

// 假设一个部门有两个人事,它们都会招人, 然后人事又属于经理所管理,那么经理假设可以查看 查看人事的相关数据,那么除了分配角色之外,这是不够的 ..
// 不管角色是否存在角色继承的情况 ... 还需要做业务关联关系,那么acl就可以来管理这种业务关联关系 ..

// 这可以在写业务的情况下,填充acl 表,则在其他情况下则可以通过统一的权限判断来决定它是否具有权限访问  ...
