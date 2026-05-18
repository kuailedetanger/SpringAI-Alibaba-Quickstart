# 阿里云百炼 API 实战教程

## 🎯 课程目标

通过本课程学习，你将能够：
- 理解什么是 AI 大模型 API
- 学会调用阿里云百炼平台的文本生成接口
- 掌握流式输出和多模态（文本+音频）的调用方式
- 了解如何在实际项目中集成 AI 能力

---

## 🏗️ 项目结构概览

---

## 📚 第一章：项目环境准备

### 1.1 所需工具

- **JDK 17**：Java 开发环境（必须是 17 版本）
- **Maven**：项目依赖管理工具
- **IntelliJ IDEA**：代码编辑器（推荐）
- **阿里云账号**：用于获取 API Key

### 1.2 获取 API Key

1. 访问 [阿里云百炼平台](https://dashscope.aliyuncs.com/)
2. 注册/登录账号
3. 在控制台找到 API Key 管理
4. 创建并复制你的 API Key（类似 `sk-xxxxxxxxxxxx`）

### 1.3 设置环境变量

**Windows 用户：**
```cmd
setx DASHSCOPE_API_KEY "你的API Key"
```

**设置后需要重启 IDE！**

---

## 🚀 第二章：文本生成入门

### 2.1 核心代码解析

打开 `TextGeneration.java` 文件，我们来逐行理解：

```java
// 1. 定义消息结构
static class Message {
    String role;    // 角色：system（系统）、user（用户）、assistant（助手）
    String content; // 内容
    
    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
```

**角色说明：**
- `system`：系统提示词，告诉 AI 它的身份和行为准则
- `user`：用户的问题或请求
- `assistant`：AI 的回复（用于多轮对话）

### 2.2 请求流程

```java
// 创建请求体
RequestBody requestBody = new RequestBody(
    "qwen-plus",                              // 使用的模型名称
    new Input(new Message[] {
        new Message("system", "You are a helpful assistant."),  // 系统设定
        new Message("user", "你是谁？")                         // 用户问题
    }),
    new Parameters("message")                 // 返回格式
);
```

### 2.3 发送 HTTP 请求

```java
// 创建 URL 和连接
URL url = new URL("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation");
HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

// 设置请求方法和头信息
httpURLConnection.setRequestMethod("POST");
httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
httpURLConnection.setRequestProperty("Authorization", "Bearer " + apiKey);
```

### 2.4 运行程序

右键运行 `TextGeneration.java` 的 `main` 方法，你会看到类似这样的输出：

```json
{"output":{"text":"我是阿里云研发的AI助手豆包，很高兴为您服务！"},"usage":{"input_tokens":12,"output_tokens":15}}
```

---

## 🌊 第三章：流式输出详解

### 3.1 什么是流式输出？

普通请求：等待完整响应后一次性返回（像快递打包送达）

流式输出：边生成边返回，逐字显示（像打字机一样）

### 3.2 运行体验

打开 `StreamGengeration.java` 文件，运行后观察输出效果：


**优点：**
- 用户体验更好，不用长时间等待
- 适合实时聊天场景

---

## 🎵 第四章：多模态功能

### 4.1 什么是多模态？

多模态 = 多种数据类型的融合

本项目中的多模态：**文本 + 音频**

### 4.2 代码解析

打开 `TextAudioGeneration.java`：

```java
private static final String MP3_URL = "https://dashscope.oss-cn-beijing.aliyuncs.com/audios/welcome.mp3";

RequestBody requestBody = new RequestBody(
    "qwen-omni-turbo",                       // 多模态模型
    new Message[] {
        new Message("system", new Content[] {
            new Content("text", "You are a helpful assistant.")
        }),
        new Message("user", new Content[] {
            new Content("input_audio", new InputAudio(MP3_URL, "mp3")), // 音频输入
            new Content("text", "这段音频在说什么")                       // 文本问题
        })
    },
    true,                                    // 启用流式输出
    new StreamOptions(true),
    List.of("text", "audio"),                // 输出文本和音频
    new Audio("Cherry", "wav")               // 语音类型和格式
);
```

### 4.3 功能演示

运行后，AI 会：
1. 识别音频内容
2. 用文字回答你的问题
3. 同时生成语音回复

---

## 💡 第五章：实战练习

### 练习 1：修改提问内容

修改 `TextGeneration.java` 中的用户问题：

```java
new Message("user", "请用一句话介绍人工智能")
```

运行看看 AI 如何回答。

### 练习 2：添加多轮对话

尝试添加更多对话历史：

```java
new Input(new Message[] {
    new Message("system", "你是一个数学老师。"),
    new Message("user", "什么是勾股定理？"),
    new Message("assistant", "勾股定理是指直角三角形的两条直角边的平方和等于斜边的平方。"),
    new Message("user", "如何证明它？")
})
```

### 练习 3：自定义系统角色

```java
new Message("system", "你是一个幽默风趣的美食家，请用轻松的语气回答问题。")
```

---

## ⚠️ 常见问题与解决方案

### Q1：运行时报错 "API Key 无效"

**解决方案：**
1. 检查环境变量是否正确设置
2. 重启 IDE 确保环境变量生效
3. 直接在代码中硬编码 API Key（仅测试用，正式项目不要这样做）：

```java
// 仅用于测试！
String apiKey = "sk-xxxxxxxxxxxxxxxxxxxxxxxx";
```

### Q2：网络连接失败

**解决方案：**
1. 检查网络是否正常
2. 尝试切换网络（如使用手机热点）
3. 确认 URL 地址正确

### Q3：响应内容乱码

**解决方案：**
确保使用 UTF-8 编码：

```java
httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
```

---

## 🔧 扩展学习

### 推荐学习路径

1. **基础篇**：掌握 HTTP 请求和 JSON 数据处理
2. **进阶篇**：学习 Spring AI 框架的使用
3. **实战篇**：开发完整的 AI 聊天应用

### 参考资源

- [阿里云百炼官方文档](https://help.aliyun.com/zh/model-studio/)
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)

---

## 📝 课后作业

1. 修改 `TextGeneration.java`，让 AI 生成一首关于春天的诗
2. 尝试调用流式输出接口，观察输出效果
3. 查阅官方文档，了解更多可选参数（如 `temperature` 控制随机性）

---

**🎉 恭喜！你已经掌握了阿里云百炼 API 的基本使用方法！**

