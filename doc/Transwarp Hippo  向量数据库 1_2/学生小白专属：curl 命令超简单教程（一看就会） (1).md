# 学生小白专属：curl 命令超简单教程（一看就会）

# 学生小白专属：curl 命令超简单教程（一看就会）

我用**最通俗、最直白、最适合学生**的方式讲，不讲废话，直接能用！

## 一、curl 到底是什么？

一句话：
**curl = 命令行里的浏览器**
不用打开浏览器，直接在终端 / CMD 里发送网络请求、拿数据。

你可以用它：

- 测试接口

- 下载文件

- 发 GET/POST

- 测试你写的后端接口

- 看网页源码

---

# 二、最最基础命令（必背）

## 1\. 最简单：访问一个网址

```bash
curl https://www.baidu.com
```

作用：把百度首页的**源码**直接打印出来。

## 2\. 只看响应头信息（调试用）

```bash
curl -I https://www.baidu.com
```

## 3\. 访问你自己写的本地接口

```bash
curl http://localhost:8080/user
```

---

# 三、最常用 4 个请求（对应 Restful）

小白记住这 4 个，90% 场景够用！

## 1\. GET 请求（查数据）

```bash
curl http://localhost:8080/users
```

带参数：

```bash
curl "http://localhost:8080/user?id=1001"
```

## 2\. POST 请求（新增数据）

发 JSON 格式（最常用）：

```bash
curl -X POST -H "Content-Type: application/json" -d '{"name":"小明","age":20}' http://localhost:8080/user
```

小白翻译：

- `\-X POST`：用 POST 方法

- `\-H`：请求头

- `\-d`：发送的数据

## 3\. PUT 请求（修改数据）

```bash
curl -X PUT -d '{"name":"小红"}' http://localhost:8080/user/1001
```

## 4\. DELETE 请求（删除）

```bash
curl -X DELETE http://localhost:8080/user/1001
```

---

# 四、curl 最常用参数（小白必记）

## 4\.1 重点专项：彻底弄懂 curl \-X 参数

### 1、\-X 是什么？（大白话）

**\-X 全称 \-\-request，作用：手动指定 HTTP 的请求方式**。

你可以简单理解：命令行默认只会发**GET 请求**，如果你想发 POST、PUT、DELETE，就必须加 `\-X` 参数告诉curl：我要切换请求模式。

### 2、为什么 GET 请求不用 \-X？

curl **默认原生绑定 GET**，发送查询请求时，无需额外声明，直接写网址即可运行，简化命令书写；

除GET以外的 **POST / PUT / PATCH / DELETE** 都不属于curl默认请求，必须搭配 `\-X` 参数强制指定。

### 3、核心使用规则（小白死记）

- ✅ GET：**不需要** \-X（默认）

- ✅ POST/PUT/DELETE/PATCH：**必须带** \-X

### 4、统一标准语法

```bash
# 通用格式
curl -X 请求类型 [其他参数] 访问地址

```

### 5、正反案例对比（一眼看懂）

```bash
# 正确：GET（无需-X）
curl http://localhost:8080/user

# 正确：POST（必须加 -X POST）
curl -X POST -H "Content-Type:application/json" -d "{}" http://localhost:8080/user

# 正确：PUT / DELETE 同理
curl -X PUT -d "{}" http://localhost:8080/user/1
curl -X DELETE http://localhost:8080/user/1

```

### 6、新手高频误区

误区：写GET请求时手动加 \-X GET
不报错，但属于**多余写法**，行业内没人这么写，代码冗余不规范。

## 4\.2 全部常用参数汇总表

|参数|作用|
|---|---|
|`\-X GET/POST/PUT/DELETE`|手动指定请求方式，非GET请求必加|
|`\-H \&\#34;Content\-Type: application/json\&\#34;`|设置请求头，声明传输JSON数据|
|`\-d 数据`|携带请求体，向后端传递数据|
|`\-I`|仅获取、查看接口响应头|
|`\-v`|**显示完整请求日志（接口调试神器）**|
|`\-o 文件名`|将访问结果保存至指定文件|

|参数|作用|
|---|---|
|`\-X GET/POST/PUT/DELETE`|指定请求方法|
|`\-H \&\#34;Content\-Type: application/json\&\#34;`|告诉服务器发 JSON|
|`\-d \&\#39;数据\&\#39;`|发送请求体数据|
|`\-I`|只看响应头|
|`\-v`|**显示详细请求过程（调试神器）**|
|`\-o 文件名`|保存到文件|

## 超级好用：\-v 查看详细过程

```bash
curl -v http://localhost:8080/user
```

能看到：

- 请求头

- 响应头

- 状态码

- 数据内容
**调试接口必用！**

---

# 五、下载文件（超级实用）

```bash
curl -O https://www.baidu.com/img/bd_logo1.png
```

- `\-O`：自动保存原名

- `\-o 名字\.png`：自定义名字

---

# 六、小白最常用万能模板（直接复制）

## 模板 1：GET 查询

```bash
curl http://localhost:8080/资源名
```

## 模板 2：POST 新增（JSON）

```bash
curl -X POST -H "Content-Type: application/json" -d '{"key":"value"}' http://localhost:8080/资源名
```

## 模板 3：带详细日志（调试）

```bash
curl -v http://localhost:8080/test
```

---

# 七、3 句话总结 curl

1. **curl = 命令行浏览器**

2. **\-X 指定方法，\-d 发数据，\-H 加请求头**

3. **\-v 调试最方便**

---

## 你想要我再给你做一份

**curl 速查表（一页纸）**
还是
**针对你写的接口，直接给你生成 curl 命令？**

> （注：文档部分内容可能由 AI 生成）
