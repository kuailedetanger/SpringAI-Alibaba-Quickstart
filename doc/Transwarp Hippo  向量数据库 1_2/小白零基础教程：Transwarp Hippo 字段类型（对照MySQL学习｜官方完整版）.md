# 小白零基础教程：Transwarp Hippo 字段类型（对照MySQL学习｜官方完整版）

## 前言

本教程适配人群：会基础MySQL、零基础入门星环Hippo 1\.2向量数据库的学生/开发人员。所有内容**严格参照星环Hippo1\.2官方RESTful建表文档**编写，数据类型、SQL语法、API参数100%贴合官方规范。

核心学习逻辑：**Hippo兼容90%MySQL常规字段，额外新增专属向量类型**。你可以直白理解：Hippo就是一款\*\*自带专业向量检索功能的增强版MySQL\*\*。

普通结构化字段用法、SQL语法完全对标MySQL，零学习成本；仅需单独掌握Hippo独有向量字段、索引、相似度检索相关知识点即可。

---

## 一、先搞懂：MySQL 和 Hippo 本质区别

### 1\.1 MySQL（传统关系型数据库）

- 核心擅长：存储结构化普通数据（数字、字符串、日期、JSON）

- 检索方式：仅支持精准匹配、范围查询、模糊查询（固定条件筛选）

- 核心短板：无专属向量字段，无法存储Embedding数组，不支持AI语义相似度检索

### 1\.2 Transwarp Hippo 1\.2（分布式向量数据库）

- 基础能力：全面兼容MySQL主流标量字段与SQL语法，可直接替代MySQL做常规业务数据存储

- 独有能力：内置4类官方原生向量字段，配套向量索引、专属相似度函数，适配AI知识库、图片检索、文本语义问答等场景

- 产品定位：一站式存储「结构化业务数据\+AI向量数据」的复合型数据库

---

## 二、Hippo字段整体分类（官方划分）

参照官方文档，Hippo所有字段分为**标量字段**、**向量字段**两大类，分工明确：

1. **标量字段（Scalar）**：对标MySQL，用于存储常规数据，支撑精准筛选、条件过滤、排序分组

2. **向量字段（Vector）**：Hippo专属、MySQL无对应类型，用于存储AI生成的Embedding向量数组，支撑语义相似度检索

---

## 三、标量字段（官方完整版｜对标MySQL）

本节整理Hippo1\.2官方RESTful API与SQL双层全部支持的标量类型，包含底层API字段名、SQL写法、对应MySQL类型及使用约束。

### 3\.1 整数类型

底层API使用小写int8/int16/int32/int64，SQL层面兼容MySQL大小写写法，适配不同数值范围业务场景。

|RESTful底层类型|SQL书写类型|对应MySQL|适用场景|
|---|---|---|---|
|int8|TINYINT|TINYINT|状态开关、枚举值（0/1）、极小范围数字|
|int16|SMALLINT|SMALLINT|小范围分类编号、短数值|
|int32|INT/INTEGER|INT|普通ID、年龄、商品数量（最常用整数类型）|
|int64|BIGINT|BIGINT|雪花ID、时间戳、分布式主键、超大编号|

### 3\.2 浮点类型

|RESTful底层类型|SQL书写类型|对应MySQL|备注|
|---|---|---|---|
|float|FLOAT|FLOAT|单精度浮点数，用于评分、简易小数|
|double|DOUBLE|DOUBLE|双精度浮点数，用于距离、高精度计算|

### 3\.3 字符串与大文本类型（官方约束重点）

核心约束：**varchar、varchar2 必须手动指定长度**，无默认长度；string、clob无需指定长度。

|RESTful底层类型|SQL书写类型|对应MySQL|使用约束|
|---|---|---|---|
|string|STRING|VARCHAR|无需指定长度，短文本首选|
|char|CHAR\(N\)|CHAR\(N\)|固定长度字符串，必须指定N|
|varchar|VARCHAR\(N\)|VARCHAR\(N\)|可变长度，1≤N≤65535，必须指定长度|
|varchar2|VARCHAR2\(N\)|VARCHAR\(N\)|兼容Oracle语法，同样必须指定长度|
|clob|TEXT/CLOB|TEXT|超长文本，用于知识库内容、文章|

### 3\.4 布尔、时间、二进制类型

|RESTful底层类型|SQL书写类型|对应MySQL|使用场景|
|---|---|---|---|
|bool|BOOLEAN|BOOLEAN|布尔开关状态|
|date|DATE|DATE|仅存储年月日|
|datetime|DATETIME|DATETIME|存储年月日时分秒|
|timestamp|TIMESTAMP|TIMESTAMP|高精度时间戳|
|binary|BINARY|BINARY|短二进制数据|
|blob|BLOB|BLOB|大文件二进制数据|

