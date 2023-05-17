# k8s入门
## 安装方式
k8s的安装方式有很多种,包括本地机器,云,或者自己的数据中心上部署k8s集群 ..

但是建议将某些k8s组件通过容器镜像运行,并让k8s管理这些组件,但是运行容器的组件kubelet,是一个意外 .

## 学习环境
### 安装工具
[官网页面](https://kubernetes.io/zh-cn/docs/tasks/tools/)

- kubectl
    
    这是k8s的命令行工具,通过对k8s集群运行命令,通过kubectl 可以部署应用,监测和管理集群资源以及查看日志 ..
    详细信息参考[kubectl 参考文件](https://kubernetes.io/zh-cn/docs/reference/kubectl/)

    安装方式可以有三种方式
    - linux
    - macos
    - windows
- kind

    这是一种能够在本地计算机中运行k8s的方式,但是需要docker ... 查看[快速开始了解](https://kind.sigs.k8s.io/docs/user/quick-start/)
- minikube

    这是一个可以在本地运行k8s的工具,它会运行一个一体化(all-in-one)或者多节点的本地k8s集群 ..
    这相当于是一个单机k8s集群,了解[开始](https://minikube.sigs.k8s.io/docs/start/) 
- kubeadm
    一般也使用它进行自托管集群的部署 
    
    它可以创建并管理k8s集群,[安装](https://kubernetes.io/zh-cn/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)  
    
    一旦安装成功,则可以创建[集群](https://kubernetes.io/zh-cn/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/) 

## 下载k8s
k8s为每一个组件提供了二进制文件以及一组标准的客户端应用程序来引导集群和集群交互 .. 像api服务器这样的组件也能够在集群內作为容器镜像运行 .. 官方也提供了容器
镜像提供..

### 容器镜像
可以从registry.k8s.io容器镜像仓库中获取 ..

### 二进制
在[CHANGELOG](https://github.com/kubernetes/kubernetes/tree/master/CHANGELOG) 中下载k8s组件的链接(包含了校验和) 或者通过[下载地址](https://www.downloadkubernetes.com/)
进行版本和架构过滤 ..

### kubectl
k8s的命令行工具[kubectl](https://kubernetes.io/zh-cn/docs/reference/kubectl/kubectl/) 可以进行k8s集群执行命令 ..

可以通过kubectl 部署应用程序,检查和管理集群资源以及查看日志 ..更多信息查看 [kubectl 参考文档](https://kubernetes.io/zh-cn/docs/reference/kubectl/)

## 选择一种安装方式
这里我们选择使用kubectl ..

并且我们在macos 上安装并设置kubectl .. 并且我们选择苹果芯片的安装方式,而非intel ..
```shell
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/arm64/kubectl"
```
请注意这仅仅是与k8s集群交互的客户端工具 ..

## 选择容器运行时
根据基本概念,每一个将节点都需要安装一个容器运行时来使得pod(容器集合)可以运行在上面 ..

k8s要求我们使用符合容器运行时接口(cri container runtime interface)的运行时

详细信息查看 [cri 版本支持](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#cri-versions)

以下是常见的集中容器运行时
- [containered](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#containerd)
- [cri-o](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#cri-o)
- [docker engine](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#docker)
- [mirantis container runtime](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#mcr)

但是我们仅仅只需要学习一种即可,docker engine ...
> v1.24之前的k8s 直接集成了docker engine 的一个组件,叫做dockershim,这个特殊组合不再是k8s的一部分 ..
> 有关dockershim的详细信息,查看 [移除影响](https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/migrating-from-dockershim/check-if-dockershim-removal-affects-you/) 以及 [dockershim 迁移](https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/migrating-from-dockershim/)

### 安装和配置先决条件
这将通用的设置应用到linux的k8s节点上..
有关更多信息,查看 [网络插件要求](https://kubernetes.io/zh-cn/docs/concepts/extend-kubernetes/compute-storage-net/network-plugins/#network-plugin-requirements) 或者特定的容器运行时文档

#### 转发 ipv4 并让iptables 看到桥接流量
```shell
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# 设置所需的 sysctl 参数，参数在重新启动后保持不变
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables  = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward                 = 1
EOF

# 应用 sysctl 参数而不重新启动
sudo sysctl --system
```
通过以下命令确认`br_netfilter` 以及 `overlay` 模块被加载
```shell
lsmod | grep br_netfilter
lsmod | grep overlay
```
检查`net.bridge.bridge-nf-call-iptables` 以及 `net.bridge.bridge-nf-call-ip6tables` 以及 `net.ipv4.ip_forward` 系统变量在`sysctl` 中的
配置设置为 1
```shell
sysctl net.bridge.bridge-nf-call-iptables net.bridge.bridge-nf-call-ip6tables net.ipv4.ip_forward
```

#### cgroup 驱动
linux上,[控制组(cgroup)](https://kubernetes.io/zh-cn/docs/reference/glossary/?all=true#term-cgroup) 用于限制分配给进程的资源

kubelet 和底层的容器运行时都需要对接控制组来强制执行[为pod和容器管理资源](https://kubernetes.io/zh-cn/docs/concepts/configuration/manage-resources-containers/)  - 例如
cpu / 内存这类资源设置请求和限制 .. 若要对接控制组,kubelet 和容器运行时需要一个cgroup 驱动. 关键点是kubelet 和容器运行时需要使用相同的cgroup 驱动并采用相同的配置 ..

可用的cgroup 驱动有两个:
- [cgroupfs](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#cgroupfs-cgroup-driver)
- [systemd](https://kubernetes.io/zh-cn/docs/setup/production-environment/container-runtimes/#systemd-cgroup-driver)

##### cgroupfs 驱动
cgroupfs 驱动是 kubelet 中默认的 cgroup 驱动。当使用 cgroupfs 驱动时， kubelet 和容器运行时将直接对接 cgroup 文件系统来配置 cgroup。

当 systemd 是初始化系统时， 不 推荐使用 cgroupfs 驱动，因为 systemd 期望系统上只有一个 cgroup 管理器。 此外，如果你使用 [cgroup v2](https://kubernetes.io/zh-cn/docs/concepts/architecture/cgroups) ， 则应用 systemd cgroup 驱动取代 cgroupfs。

##### systemd cgroup驱动
当某个 Linux 系统发行版使用 systemd 作为其初始化系统时，初始化进程会生成并使用一个 root 控制组（cgroup），并充当 cgroup 管理器。
systemd 与 cgroup 集成紧密，并将为每个 systemd 单元分配一个 cgroup。 因此，如果你 systemd 用作初始化系统，同时使用 cgroupfs 驱动，则系统中会存在两个不同的 cgroup 管理器。

同时存在两个 cgroup 管理器将造成系统中针对可用的资源和使用中的资源出现两个视图。某些情况下， 将 kubelet 和容器运行时配置为使用 cgroupfs、但为剩余的进程使用 systemd 的那些节点将在资源压力增大时变得不稳定。
当 systemd 是选定的初始化系统时，缓解这个不稳定问题的方法是针对 kubelet 和容器运行时将 systemd 用作 cgroup 驱动。

所以我们可以配置 [KubeletConfiguration](https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/kubelet-config-file/)的cgroupDriver选项将其设置为 systemd ..
```text
apiVersion: kubelet.config.k8s.io/v1beta1
kind: KubeletConfiguration
...
cgroupDriver: systemd
```
如果你将systemd 配置为kubelet的 cgroup驱动,那么必须将systemd配置为容器运行时的cgroup驱动,参考容器运行时文档,了解指示说明

这是因为kubelet 和容器运行时使用相同的cgroup驱动进行pod和容器的资源管理 ..
> 当更改已经加入节点的cgroup驱动是一项敏感操作,如果kubelet 已经使用某种cgroup驱动的语意创建了pod,那么更改运行时使用其他的cgroup驱动 或者当为现有的
> pod重新创建PodSandbox将会导致错误 .. 重启kubelet 也可能无法解决这个问题 ..
> 
> 如果你有切实可行的自动化方案,使用其他已更新配置的节点来替换该节点,或者使用自动化方案来重新安装,这可能是一个好的事情 ..
> 也就是将这个节点替换为使用其他cgroup驱动的节点,从而实现cgroup的更改 ..
> 
> 也就是说这没有一个好的解决方案,本身也不需要解决方案,一种操作系统或者一种虚拟机 本身也就是cgroup驱动中的其中一种,替换的方式必然只有使用其他节点进行替换或者
> 重建集群 ..

#### 将kubeadm 管理的集群迁移到使用systemd驱动节点上
[配置cgroup驱动](https://kubernetes.io/zh-cn/docs/tasks/administer-cluster/kubeadm/configure-cgroup-driver/) 教程

#### cri 版本支持
容器运行时必须至少支持特定k8s版本对应约定的容器运行时接口版本 ...
例如从k8s 1.26开始仅仅使用v1版本的cri api .. 早期默认为v1版本,但是容器运行时不支持 v1版本的api,那么kubelet 将会回退使用已经弃用的v1 alpha2版本的api ..

这里仅仅关心docker engine
#### docker engine cgroup 配置
1. 在你的每个节点上，遵循安装 Docker Engine 指南为你的 Linux 发行版安装 Docker。
2. 按照源代码仓库中的说明安装 [cri-dockerd](https://github.com/Mirantis/cri-dockerd)

对于cri-dockerd,默认情况下 cri套接字是`/run/cri-dockerd.sock`  ..

## 网络插件
[链接说明](https://kubernetes.io/zh-cn/docs/concepts/cluster-administration/networking/#how-to-implement-the-kubernetes-networking-model)







