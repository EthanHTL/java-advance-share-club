# spring cloud
## 简介
spring cloud 提供了许多工具能够让开发者快速的在分布式系统中构建某些通用模式(例如配置管理,服务发现,熔断器,智能路由,微服务代理,控制总线,一次性token,
全局锁,领导选举,分布式会话,集群状态)...
## 特性
spring cloud 聚焦提供开箱即用的体验 - 对于大多数情况 并且提供了可扩展的机制来覆盖其他情况 !!
- 分布式 / 版本配置
- 服务注册和发现
- 路由
- 服务到服务之间的调用
- 负载均衡
- 限流熔断器
- 全局锁
- 领导选举和集群状态
- 分布式消息

## 主要项目
### spring cloud config
中心化的扩展配置管理 - 通过git 仓库支持, 这个配置资源直接映射到spring的 Environment 但是能够被非spring应用使用..
### spring cloud netflix
与各种Netflix OSS组件集成(Eureka,Hystrix,Zuul,Archaius,etc.)
### spring cloud bus
链接服务和服务实例与分布式消息结合的事件总线, 在一个集群中传递状态改变是非常有用的,例如配置改变事件 !!
### spring cloud cloudfoundry
让应用和 Pivotal Cloud Foundry 集成,提供服务发现实现 并且让它能够容易的实现SSO 以及OAuth2 保护的资源 !!
### spring cloud open service broker
对于构建了实现 open service broker api的 服务代理器提供了一个起点 ..
### spring cloud cluster
领导选举 以及通用的有状态模式的抽象和实现(例如 zookeeper / redis / hazelcast/consul) ...
### spring cloud consul
使用Hashicorp consul进行服务发现和配置管理
### spring cloud security
提供了对负载均衡的 oauth2 rest client 以及在zuul代理上中继的认证头的支持 ...
### spring cloud sleuth
对spring cloud 应用的分布式链路跟踪,兼容zipkin,Htrace / 以及基于日志的跟踪(ELK) ...
### spring cloud data flow
为现代运行系统上的可组合微服务应用提供云原生协调服务。 易于使用的DSL、拖放式GUI和REST-APIs共同简化了基于微服务的数据管道的整体协调工作。
### spring cloud stream
一个轻量级的事件驱动的微服务框架能够快速的构建链接外部系统的应用, 简单的声明式模型去发送和接收消息(在spring boot 应用之中通过apache kafka / rabbitMQ)
### spring cloud stream applications
spring cloud stream application 式开箱即用的spring boot 应用提供了和外部中间件系统的集成,例如apache kafka,rabbitMQ. 使用spring cloud stream中的
binder 抽象 !!
### spring cloud task
一个短暂的微服务框架，用于快速构建执行有限数量数据处理的应用程序。 简单的声明去增加函数式或者非函数式特性到spring boot中 ..
### spring cloud task app starters
spring cloud task app starters 是spring boot 应用- 能够进行任何处理,包括spring 批处理job(不会永久运行的),并且可以在一个数据处理的有限周期之后结束或者停止他们 !!
### spring cloud zookeeper
基于apache zookeeper 进行的服务发现和配置管理
### spring cloud connectors
让在差异化平台的 PaaS 应用能够更容易链接后台服务  例如数据库以及消息代理(项目前称 spring cloud)
### spring cloud starters
spring boot风格的 starter 项目能够对spring cloud的消费者进行依赖管理 ..将在Angel.SR2之后和其他项目合并 !!!
### spring cloud cli
spring boot cli插件能够创建spring cloud 组件项目 - 通过groovy 快速创建
### spring cloud Contract
spring cloud Contract 是一个伞式项目 - 包含了能够帮助用户能够成功实现消费者驱动的 约定(合同)方式的解决方案 !!!
### spring cloud gateway
spring cloud gateway 是一个智能的并且可以编程式的基于Reactor项目的路由 !!
### spring cloud OpenFeign
提供了对spring boot 应用的集成 - 通过自动配置并绑定到spring 环境 以及其他spring 编程式模型风格 !!
### spring cloud pipelines
Spring Cloud Pipelines提供了一个有意见的部署管道，其步骤可以确保你的应用程序可以以零停机时间的方式进行部署，并且在出现问题时可以轻松回滚。
### spring cloud function
spring cloud function 促进了通过函数实现业务逻辑 ... 它支持跨无服务器供应商的统一编程模型。同样运行在单机上的能力(本地或者在Paas中)
