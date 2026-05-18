package com.yingzi.toolCalling.component.weather;

// 导入JSON处理工具
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// 导入日志工具
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// 导入Spring AI工具注解（核心！）
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
// 导入HTTP请求相关工具
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


/**
 * 天气工具类 - AI可以调用这个类来查询天气
 * 
 * 这个类的作用：
 * 1. 定义一个可以被AI调用的工具方法
 * 2. 调用外部天气API获取实时天气数据
 * 3. 返回格式化的天气信息
 * 
 * @author yingzi
 * @date 2025/3/26:13:19
 */
public class WeatherTools {

    // 日志对象 - 用于打印调试信息和错误信息
    private static final Logger logger = LoggerFactory.getLogger(WeatherTools.class);

    // 天气API的基础地址
    private static final String WEATHER_API_URL = "https://api.weatherapi.com/v1/forecast.json";

    // WebClient - 用于发送HTTP请求的工具
    private final WebClient webClient;

    // ObjectMapper - 用于处理JSON数据的工具
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造方法 - 创建WeatherTools对象时会自动调用
     * 
     * @param properties 天气配置信息（包含API密钥）
     */
    public WeatherTools(WeatherProperties properties) {
        // 创建WebClient实例，配置默认请求头
        this.webClient = WebClient.builder()
                // 设置请求内容类型
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                // 设置API密钥（从配置文件读取）
                .defaultHeader("key", properties.getApiKey())
                .build();
    }

    /**
     * 获取天气信息的工具方法（核心！）
     * 
     * @Tool注解：告诉AI这是一个可以调用的工具
     * description属性：描述这个工具的用途，AI靠这个理解什么时候调用
     * 
     * @param city 城市名称（必填）
     * @param days 预报天数（1-14天）
     * @return 天气响应对象
     */
    @Tool(description = "使用天气API获取天气信息，可以查询指定城市的天气预报")
    public Response getWeatherServiceMethod(
            @ToolParam(description = "城市名称，例如：北京、上海、广州") String city,
            @ToolParam(description = "预报天数，范围是1到14天") int days) {

        // 1. 参数校验：城市名称不能为空
        if (!StringUtils.hasText(city)) {
            logger.error("请求无效：城市名称不能为空");
            return null;
        }
        
        // 2. 预处理城市名称（比如转换拼音等）
        String location = WeatherUtils.preprocessLocation(city);
        
        // 3. 构建完整的API请求URL
        String url = UriComponentsBuilder.fromHttpUrl(WEATHER_API_URL)
                .queryParam("q", location)      // 添加城市参数
                .queryParam("days", days)      // 添加预报天数参数
                .toUriString();                 // 转换为字符串URL
        
        logger.info("请求的URL：{}", url);
        
        try {
            // 4. 发送HTTP GET请求获取天气数据
            Mono<String> responseMono = webClient.get().uri(url).retrieve().bodyToMono(String.class);
            
            // 5. 等待并获取响应结果（block()表示同步等待）
            String jsonResponse = responseMono.block();
            assert jsonResponse != null;  // 确保响应不为空

            // 6. 将JSON字符串解析为Response对象
            Response response = fromJson(objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {}));
            
            logger.info("成功获取城市 {} 的天气数据", response.city());
            return response;
            
        } catch (Exception e) {
            // 处理异常情况
            logger.error("获取天气数据失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON格式的天气数据转换为Response对象
     * 
     * API返回的是JSON格式，我们需要把它转换成Java对象方便使用
     * 
     * @param json 原始JSON数据（Map格式）
     * @return 格式化后的天气响应对象
     */
    public static Response fromJson(Map<String, Object> json) {
        // 从JSON中提取各个部分的数据
        Map<String, Object> location = (Map<String, Object>) json.get("location");  // 位置信息
        Map<String, Object> current = (Map<String, Object>) json.get("current");    // 当前天气
        Map<String, Object> forecast = (Map<String, Object>) json.get("forecast");  // 预报信息
        List<Map<String, Object>> forecastDays = (List<Map<String, Object>>) forecast.get("forecastday");  // 每天的预报
        
        // 提取城市名称
        String city = (String) location.get("name");
        
        // 创建并返回Response对象
        return new Response(city, current, forecastDays);
    }

    /**
     * 天气响应记录类（用于存储天气数据）
     * 
     * record是Java 14引入的简化类，自动生成getter、equals、hashCode等方法
     * 
     * @param city 城市名称
     * @param current 当前天气信息
     * @param forecastDays 未来几天的预报列表
     */
    public record Response(String city, Map<String, Object> current, List<Map<String, Object>> forecastDays) {
    }

}