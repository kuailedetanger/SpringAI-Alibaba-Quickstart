package com.yingzi.mcp.server.stdio.config;

import com.yingzi.mcp.server.stdio.component.baidutranslate.BaidutranslateProperties;
import com.yingzi.mcp.server.stdio.component.time.TimeService;
import com.yingzi.mcp.server.stdio.component.baidutranslate.BaidutranslateService;
import com.yingzi.mcp.server.stdio.component.weather.OpenMeteoService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具配置类

 */
@EnableConfigurationProperties({BaidutranslateProperties.class})
@Configuration
public class ToolConfiguration {


    /**
     * @param openMeteoService	OpenMeteoService	天气服务工具
     * @param timeService	TimeService	时间服务工具
     * @param baidutranslateService	BaidutranslateService	翻译服务工具
     *   这个类的核心功能就是 "把所有工具打包好，交给 AI 模型使用"
     *
     *
     *  ┌─────────────────────────────────────────────────────────┐
     * │                    Spring 容器                          │
     * ├─────────────────────────────────────────────────────────┤
     * │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐ │
     * │  │ TimeService  │  │OpenMeteoService│ │Baidutranslate│ │
     * │  │   (时间工具)  │  │   (天气工具)   │ │  (翻译工具)   │ │
     * │  └──────┬───────┘  └───────┬──────┘  └───────┬──────┘ │
     * │         │                  │                  │        │
     * │         └──────────────────┼──────────────────┘        │
     * │                            ▼                           │
     * │              ┌───────────────────────┐                 │
     * │              │   ToolCallbackProvider│                 │
     * │              │      (工具提供者)      │                 │
     * │              └───────────┬───────────┘                 │
     * │                          ▼                             │
     * │              ┌───────────────────────┐                 │
     * │              │      AI 模型          │                 │
     * │              │   (可以调用所有工具)   │                 │
     * │              └───────────────────────┘                 │
     * └─────────────────────────────────────────────────────────┘
     *
     *   四、为什么需要这个类？
     * 核心原因：AI 模型需要知道有哪些工具可用。
     *
     * 如果没有这个配置类：
     *
     * 工具服务类虽然存在，但 AI 模型"看不到"它们
     * AI 无法调用这些工具来完成任务
     * 有了这个配置类：
     *
     * 所有工具被统一注册到 ToolCallbackProvider
     * AI 模型可以自动发现这些工具
     * 当用户提问时，AI 可以根据问题选择合适的工具调用
     *
     *
     * @return
     */
    @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService, TimeService timeService, BaidutranslateService baidutranslateService) {
        return MethodToolCallbackProvider.builder().toolObjects(openMeteoService, timeService, baidutranslateService).build();
    }
}