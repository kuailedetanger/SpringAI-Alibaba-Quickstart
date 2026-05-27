package com.yingzi.vector.simple.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量数据库入门示例控制器
 * 
 * 这个类展示了如何使用 Spring AI 的 SimpleVectorStore 进行向量数据的
 * 添加、删除、保存、加载和相似度搜索操作。
 * 
 * @author yingzi
 * @date 2025/4/16:19:11
 */
@RestController
@RequestMapping("/simple")
public class SimpleController {

    // 日志工具，用于打印运行信息
    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);
    
    // 向量存储对象，负责存储和搜索向量数据
    private final SimpleVectorStore simpleVectorStore;
    
    // 数据保存路径：将向量数据持久化到这个JSON文件
    private final String SAVE_PATH = System.getProperty("user.dir") + "/Vector-Databases/vector-simple-example/src/main/resources/save.json";

    /**
     * 构造函数：初始化向量存储
     * 
     * @param embeddingModel 嵌入模型（AI模型），负责将文字转换成向量
     *                       Spring AI会自动注入这个模型
     */
    public SimpleController(EmbeddingModel embeddingModel) {
        // 使用构建器模式创建 SimpleVectorStore
        // SimpleVectorStore 是一个简单的内存向量存储，适合学习和测试
        this.simpleVectorStore = SimpleVectorStore
                .builder(embeddingModel).build();
    }

    /**
     * 接口：添加文档到向量数据库
     * 
     * 访问方式：GET http://localhost:8090/simple/add
     * 
     * 功能说明：
     * 1. 创建多个 Document（文档）对象
     * 2. 每个文档可以包含内容和元数据（如年份、作者等）
     * 3. 通过 add() 方法将文档存入向量数据库
     * 4. 系统会自动将文字转换成向量存储
     */
    @GetMapping("/add")
    public void importData() {
        logger.info("开始添加数据");

        // 创建元数据：可以给文档添加额外的描述信息
        HashMap<String, Object> map = new HashMap<>();
        map.put("year", 2025);      // 年份
        map.put("name", "yingzi");  // 作者名称

        // 创建文档列表
        // Document 是 Spring AI 提供的文档对象，包含：内容 + 可选的元数据
        List<Document> documents = List.of(
                // 文档1：只有内容，没有元数据
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                // 文档2：内容 + 元数据（年份2024）
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("year", 2024)),
                // 文档3：内容 + 元数据（年份2025，作者yingzi）
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", map),
                // 文档4：指定ID + 内容 + 元数据（ID为"1"）
                new Document("1", "test content", map));

        // 将文档添加到向量数据库
        // 底层会自动调用嵌入模型，把文字转换成向量存储
        simpleVectorStore.add(documents);
        
        logger.info("数据添加完成");
    }

    /**
     * 接口：根据ID删除文档
     * 
     * 访问方式：GET http://localhost:8090/simple/delete
     * 
     * 功能说明：
     * 根据文档的ID删除指定文档
     * 在本示例中，会删除ID为"1"的文档（即上面添加的"test content"）
     */
    @GetMapping("/delete")
    public void delete() {
        logger.info("开始删除数据");
        // 删除ID为"1"的文档
        simpleVectorStore.delete(List.of("1"));
        logger.info("数据删除完成");
    }

    /**
     * 接口：保存向量数据到文件
     * 
     * 访问方式：GET http://localhost:8090/simple/save
     * 
     * 功能说明：
     * SimpleVectorStore 默认存储在内存中，重启程序后数据会丢失
     * 调用此接口可以将数据保存到JSON文件，实现持久化
     */
    @GetMapping("/save")
    public void save() {
        logger.info("开始保存数据到文件: {}", SAVE_PATH);
        
        File file = new File(SAVE_PATH);
        // 如果文件已存在，先删除
        if (file.exists()) {
            file.delete();
        }
        
        // 将内存中的向量数据保存到文件
        simpleVectorStore.save(file);
        
        logger.info("数据保存完成");
    }

    /**
     * 接口：从文件加载向量数据
     * 
     * 访问方式：GET http://localhost:8090/simple/load
     * 
     * 功能说明：
     * 从之前保存的JSON文件中加载向量数据到内存
     * 用于程序重启后恢复数据
     */
    @GetMapping("/load")
    public void load() {
        logger.info("开始从文件加载数据: {}", SAVE_PATH);
        
        File file = new File(SAVE_PATH);
        // 从文件加载向量数据到内存
        simpleVectorStore.load(file);
        
        logger.info("数据加载完成");
    }

    /**
     * 接口：相似度搜索
     * 
     * 访问方式：GET http://localhost:8090/simple/search
     * 
     * 功能说明：
     * 根据查询词搜索最相似的文档
     * 这是向量数据库最核心的功能！
     * 
     * 工作原理：
     * 1. 将查询词"Spring"转换成向量
     * 2. 在向量数据库中找到与这个向量最相似的文档
     * 3. 返回相似度最高的前N条结果
     */
    @GetMapping("/search")
    public List<Document> search() {
        logger.info("开始相似度搜索");
        
        // 创建搜索请求
        SearchRequest request = SearchRequest
                .builder()
                .query("Spring")  // 查询词：搜索与"Spring"相关的内容
                .topK(2)          // 返回前2条最相似的结果
                .build();
        
        // 执行相似度搜索
        // 返回的文档按相似度从高到低排序
        return simpleVectorStore.similaritySearch(request);
    }

    /**
     * 接口：带过滤条件的相似度搜索
     * 
     * 访问方式：GET http://localhost:8090/simple/search-filter
     * 
     * 功能说明：
     * 在搜索时添加元数据过滤条件，缩小搜索范围
     * 
     * 本示例过滤条件：
     * - year 必须是 2024 或 2025
     * - name 必须等于 "yingzi"
     * 
     * 常用过滤操作：
     * - eq(字段, 值): 等于
     * - in(字段, 值1, 值2...): 在指定范围内
     * - gt(字段, 值): 大于
     * - lt(字段, 值): 小于
     * - and(): 多个条件同时满足
     * - or(): 满足任一条件
     */
    @GetMapping("/search-filter")
    public List<Document> searchFilter() {
        logger.info("开始带过滤条件的搜索");
        
        // 创建过滤条件构建器
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        
        // 构建过滤表达式：year在[2025, 2024] 并且 name等于"yingzi"
        Filter.Expression expression = b.and(
                b.in("year", 2025, 2024),  // year必须是2025或2024
                b.eq("name", "yingzi")      // name必须是"yingzi"
        ).build();

        // 创建带过滤条件的搜索请求
        SearchRequest request = SearchRequest
                .builder()
                .query("Spring")           // 查询词
                .topK(2)                   // 返回前2条
                .filterExpression(expression)  // 添加过滤条件
                .build();
        
        // 执行带过滤的相似度搜索
        return simpleVectorStore.similaritySearch(request);
    }

}