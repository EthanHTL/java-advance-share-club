# openJDK 开发者指南
## 介绍

* OpenJDK
* 作者,提交者,审查者
  - 成为一个作者
  - 成为一个提交者
  - 繁琐/重要的改变
## 贡献一个OpenJDK 项目
* 在专注于改变OpenJDK 代码之前需要考虑的事情
* 我有一个补丁,我应该做什么?
  * 登录OCA
  * 将你的改变大众化(发布出来用于大众评审)
  * 发现一个赞助者
  * 在JBS中创建一个跟踪的议题(issue)
  * 熟悉本地进程
* 为什么我的改变被拒绝了?
## 邮件列表
* 改变你的邮件地址
## 代码约定
## JBS-JDK Bug System
* 议题的归档(issue)
  * 议题的类型
  * 一个大改变的实现
  * 实现一个JEP
* 议题状态
* 分类一个议题
  * 敏感信息(例如. hs_err.log)
* 在修复的时候更新一个issue
* 解决或者关闭一个issue
  * 当重复的时候关闭issue
  * 关闭不完整的issue
* 验证一个issue
* 移除一个issue
* JBS 标签
* JBS 标签字典
## 修复一个bug
## 克隆JDK
* 生成一个SSH key
## 构建JDK
* 配置选项
  * 与多个配置工作
* 制作目标(MAKE Targets)
## 测试jDK
* jtreg
  * 运行OpenJDK jtreg 测试
* GTest
  * 运行OpenJDK GTest 测试
* GitHub 动作
* 排除一个测试
  * problemListing jtreg 测试
  * 使用ignore 排除jtreg 测试
  * 为测试排除处理JBS bug
## 放弃一个改变
* 当一个改变被放弃的时候如何与JBS工作
* 当一个改变被放弃的时候如何和git工作
* 当一个改变被放弃的时候怎样与Mercurial 工作
## 放弃一个移植
* 使用这个模式的依据
## 同拉取请求工作
* 思考多次
* 在创建PR之前Rebase(变基)
* 在创建PR之前最终检查
* 一个PR的生命周期
* Webrevs
* 对于OpenJDK 工件的文件存储 - cr.openjdk.java.net
## 向后移植
* 向后移植到功能发布稳定存储库
* 在JBS中与移植工作
* 对于移植的拉票
* 使用Skara 工具去帮助移植
* 如何修复一个在JBS中创建的移植
## 发行注意事项
* 写一个发行注意事项
* 对于发行注意事项的通用约定
* 高级的选项
* RN-labels
* 查询发行注意事项
## JDK 发行流程
* 发行生命周期
* 里程碑和阶段
  * 推送或者延迟(在落后的时候,或者在下斜坡的时候)
  * 延迟P1 和 P2 bugs
## 项目维护
* 合并JDK 主线到项目仓库中
* 初始化 - 主需要一次
* 执行合并
* 推送之前进行测试
* 然后提交,推送并发起PR
* 共享工作
## HotSpot 开发
* 日志
  * 启用日志
## 使用传统的 Mercurial 服务器
  * 安装并配置Mercurial
    * 验证配置
  * 克隆一个 Mercurial 仓库
    * 克隆单个仓库
  * 创建一个Mercurial 变化
    * 格式化一个变化提交
    * 提交一个变化
  * 合并Mercurial 变化
  * 推送Mercurial 变化
    * 获取你已经安装的SSH key
    * 设置推送到服务器仓库的默认push 路径
    * 执行push
## 代码拥有者
* 区域邮件列表
* 目录到区域映射
  * 移除的目录
## 关于这个指南
## 术语