# 向量数据库入门教程

## 一、什么是向量数据库？

### 1.1 简单理解

想象一下，你有很多书本，现在想找一本关于"人工智能"的书。

- **传统数据库**：需要精确匹配书名或关键词，比如必须输入"人工智能"这四个字
- **向量数据库**：你可以说"我想找一本讲机器怎么学习的书"，它能理解你的意思，找到相关的书

**向量数据库就像一个聪明的图书管理员，能理解文字的含义，而不只是匹配文字本身。**

### 1.2 核心概念

| 概念 | 通俗解释 |
|------|----------|
| **向量 (Vector)** | 把文字变成一串数字，就像给每个句子一个"身份证" |
| **嵌入模型 (Embedding Model)** | 负责把文字转换成向量的工具 |
| **相似度搜索** | 找与查询内容意思最接近的文档 |
| **元数据过滤** | 按条件筛选（比如只找2025年的文档） |

## 二、项目结构介绍


### 2.1 vector-simple-example

**适合场景**：学习、测试、小规模数据

这个例子使用 `SimpleVectorStore`，数据存储在内存中，重启后数据会消失。

**核心代码解读**（SimpleController.java）：

```java
// 创建向量存储
this.simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

// 添加文档
List<Document> documents = List.of(
    new Document("Spring AI rocks!!"),
    new Document("You walk forward facing the past...", Map.of("year", 2024))
);
simpleVectorStore.add(documents);

// 搜索相似文档
simpleVectorStore.similaritySearch(SearchRequest
    .builder()
    .query("Spring")  // 搜索关键词
    .topK(2)          // 返回前2条
    .build());
```

**测试接口**：

| 接口 | 功能 |
|------|------|
| GET /simple/add | 添加测试数据 |
| GET /simple/search | 搜索相似文档 |
| GET /simple/search-filter | 带条件过滤搜索 |
| GET /simple/save | 保存到文件 |
| GET /simple/load | 从文件加载 |

### 2.2 vector-elasticsearch-exmaple

**适合场景**：生产环境、大规模数据

Elasticsearch是专业的搜索引擎，支持海量数据的快速搜索。

**配置说明**（application.yml）：
```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200  # Elasticsearch地址
```

### 2.3 vector-redis-example

**适合场景**：高性能场景、缓存+向量存储

Redis是内存数据库，速度非常快，适合对响应时间要求高的场景。

**配置说明**（application.yml）：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 三、工作流程演示

### 3.1 完整流程


### 3.2 实际操作步骤

**第一步：启动服务**
```bash
# 进入项目目录
cd vector-simple-example

# 运行项目
mvn spring-boot:run
```

**第二步：添加数据**
```bash
curl http://localhost:8090/simple/add
```

**第三步：搜索数据**
```bash
curl http://localhost:8090/simple/search
```

**预期结果**：
```json
[
  {
    "content": "Spring AI rocks!! Spring AI rocks!!...",
    "metadata": {"year": 2025, "name": "yingzi"},
    "similarityScore": 0.95
  }
]
```

## 四、三种向量存储对比

| 特性 | SimpleVectorStore | Elasticsearch | Redis |
|------|-------------------|---------------|-------|
| **数据持久化** | 需要手动保存 | 自动持久化 | 可配置持久化 |
| **数据规模** | 小 | 大 | 中到大 |
| **搜索速度** | 一般 | 快 | 非常快 |
| **部署复杂度** | 无 | 中等 | 简单 |
| **适用场景** | 学习测试 | 生产环境 | 高性能场景 |

## 五、课后练习

### 练习1：添加更多文档

修改 `SimpleController.java` 的 `importData()` 方法，添加你自己的文档：

```java
List<Document> documents = List.of(
    new Document("Java是一种编程语言"),
    new Document("Spring Boot是Java的一个框架"),
    new Document("机器学习是人工智能的分支"),
    // 在这里添加你自己的文档
    new Document("你的文档内容", Map.of("author", "你的名字"))
);
```

### 练习2：测试不同的搜索词

尝试用不同的关键词搜索，观察结果变化：

```bash
# 搜索 "Java"
curl http://localhost:8090/simple/search?query=Java

# 搜索 "编程"
curl http://localhost:8090/simple/search?query=编程
```

### 练习3：使用过滤条件

修改 `searchFilter()` 方法，尝试不同的过滤条件：

```java
Filter.Expression expression = b.and(
    b.in("year", 2025, 2024),  // 年份在2024或2025
    b.eq("name", "yingzi")       // 名字等于yingzi
).build();
```

## 六、常见问题

### Q1：为什么需要向量数据库？

A：传统数据库只能精确匹配，比如搜索"苹果"只能找到包含"苹果"的文档。向量数据库可以找到意思相似的内容，比如搜索"苹果"可能会找到"iPhone"相关的文档。

### Q2：向量是怎么生成的？

A：向量是通过嵌入模型（Embedding Model）生成的。这个模型就像一个翻译官，把文字翻译成数字语言，让计算机能够理解和比较。

### Q3：数据会丢失吗？

A：
- `SimpleVectorStore`：数据在内存中，重启服务会丢失，需要调用 `/save` 保存到文件
- `Elasticsearch` 和 `Redis`：默认会持久化到磁盘

## 七、总结

| 要点 | 说明 |
|------|------|
| **向量数据库** | 存储和搜索向量数据的专用数据库 |
| **核心能力** | 语义相似度搜索、元数据过滤 |
| **三种实现** | Simple（学习）、Elasticsearch（生产）、Redis（高性能） |
| **工作流程** | 文本→向量→存储→搜索→返回结果 |

下节课我们将学习如何将向量数据库应用到实际的RAG（检索增强生成）场景中。
