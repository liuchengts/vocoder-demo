# vocoder-demo

基于`spring boot web`的权限拦截验证

## 重要声明

* 本分支由`1.0.7`为第一个版本，此版本默认提供`guava`存储令牌的功能
* 根据`guava`的缓存特性，提供了`2`种缓存方案(`LoadingCache、Cache`)
* 如果需要使用`redis`存储方案，参考: [四、自定义缓存方案](#jump) ；或请直接使用`1.0.5`版本（有一些遗留`bug`）
