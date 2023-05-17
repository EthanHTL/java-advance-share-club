# k8s组件学习

## 1. kubectl 命令行工具
  和 k8s 集群交互的命令式工具

## 2. 配置文件
yaml,资源清单文件,给定一个示例文件
- 创建一个namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: test
```
- 创建一个pod
```yaml
apiVersion: v1
kind: Pod
metada:
  name: pod1
spec:
  containers:
    - name: nginx-containers
      image: nginx:latest
```
- 定义一个deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  namespace: default
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nginx
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
        - name:
          image: nginx:latest
          ports:
            - containerPort: 80
```

首先我们可以通过命令
```shell
kubectl api-versions
```
能够查看yaml 资源清单文件所支持的所有api 版本,我们在编写的时候,可以对照此命令输出的版本

其次我们可以查看到所有支持的资源类型
```shell
kubectl api-resources
```
在编写文件的时候,可以对照支持的资源类型 ...

最终,在我们平时编写yaml文件的时候,应该通过其他命令将默认的资源yaml文件模版生成,然后做修改,并刷新集群配置 .
```shell
kubectl create [resouce type] [name] --options
```
由于kubectl的命令行工具支持 -o选项 则可以将资源配置输出为指定的文件格式 可以执行文件类型 ...

其次,我们需要加入--dry-run=client 来告诉它尝试运行,但是不真正的运行,也就是可以执行语法检查等相关任务,但是不真正的执行 ..
 
例如,我们可以打印出nginx的deployment资源类型的yaml清单文件内容
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: nginx
  name: nginx
spec:
  replicas: 1
  selector:
    matchLabels:
      app: nginx
  strategy: {}
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: nginx
    spec:
      containers:
      - image: nginx
        name: nginx
        resources: {}
status: {}
```

## 工作负载
### pod
pod是K8s的工作负载,它负责一组容器的管理,并且提供共享网络和存储的概念,一般来说一个pod 运行一个实例应用程序,如果需要横向扩展,
可以进行复制集处理,也就是启用多个pod(复制pod并运行) ... 并且一个pod的多个应用容器 共享一组端口 和网络ip 以及一组存储 ...

并且pod还具有很多特性,例如设置它部署的运行时 / 以及运行的操作系统 / 以及pod的类型,例如(静态pod) 包括kubelet 通过容器探针
观察容器的生命周期状态 ... 包括容器的特权模式设定 ..

有关共享存储,通过容器卷的方式来实现共享存储 ...

以下是一个示例:
```yaml
---
apiVersion: v1
kind: Pod
metadata:
  name: hello-world
spec:
  containers:
    - name: data-write
      image: centos
      command: ["bash","-c","for i in {1..100}; do echo $i >> /data/hello;sleep 1; done"]
      volumeMounts:
        - name: data
          mountPath: /data
    
    - name: data-read
      image: centos
      command: ["bash","-c","tail -f /data/hello"]
      volumeMounts:
        - name: data
          mountPath: /data
  volumes:
    - name: data
      emptyDir: {}
