# spring-boot-actuator-web-api

# overview
需要先了解,urls / timestamp ..
## 1.1 urls
    端点通过Url进行呈现,主要形式是在管理服务器路径上下文之下后跟端点的路径信息,例如
`/actuator/{id}`, 但是基础路径信息能够被配置 ...,例如 management.endpoints.web.base-path进行配置 ..

## 1.2 时间戳
所有的时间戳将会被端点进行消费,要么是查询参数或者是请求body,必须能够格式化作为一个在[ISO8601](https://en.wikipedia.org/wiki/ISO_8601)中规定的日期和时间

# 2. 审查事件(auditevents)
这个端点提供了应用的审查事件 ...的相关信息 ..
## 2.1 抓取一个audit 事件
只需要通过发送get 请求到`/actuator/auditevents` ,例如
```text
$ curl 'http://localhost:8080/actuator/auditevents?principal=alice&after=2021-11-18T07%3A36%3A03.69Z&type=logout' -i -X GET
```
这个示例将会抓取logout 事件(身份信息： alice,发生时间),响应结果类似于下面:
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 121

{
  "events" : [ {
    "timestamp" : "2021-11-18T07:36:03.691Z",
    "principal" : "alice",
    "type" : "logout"
  } ]
}
```
### 2.1.1 查询参数
这个端点可以使用查询参数去限制返回的事件,例如支持的查询参数\

| 参数 | 描述 |
|-----|-----|
|after | 限制事件需要发生在给定时间之后,可选 |
|principal | 限制获取具有给定身份的事件,可选 |
| type | 限制获取给定类型的事件,可选 |

### 2.1.2 响应结构
这个响应包含了所有audit 事件的详情(匹配查询的),一下表描述了响应的结构:

| path | type | 描述|
|----- | ----- | -----|
|events | Array | audit 事件的列表 |
|events.[].timestamp | String | 当事件发生时出现的时间戳 |
|events.[].principal | String | 谁出发了这个事件 |
|events.[].type | String  | 事件的类型 |

## 3. Beans(beans)
提供有关应用bean信息的端点 ...
为了抓取beans,发起get请求到`/actuator/beans` ,例如
```shell
$ curl 'http://localhost:8080/actuator/beans' -i -X GET
```
响应结果如下:
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 1089

{
  "contexts" : {
    "application" : {
      "beans" : {
        "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration" : {
          "aliases" : [ ],
          "scope" : "singleton",
          "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration$DispatcherServletRegistrationConfiguration",
          "dependencies" : [ ]
        },
        "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration" : {
          "aliases" : [ ],
          "scope" : "singleton",
          "type" : "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration",
          "dependencies" : [ ]
        },
        "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration" : {
          "aliases" : [ ],
          "scope" : "singleton",
          "type" : "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration",
          "dependencies" : [ ]
        }
      }
    }
  }
}
```
这个端点的作用,可以方便我们了解spring 到底是如何处理bean之间的依赖关系的,加深对spring的理解 ... \
响应结构无需赘述:
- contexts
    Object, 应用的上下文(通过id 标记的)
- contexts.*.parentId
    String, 应用上下文的父id,如果有
- contexts.*.beans
    Object,在应用上下文中的bean的名称
- contexts.*.beans.aliases
    Array,别名的名称
- contexts.*.beans.*.scope
    String,bean的scope
- contexts.*.beans.*.type
    String,bean完全限定类型
- contexts.*.beans.*.resource
    String,bean所定义的资源,如果有
- contexts.*.beans.*.dependencies
    Array, 依赖的名称列表


## 4. Caches(caches)
能够访问应用的缓存的caches 端点,抓取,例如:
```shell
$ curl 'http://localhost:8080/actuator/caches' -i -X GET
```
响应如下:
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 435

{
  "cacheManagers" : {
    "anotherCacheManager" : {
      "caches" : {
        "countries" : {
          "target" : "java.util.concurrent.ConcurrentHashMap"
        }
      }
    },
    "cacheManager" : {
      "caches" : {
        "cities" : {
          "target" : "java.util.concurrent.ConcurrentHashMap"
        },
        "countries" : {
          "target" : "java.util.concurrent.ConcurrentHashMap"
        }
      }
    }
  }
}
```
响应结构: \

| path | type | 描述 |
|---- | ---- | ---- |
|cacheManagers | Object | 缓存管理器的id |
| cacheManagers.*.caches | Object | 在应用上下文中由名称键控的缓存 |
| cacheManagers.*.caches.*.target | String | 原始缓存的全限定名称 |

### 4.2通过名称抓取缓存
为了通过cache 抓取缓存,可以发送get请求到actuator/caches/{name} ...
```shell
$ curl 'http://localhost:8080/actuator/caches/cities' -i -X GET
```
例如这个例子抓取了缓存名为 cities的缓存 ..
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 113

{
  "target" : "java.util.concurrent.ConcurrentHashMap",
  "name" : "cities",
  "cacheManager" : "cacheManager"
}
```
#### 4.2.1 查询参数
请求的名称如果足够标识单个缓存,那么不需要额外的参数 .. 否则需要指定cacheManager ...,下表支持的查询参数有: \

| 参数 | 描述 |
| ---- | ---- |
|cacheManager | cacheManager的名称去限定缓存,如果缓存名是唯一的,可以忽略 |

#### 4.2.2 响应结果
响应中包含了请求缓存的详情,一下表描述了响应的结构: \
| 路径 | 类型 | 描述 |
| ---- | ---- | ---- |
| name | String | 缓存名称 |
| cacheManager | String | 缓存管理名称 | 
| target | String | 本地缓存的完全限定名 |

### 4.3 删除所有的缓存
通过delete请求即可
```shell
$ curl 'http://localhost:8080/actuator/caches' -i -X DELETE
```
### 4.4 通过名称进行移除
发送删除请求到 指定了名称的请求(/actuator/caches/{name})
```shell
$ curl 'http://localhost:8080/actuator/caches/countries?cacheManager=anotherCacheManager' -i -X DELETE
```
如果缓存名不是唯一的,需要提供查询参数(cacheManager的名称)

## 5. 条件评估报告
conditions 端点提供了有关配置以及自动配置类的条件评估报告 ..
### 5.1 抓取报告
发送get请求到 `/actuator/conditions`,展示如下
```shell
$ curl 'http://localhost:8080/actuator/conditions' -i -X GET
```
类似于如下:
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 3255

{
  "contexts" : {
    "application" : {
      "positiveMatches" : {
        "EndpointAutoConfiguration#endpointOperationParameterMapper" : [ {
          "condition" : "OnBeanCondition",
          "message" : "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoke.ParameterValueMapper; SearchStrategy: all) did not find any beans"
        } ],
        "EndpointAutoConfiguration#endpointCachingOperationInvokerAdvisor" : [ {
          "condition" : "OnBeanCondition",
          "message" : "@ConditionalOnMissingBean (types: org.springframework.boot.actuate.endpoint.invoker.cache.CachingOperationInvokerAdvisor; SearchStrategy: all) did not find any beans"
        } ],
        "WebEndpointAutoConfiguration" : [ {
          "condition" : "OnWebApplicationCondition",
          "message" : "@ConditionalOnWebApplication (required) found 'session' scope"
        } ]
      },
      "negativeMatches" : {
        "WebFluxEndpointManagementContextConfiguration" : {
          "notMatched" : [ {
            "condition" : "OnWebApplicationCondition",
            "message" : "not a reactive web application"
          } ],
          "matched" : [ {
            "condition" : "OnClassCondition",
            "message" : "@ConditionalOnClass found required classes 'org.springframework.web.reactive.DispatcherHandler', 'org.springframework.http.server.reactive.HttpHandler'"
          } ]
        },
        "GsonHttpMessageConvertersConfiguration.GsonHttpMessageConverterConfiguration" : {
          "notMatched" : [ {
            "condition" : "GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition",
            "message" : "AnyNestedCondition 0 matched 2 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.JacksonJsonbUnavailable NoneNestedConditions 1 matched 1 did not; NestedCondition on GsonHttpMessageConvertersConfiguration.JacksonAndJsonbUnavailableCondition.JsonbPreferred @ConditionalOnProperty (spring.mvc.converters.preferred-json-mapper=jsonb) did not find property 'spring.mvc.converters.preferred-json-mapper'; NestedCondition on GsonHttpMessageConvertersConfiguration.JacksonAndJsonbUnavailableCondition.JacksonAvailable @ConditionalOnBean (types: org.springframework.http.converter.json.MappingJackson2HttpMessageConverter; SearchStrategy: all) found bean 'mappingJackson2HttpMessageConverter'; NestedCondition on GsonHttpMessageConvertersConfiguration.PreferGsonOrJacksonAndJsonbUnavailableCondition.GsonPreferred @ConditionalOnProperty (spring.mvc.converters.preferred-json-mapper=gson) did not find property 'spring.mvc.converters.preferred-json-mapper'"
          } ],
          "matched" : [ ]
        },
        "JsonbHttpMessageConvertersConfiguration" : {
          "notMatched" : [ {
            "condition" : "OnClassCondition",
            "message" : "@ConditionalOnClass did not find required class 'javax.json.bind.Jsonb'"
          } ],
          "matched" : [ ]
        }
      },
      "unconditionalClasses" : [ "org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration", "org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration" ]
    }
  }
}
```
#### 5.1.1 响应结构
不再赘述 ..比较简单 ... [查看官网](https://docs.spring.io/spring-boot/docs/2.4.13/actuator-api/htmlsingle/#conditions-retrieving-response-structure)

## 6. 配置属性(configprops)
这个端点提供了应用的@ConfigurationProperties bean 的相关信息 ..
### 6.1 抓取 @ConfigurationProperties bean
为了抓取,发送GET 请求到 `/actuator/configprops`,例如:
```shell
$ curl 'http://localhost:8080/actuator/configprops' -i -X GET
```
响应结构也没有什么需要详细说明的 ...
## 7. 环境
通过此端点可以获取应用的Environment ...
### 7.1 抓取整体环境
发送get请求到`/actuator/env` 
```shell
$ curl 'http://localhost:8080/actuator/env' -i -X GET
```
响应结果类似于:
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 820

{
  "activeProfiles" : [ ],
  "propertySources" : [ {
    "name" : "systemProperties",
    "properties" : {
      "java.runtime.name" : {
        "value" : "OpenJDK Runtime Environment"
      },
      "java.vm.version" : {
        "value" : "25.302-b08"
      },
      "java.vm.vendor" : {
        "value" : "Temurin"
      }
    }
  }, {
    "name" : "systemEnvironment",
    "properties" : {
      "JAVA_HOME" : {
        "value" : "/opt/openjdk",
        "origin" : "System Environment Property \"JAVA_HOME\""
      }
    }
  }, {
    "name" : "Config resource 'class path resource [application.properties]' via location 'classpath:/'",
    "properties" : {
      "com.example.cache.max-size" : {
        "value" : "1000",
        "origin" : "class path resource [application.properties] - 1:29"
      }
    }
  } ]
}
```
#### 7.1.1 响应结构
不再赘述,比较简单 ...
#### 7.2 抓取单个属性
发送get请求到 /actuator/env/{property.name},例如:
```shell
$ curl 'http://localhost:8080/actuator/env/com.example.cache.max-size' -i -X GET
```
查询一个名为xxx的属性的相关信息:
它会将每一个propertySource中的相关信息查找一番,具体查看[响应结构](https://docs.spring.io/spring-boot/docs/2.4.13/actuator-api/htmlsingle/#env-single-response-structure),不再赘述 ..

## 8.Flyway(flyway)
这个端点提供了有关flyway执行的数据库迁移的相关信息
### 8.1 抓取迁移数据
发送get请求到`/actuator/flyway` ,例如:
```shell
$ curl 'http://localhost:8080/actuator/flyway' -i -X GET
```
响应结果
```text
HTTP/1.1 200 OK
Content-Type: application/vnd.spring-boot.actuator.v3+json
Content-Length: 515

{
  "contexts" : {
    "application" : {
      "flywayBeans" : {
        "flyway" : {
          "migrations" : [ {
            "type" : "SQL",
            "checksum" : -156244537,
            "version" : "1",
            "description" : "init",
            "script" : "V1__init.sql",
            "state" : "SUCCESS",
            "installedBy" : "SA",
            "installedOn" : "2021-11-18T07:36:02.312Z",
            "installedRank" : 1,
            "executionTime" : 5
          } ]
        }
      }
    }
  }
}
```
后续详细使用了解 ..

## 9. 健康
### 9.1 获取应用的健康信息
发起get请求到`/actuator/health` ,例如:
```text
$ curl 'http://localhost:8080/actuator/health' -i -X GET \
    -H 'Accept: application/json'
```
响应结果:
```text
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 704

{
  "status" : "UP",
  "components" : {
    "broker" : {
      "status" : "UP",
      "components" : {
        "us1" : {
          "status" : "UP",
          "details" : {
            "version" : "1.0.2"
          }
        },
        "us2" : {
          "status" : "UP",
          "details" : {
            "version" : "1.0.4"
          }
        }
      }
    },
    "db" : {
      "status" : "UP",
      "details" : {
        "database" : "H2",
        "validationQuery" : "isValid()"
      }
    },
    "diskSpace" : {
      "status" : "UP",
      "details" : {
        "total" : 325496897536,
        "free" : 174224748544,
        "threshold" : 10485760,
        "exists" : true
      }
    }
  }
}
```
响应结果: \
应用健康状态的详情,响应结构: \

| 路径 | 类型 | 描述 |
| ---- | --- | ----|