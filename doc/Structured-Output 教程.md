D:\IDEA_WorkingSpace2\SpringAI-Alibaba-Quickstart\Structured-Output\doc\structured-output-tutorial.md
# Spring AI 结构化输出教学指南

## 一、什么是结构化输出？

### 简单理解
想象一下，你问 AI："帮我写一首诗"，AI 回复给你一段优美的文字。但如果我们想把这段文字存到数据库里，或者用程序处理它，就会很麻烦。

**结构化输出** 就是让 AI 按照我们规定的格式（比如 JSON）返回结果，这样程序就能轻松解析和使用了。

### 生活中的类比
- **非结构化输出**：老师在黑板上写了一首诗，你只能用眼睛看，想整理成表格很麻烦。
- **结构化输出**：老师直接给你一个 Excel 表格，里面有标题、作者、日期、内容，你可以直接复制到电脑里处理。

---

## 二、项目结构概览


---

## 三、核心概念讲解

### 3.1 转换器（Converter）

转换器就像一个翻译官，把 AI 返回的文本转换成程序能理解的数据结构。

| 转换器类型 | 作用 | 适用场景 |
|-----------|------|---------|
| `BeanOutputConverter` | 转成 Java 对象 | 需要严格的数据结构 |
| `MapOutputConverter` | 转成 Map 集合 | 灵活的键值对数据 |
| `ListOutputConverter` | 转成 List 列表 | 一组同类数据 |

### 3.2 实体类（BeanEntity）

`BeanEntity.java` 定义了我们期望的数据结构：

```java
public class BeanEntity {
    private String title;   // 标题
    private String author;  // 作者
    private String date;    // 日期
    private String content; // 内容
}
```

这就像一个表格模板，告诉 AI："请按这个格式回复我！"

---

## 四、三种实现方式详解

### 方式一：Bean 对象转换（BeanController）

**核心代码**：

```java
// 创建转换器，指定要转换的类型
BeanOutputConverter<BeanEntity> converter = new BeanOutputConverter<>(
    new ParameterizedTypeReference<BeanEntity>() {}
);

// 获取格式要求字符串
String format = converter.getFormat();
// format 内容类似：{"title":"...","author":"...","date":"...","content":"..."}

// 调用 AI 并转换
String result = chatClient.prompt(query)
    .user(u -> u.text(promptUserSpec).param("format", format))
    .call().content();

// 将结果转换成 Bean 对象
BeanEntity poem = converter.convert(result);
```

**工作流程**：
1. 告诉 AI："请按这个 JSON 格式回复我"
2. AI 返回符合格式的文本
3. 转换器把文本变成 Java 对象

**使用场景**：需要严格数据结构的业务场景，比如存入数据库。

---

### 方式二：内置 JSON 格式（JsonController）

**核心代码**：

```java
// 创建内置的 JSON 响应格式
DashScopeResponseFormat responseFormat = new DashScopeResponseFormat();
responseFormat.setType(DashScopeResponseFormat.Type.JSON_OBJECT);

// 调用时指定格式
return chatClient.prompt(query)
    .options(DashScopeChatOptions.builder()
            .withResponseFormat(responseFormat)
            .build())
    .call().content();
```

**工作流程**：
1. 使用阿里云模型内置的 JSON 模式
2. AI 直接返回 JSON 格式结果
3. 不需要额外转换

**使用场景**：简单的 JSON 输出需求，不需要转换成特定对象。

---

### 方式三：Map/List 转换（MapListController）

**核心代码**：

```java
// Map 转换器
MapOutputConverter mapConverter = new MapOutputConverter();
Map<String, Object> resultMap = mapConverter.convert(result);

// List 转换器
ListOutputConverter listConverter = new ListOutputConverter(new DefaultConversionService());
List<String> resultList = listConverter.convert(result);
```

**工作流程**：
1. 使用通用的 Map 或 List 格式
2. AI 返回对应格式的文本
3. 转换器直接解析

**使用场景**：数据结构不固定，需要灵活处理的情况。

---

## 五、如何运行项目

### 5.1 环境准备

1. **安装 JDK 17**：确保电脑上安装了 Java 17 或更高版本
2. **获取 API Key**：
   - 访问阿里云 DashScope 控制台
   - 创建 API Key（这是调用 AI 模型的钥匙）

### 5.2 配置环境变量

在系统中设置环境变量：
- **变量名**：`DASHSCOPE_API_KEY`
- **变量值**：你的 API Key

### 5.3 运行项目

1. 打开 IDE（如 IntelliJ IDEA）
2. 导入项目
3. 运行 `StructedOutputApplication.java`
4. 项目启动后访问：`http://localhost:8088`

### 5.4 测试接口

| 接口地址 | 功能 | 示例 |
|---------|------|------|
| `/bean/chat` | 普通对话 | `http://localhost:8088/bean/chat?query=写一首诗` |
| `/bean/chat-format` | Bean 结构化输出 | `http://localhost:8088/bean/chat-format` |
| `/json/chat-format` | JSON 格式输出 | `http://localhost:8088/json/chat-format` |
| `/map/chatMap` | Map 格式输出 | `http://localhost:8088/map/chatMap` |
| `/map/chatList` | List 格式输出 | `http://localhost:8088/map/chatList` |

---

## 六、课堂练习

### 练习 1：理解格式要求

观察 `BeanController` 中的 `format` 变量，回答：
- `converter.getFormat()` 返回什么内容？
- 如果想增加一个 `category`（分类）字段，需要修改哪里？

### 练习 2：修改实体类

在 `BeanEntity.java` 中添加：
- `category` 字段（诗歌分类）
- `wordCount` 字段（字数统计）

然后修改 `BeanController` 测试新字段是否生效。

### 练习 3：创建新接口

参考 `MapListController`，创建一个新的控制器，实现：
- 接收用户输入的主题
- 返回该主题的 5 个关键词（List 格式）

---

## 七、常见问题

### Q1：为什么要使用结构化输出？

**A**：如果 AI 返回普通文本，程序很难提取有用信息。结构化输出让数据有固定格式，方便程序处理。

### Q2：API Key 在哪里获取？

**A**：登录阿里云 DashScope 控制台（https://dashscope.aliyun.com/），在"API Key管理"中创建。

### Q3：为什么反序列化会失败？

**A**：可能是 AI 返回的格式不符合要求。可以检查日志中的 `result` 内容，看看是否是标准 JSON 格式。

### Q4：三种方式有什么区别？

| 方式 | 灵活性 | 复杂度 | 适用场景 |
|------|--------|--------|---------|
| Bean | 低 | 中 | 固定数据结构 |
| JSON | 中 | 低 | 简单 JSON 输出 |
| Map/List | 高 | 低 | 灵活数据结构 |

---

## 八、总结

通过本项目，我们学习了：

1. **什么是结构化输出**：让 AI 返回格式化数据
2. **三种实现方式**：Bean、JSON、Map/List
3. **核心组件**：转换器（Converter）的使用
4. **实际应用**：如何在 Spring Boot 项目中集成

**关键要点**：
- 结构化输出是 AI 应用开发的重要技能
- 选择合适的转换方式可以提高开发效率
- 格式提示词的设计很重要，要清晰告诉 AI 返回什么格式

---

**附录：参考资料**
- Spring AI 官方文档：https://docs.spring.io/spring-ai/reference/
- 阿里云 DashScope：https://dashscope.aliyun.com/
