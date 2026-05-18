package com.yingzi.structedOutput.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

/**
 * MapList控制器 - 演示灵活的数据结构转换
 * 核心功能：把AI响应转换为Map（键值对）或List（列表）格式
 * 
 * 【小白理解】：
 * - Map：像一本字典，查"苹果"就能找到"红色的水果"
 * - List：像一个清单，按顺序列出多个项目
 * 
 * @author yingzi
 * @date 2025/4/2:20:05
 */
@RestController
@RequestMapping("/map")
public class MapListController {

    // 日志工具，用于打印调试信息
    private static final Logger log = LoggerFactory.getLogger(MapListController.class);

    // ========== 核心组件 ==========
    // 1. ChatClient：高级聊天客户端
    private final ChatClient chatClient;
    
    // 2. mapConverter：【关键】把JSON转成Map<String, Object>
    //    Map就像一个装着键值对的盒子：{"名称": "值", "年龄": 18}
    private final MapOutputConverter mapConverter;
    
    // 3. listConverter：【关键】把JSON转成List<String>
    //    List就像一个购物清单：["苹果", "香蕉", "橙子"]
    private final ListOutputConverter listConverter;

    /**
     * 构造方法 - 初始化两个转换器
     */
    public MapListController(ChatClient.Builder builder) {
        // ========== 创建Map转换器 ==========
        // MapOutputConverter：把JSON对象 {"key": "value"} 转成Java的Map
        this.mapConverter = new MapOutputConverter();
        
        // ========== 创建List转换器 ==========
        // ListOutputConverter：把JSON数组 ["a", "b", "c"] 转成Java的List
        // 需要传入一个转换服务（DefaultConversionService是Spring默认的）
        this.listConverter = new ListOutputConverter(new DefaultConversionService());

        // 创建ChatClient实例
        this.chatClient = builder.build();
    }

    /**
     * 方式一：转换为Map格式
     * 演示：让AI返回键值对格式，然后转成Java的Map
     * 
     * 【适用场景】：
     * - 需要描述事物的多个属性时
     * - 比如：{"颜色": "黑色", "形状": "随物体变化", "特点": "没有重量"}
     * 
     * @param query 用户提问
     * @return Map格式的结果
     */
    @GetMapping("/chatMap")
    public Map<String, Object> chatMap(
            @RequestParam(value = "query", 
            defaultValue = "请为我描述下影子的特性") String query) {
        
        // ========== 格式提示词 ==========
        // 告诉AI：
        // 1. key为描述的东西（比如"颜色"）
        // 2. value为对应的值（比如"黑色"）
        String promptUserSpec = """
                format: key为描述的东西，value为对应的值
                outputExample: {format};
                """;
        
        // 获取Map的格式模板
        // 格式类似：{"key1": "value1", "key2": "value2"}
        String format = mapConverter.getFormat();
        log.info("【Map格式模板】: {}", format);

        // 调用AI并设置格式
        String result = chatClient.prompt(query)
                .user(u -> u.text(promptUserSpec).param("format", format))
                .call().content();
        
        log.info("【AI响应】: {}", result);
        
        // ========== 转换为Map ==========
        Map<String, Object> convert = null;
        try {
            // 把JSON字符串转成Map
            convert = mapConverter.convert(result);
            log.info("✅ Map转换成功！结果: {}", convert);
        } catch (Exception e) {
            log.error("❌ Map转换失败: {}", e.getMessage());
        }
        
        return convert;
    }

    /**
     * 方式二：转换为List格式
     * 演示：让AI返回列表格式，然后转成Java的List
     * 
     * 【适用场景】：
     * - 需要列出多个同类项目时
     * - 比如：["影子会跟着人走", "影子在阳光下出现", "影子没有颜色"]
     * 
     * @param query 用户提问
     * @return List格式的结果
     */
    @GetMapping("/chatList")
    public List<String> chatList(
            @RequestParam(value = "query", 
            defaultValue = "请为我描述下影子的特性") String query) {
        
        // ========== 格式提示词 ==========
        // 告诉AI：只需要返回值的列表
        String promptUserSpec = """
                format: value为对应的值
                outputExample: {format};
                """;
        
        // 获取List的格式模板
        // 格式类似：["value1", "value2", "value3"]
        String format = listConverter.getFormat();
        log.info("【List格式模板】: {}", format);

        // 调用AI并设置格式
        String result = chatClient.prompt(query)
                .user(u -> u.text(promptUserSpec).param("format", format))
                .call().content();
        
        log.info("【AI响应】: {}", result);
        
        // ========== 转换为List ==========
        List<String> convert = null;
        try {
            // 把JSON数组转成List
            convert = listConverter.convert(result);
            log.info("✅ List转换成功！结果: {}", convert);
        } catch (Exception e) {
            log.error("❌ List转换失败: {}", e.getMessage());
        }
        
        return convert;
    }
}