### 3\.5 标量字段建表示例（SQL）

```sql
    -- Hippo 完全兼容MySQL写法，可直接复制执行
CREATE TABLE user_info (
    id INT PRIMARY KEY,
    username VARCHAR(50),
    age TINYINT,
    score FLOAT,
    create_time DATETIME,
    extra JSON
);

```

**强制命名规范**：Hippo与MySQL最大区别：**严格区分表名、字段名大小写**，User\_Info 和 user\_info 视为两张独立表。新手统一规范：全部小写\+下划线命名，杜绝大小写混用报错。

**主键与自增提示**：① 单表仅支持**单个主键**，不支持复合主键；② AUTO\_INCREMENT仅适配本地测试环境，分布式集群高并发场景下易产生瓶颈，生产环境推荐雪花ID、全局序列替代自增主键；③ 仅String类型主键支持auto\_id自动生成，int64主键不支持。

---

## 四、向量字段（Hippo专属｜MySQL无）

向量字段是Hippo核心功能，专门存储AI模型输出的Embedding特征数组；所有向量类型均提供**SQL上层写法 \+ RESTful底层类型**，双向兼容。

### 4\.1 向量通俗释义

向量本质：一串固定格式的数值数组（例：`\[0\.11,0\.22,0\.33,0\.45\]`），用数字化特征描述文本、图片、音频内容，以此实现语义相似度匹配。

### 4\.2 四大官方向量类型详解

#### 4\.2\.1 稠密浮点向量（全局首选）

- RESTful底层类型：**float\_vector**

- SQL书写类型：**DENSE\_VECTOR\(dimension\)**

- 必填参数：dimension（维度，范围1\~65536），维度必须与Embedding模型完全一致

- 适用场景：95%AI学习/生产场景，文本知识库、问答机器人、商品语义检索

#### 4\.2\.2 二进制向量

- RESTful底层类型：**binary\_vector**

- SQL书写类型：**BINARY\_VECTOR\(dimension\)**

- 参数约束：必须指定固定维度

- 特性：仅存储0/1二进制数据，占用内存极低、检索速度最快，精度略低于稠密向量

- 适用场景：海量低精度数据、内存受限服务器

#### 4\.2\.3 稀疏浮点向量

- RESTful底层类型：**sparse\_vector**

- SQL书写类型：**SPARSE\_VECTOR**

- 参数约束：无需指定固定维度

- 特性：绝大多数数值为0，仅保留有效特征值，节省存储空间

- 适用场景：关键词检索、分词模型、高维长尾数据

#### 4\.2\.4 稀疏浮点增强向量

- RESTful底层类型：**sparse\_float\_vector**

- SQL书写类型：**SPARSE\_FLOAT\_VECTOR**

- 参数约束：无需指定固定维度

- 特性：稀疏向量升级版，支持精细化浮点权重配置

- 适用场景：全文检索\+向量混合检索、专业词条库

### 4\.3 建表示例（标量\+向量｜企业通用模板）

```sql
-- 知识库专用表：普通业务字段 + 稠密向量字段
CREATE TABLE knowledge_base (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT '文档标题',
    content TEXT COMMENT '知识库原始文本',
    create_time DATETIME COMMENT '数据创建时间',
    doc_vector DENSE_VECTOR(768) COMMENT '768维BERT文本向量'
);


## 维度理解
维度 = 3 → 3 个数字：[0.1, 0.2, 0.3]
维度 = 128 → 128 个数字
维度 = 768 → 768 个数字
DENSE_VECTOR (768) 的意思：这个字段只能存固定 768 个数字的向量，不多也不少。
##为什么大家都用 768？不是巧合！
因为 BERT 模型（最常用的文本向量模型）默认输出就是 768 维向量！

你把一句话丢给 BERT
BERT 把这句话压缩成一串 768 个数字
这串数字就能代表这句话的意思
所以大家建向量表，几乎都写 768



```

### 4\.4 向量数据插入示例

```sql
INSERT INTO knowledge_base(title,content,doc_vector)
VALUES(
    'Hippo向量数据库简介',
    'Transwarp Hippo是星环自研分布式复合型向量数据库，兼容MySQL语法',
    '[0.11,0.22,0.33,0.45,...]'
);

```

---

## 五、向量检索前置：向量索引（新手高频踩坑点）

**核心铁律**：仅存入向量数据**无法执行相似度检索**，必须创建向量索引并手动激活；无索引仅支持低效全量扫描，不支持Hippo专属相似度查询。删除向量字段会自动绑定删除对应向量索引。

### 5\.1 索引两大核心配置

#### 5\.1\.1 官方支持索引类型

- FLAT：全量暴力检索，100%召回率，速度最慢，适配极小批量数据