```

详情查看 https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/ ..


#### pod的资源限制
需要注意的是,对于k8s来说,不管你是多少核心,都比做1000m(毫核),也就是说, 100m核心,等于 10%的cpu算力,
不管你是实际核心为 1核 还是48核,都表示相同的算力 ..

那么如果你想用整体核心的 20%,那么就可以填写200毫核 ... 

此外,你也可以使用小数值标识核心,但是不能够小于0.1(如果需要设置小于0.1 也就是小于10%的算力),那么你可以通过设置
毫核来匹配你的需求 ..

此外对于资源内存的算力表达式同样是 1字节等于 1000m .. 那么k8s还支持 K / M / G 等国际运算单位 ... 
它们都是 1: 1000,而计算机中的Ki / Mi / Gi 都标识2的倍数 .. 也就是 1:1024

使用两种单位具有不同的字节内存分配 ... 没有任何单位后缀的都标识字节 ..

虽然需要为每一个容器进行资源限制,但是这样也能够考虑pod整体的资源需求,所有容器的资源限制总和就是Pod整体的资源需求 ..

#### pod 健康检查
pod的健康检查机制包含了几种检查方式
- livenessProbe (Probe)

定期探针容器活跃度。如果探针失败，容器将重新启动。无法更新。更多信息： https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/pod-lifecycle#container-probes

- readinessProbe (Probe)

定期探测容器服务就绪情况。如果探针失败，容器将被从服务端点中删除。无法更新。更多信息： https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/pod-lifecycle#container-probes

- startupProbe (Probe)

startupProbe 表示 Pod 已成功初始化。如果设置了此字段，则此探针成功完成之前不会执行其他探针。 如果这个探针失败，Pod 会重新启动，就像存活态探针失败一样。 这可用于在 Pod 生命周期开始时提供不同的探针参数，此时加载数据或预热缓存可能需要比稳态操作期间更长的时间。 这无法更新。更多信息： https://kubernetes.io/zh-cn/docs/concepts/workloads/pods/pod-lifecycle#container-probes


有关探针的详细描述,查看[官网](https://kubernetes.io/zh-cn/docs/reference/kubernetes-api/workload-resources/pod-v1/#Probe)

#### pod 调度
- 节点调度策略
  1. 基于资源限制实现pod调度 \
    将基于此资源限制寻找合适的节点 ...
  2. 可以基于节点选择器进行调度 
  3. 节点亲和性 \
     (nodeAffinity)  \
     1. 硬亲和性
     2. 软亲和性 \
     两者的不同就是 一种是硬亲和才能够调度,另一种是尝试偏向于选择一个更亲和的节点,不强求 .. 且基于权重来算出权重值最高的一个节点进行调度 .. \
        详细文档参考[官网](https://kubernetes.io/zh-cn/docs/reference/kubernetes-api/workload-resources/pod-v1/#NodeAffinity)
  4. 污点(污点容忍) \
    taint 标识污点节点 ... \
    创建污点的命令参考详情了解[官网](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#taint) \
    其中它包含了三种值: \
    - NoSchedule 不调度
    - PreferNoSchedule 有可能会调度(但是一般不会调度)
    - NoExecute 不仅不会调度还会驱逐已有Pod

#### 污点容忍
当一个节点拥有了污点信息之后,在pod资源描述文件中可以设定pod可以如何调度,其中一个方面就是通过污点容忍来指定能够容忍污点并调度到节点上的设置 ..
详情查看[官网](https://kubernetes.io/zh-cn/docs/reference/kubernetes-api/workload-resources/pod-v1/#%E8%B0%83%E5%BA%A6)

## 工作负载资源
也称为pod的控制面 .. 也可以成为pod的控制器,也称为工作负载资源 ..

一般来说,pod不会单独创建,他会依据工作负载资源来创建并管理多个pod,资源控制器能够处理副本的管理,上线 .. 并且在pod失效的时候提供自愈能力 ..

例如如果一个节点失败,那么控制器注意到这个节点上的pod停止工作,那么会尝试创建替换性的pod,调度器会将替身的pod调度到一个健康的节点上运行 ...

### deployment
部署工作负载,它可以部署一组无状态应用 ...

#### 修改已有资源的一些配置
通过kubectl set 命令可以解决 

这只是一个命令: 详情查看 [set](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#set) 
```shell
$ kubectl set image (-f FILENAME | TYPE NAME) CONTAINER_NAME_1=CONTAINER_IMAGE_1 ... CONTAINER_NAME_N=CONTAINER_IMAGE_N
```

通过它我们可以设置一个资源配置清单文件 / 或者直接指定需要修改资源的类型(类型支持)下面几种
```text
pod (po), replicationcontroller (rc), deployment (deploy), daemonset (ds), statefulset (sts), cronjob (cj), replicaset (rs)
```
然后指定类型的部署的名称 然后尝试设定一个容器的镜像是什么即可 ...
更多详细示例,可以参考命令大全 ..

#### 查看资源的铺开工作(展开工作)
包括以下资源类型都可以
- deployments
- daemonsets
- statefulsets

##### 历史记录
通过这个子命令查看 之前铺开的修订以及配置  .. 
详细参考了解[命令](https://kubernetes.io/docs/reference/generated/kubectl/kubectl-commands#-em-history-em-)
我们可以指定 修订版本来查看一个版本的特定详情:
```shell
kubectl rollout history daemonset/abc --revision=3
```

#### undo 回滚到之前版本
```shell
kubectl rollout undo [type name | type/name] --to-revision=0(上一个版本) | 其他版本
```

#### scale
用于将应用进行弹性缩放的操作, 能够设置deployment / replica set / replication controller / statefull set的到一个新的尺寸 ..

缩放还可以允许用户指定一个或者多个先决条件 - 针对缩放动作

如果一个--current-replicas 或者 --resource-version 被指定,那么它将在缩放被尝试之前进行验证 ..
并且它将保证在先决条件成立的情况下将缩放发送给服务器 ...

也就是说, --resource-version可以断言当前的资源版本 - 要求当前的资源版本匹配缩放的要求 .
对于 --current-replicas 要求当前缩放的尺寸匹配这个值,才允许进行缩放(默认等于 -1 没有任何条件)
### service
它可以暴露一组工作负载 作为服务能够被外部所访问 ... 它所支持暴露的工作负载资源包括 deployment / service / replica set / replication controller
 或者pod ...

本质上它通过名称查找这些资源并且使用这些资源的选择器作为 服务的选择器( 这将会在指定的端口上创建一个新的服务,它也具备选择器) ... 一个 deployment 或者复制集
仅当它们的选择器是可转换为服务所支持的选择器 - 才能够暴露 .. 例如: 当选择器仅仅包含了 matchLabels 组件 ..  
当没有指定port的时候并且资源本身包含了多个需要暴露的端口时, 服务将会重用这些端口,并且如果没有标签指定,那么新的服务将会重用来自所需要被暴露资源的标签 ...


它存在的意义是:
- 进行服务发现
- 进行pod中应用访问的负载均衡的作用

### 