- **IVF\_FLAT**：学生/测试首选，均衡精度、速度、内存，通用万能索引

- HNSW：高精度顶级索引，检索速度最快，内存占用高，适配线上高并发服务

- IVF\_SQ/IVF\_PQ：压缩型索引，大幅降低内存占用，精度小幅下降，适配海量数据

#### 5\.1\.2 官方距离度量算法

- **COSINE（余弦相似度）**：文本检索专用，比对向量方向，不受长度影响，新手首选

- L2（欧式距离）：通用算法，适配图片、多维数据，数值越小相似度越高

- INNER\_PRODUCT（内积）：高性能检索，使用前需手动归一化向量

### 5\.2 创建并激活索引（可直接复制）

```sql
-- 语法：create_vector_index('表名','向量字段','索引名','索引类型','距离算法')
CALL create_vector_index('knowledge_base', 'doc_vector', 'ivf_flat_idx', 'IVF_FLAT', 'COSINE');

-- Hippo强制要求：创建后必须激活索引方可生效
CALL activate_vector_index('knowledge_base', 'ivf_flat_idx');

```

---

## 六、Hippo专属：向量相似度检索SQL

完成「建表→插入向量→创建索引→激活索引」全流程后，使用以下专属函数，实现MySQL无法完成的语义检索。

### 6\.1 两大检索函数

- `cosine\_similarity\(field,vec\)`：余弦相似度，返回值0\~1，数值越大越相似，文本专用

- `l2\_distance\(field,vec\)`：欧式距离，返回非负数，数值越小越相似，通用全场景

### 6\.2 完整实操SQL

#### 示例1：余弦相似度（文本知识库推荐）

```sql
SELECT 
    id, title, content,
    cosine_similarity(doc_vector, '[0.11,0.22,0.33,0.45,...]') AS similarity_score
FROM knowledge_base
ORDER BY similarity_score DESC
LIMIT 3;

```

#### 示例2：L2欧式距离（图片/通用数据）

```sql
SELECT 
    id, title, content,
    l2_distance(doc_vector, '[0.11,0.22,0.33,0.45,...]') AS distance_score
FROM knowledge_base
ORDER BY distance_score ASC
LIMIT 3;

```



---

## 七、MySQL VS Hippo 全类型终极对照表

|数据分类|MySQL|Hippo SQL类型|Hippo API底层类型|小白备注|
|---|---|---|---|---|
|整数|TINYINT/INT/BIGINT|TINYINT/INT/BIGINT|int8/int32/int64|完全兼容，直接复用|
|浮点数|FLOAT/DOUBLE|FLOAT/DOUBLE|float/double|完全兼容，直接复用|
|字符串|VARCHAR/CHAR/TEXT|VARCHAR/CHAR/TEXT|string/varchar/clob|varchar必须指定长度|
|日期JSON|DATE/DATETIME/JSON|DATE/DATETIME/JSON|date/datetime/json|无额外约束|
|稠密向量|❌ 不支持|DENSE\_VECTOR\(N\)|float\_vector|AI知识库首选|
|二进制向量|❌ 不支持|BINARY\_VECTOR\(N\)|binary\_vector|省内存、高速度|
|稀疏向量|❌ 不支持|SPARSE\_VECTOR|sparse\_vector|关键词检索专用|

---

## 八、小白终极避坑清单（官方约束汇总）

1. 大小写敏感：表名、字段名严格区分大小写，统一小写\+下划线命名，规避低级报错；

2. 字符串约束：varchar、varchar2 必须手动填写长度，string、text类型无需指定；

3. 主键约束：单表仅支持单个主键，不支持复合主键；int64主键不支持auto\_id自生成；

4. 向量字段：稠密/二进制向量必须指定维度，且维度与Embedding模型输出完全一致；

5. 检索前置：向量数据写入后，必须创建并激活索引，否则无法进行相似度查询；

6. 字段删除：删除向量字段会同步清除关联索引，删除标量字段同步清除标量索引；

7. 自增主键：测试环境可用AUTO\_INCREMENT，分布式生产环境优先使用雪花ID。

---

## 九、新手极简总结

1. 标量字段：全盘照搬MySQL语法、类型、使用习惯，仅注意大小写与字符串长度约束；

2. 向量字段：SQL写DENSE\_VECTOR，底层API对应float\_vector，95%场景直接无脑使用；

3. 完整开发流程（万能模板）：建表\(标量\+向量\) → 插入向量数据 → 创建IVF\_FLAT索引 → 激活索引 → 余弦相似度SQL检索；

4. 核心区别：MySQL只能存数据，Hippo既能存普通数据，又能做AI语义相似度检索。

> （注：文档部分内容可能由 AI 生成）
