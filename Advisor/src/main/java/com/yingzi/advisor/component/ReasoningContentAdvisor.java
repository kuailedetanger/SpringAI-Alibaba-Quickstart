package com.yingzi.advisor.component;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/** 自定义 Advisor
 *
 * Reasoning：推理、逻辑推演、思考过程
 * Content：内容、文本内容
 * Advisor：顾问、审核器、检测器
 *
 *
 *
 ***功能说明**：
 * - QWQ 模型会在元数据中存储思考过程
 * - 这个 Advisor 将思考过程提取出来，添加到最终回答中
 * - 让用户可以看到 AI 的"思考过程"
 *
 * @author yingzi
 * @date 2025/3/21:17:36
 */
public class ReasoningContentAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

    private final int order;

    public ReasoningContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    /**
     * `before()` 方法：在 AI 回答前处理
     * @param request
     * @return
     */
    @NotNull
    @Override
    public AdvisedRequest before(@NotNull AdvisedRequest request) {

        return request;
    }

    /**
     * `after()` 方法：在 AI 回答后处理，提取思考过程并整合到回答中
     * @param advisedResponse
     * @return 处理后的回答，包含思考过程的整合结果
     */
    @NotNull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {

        // 处理 AI 的回答
        ChatResponse resp = advisedResponse.response();
        if (Objects.isNull(resp)) {

            return advisedResponse;
        }

        // 获取 AI 的思考过程
        logger.info(String.valueOf(resp.getResults().get(0).getOutput().getMetadata()));
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));

        // 如果有思考过程，将其整合到回答中
        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                    .map(generation -> {
                        AssistantMessage output = generation.getOutput();
                        AssistantMessage thinkAssistantMessage = new AssistantMessage(
                                String.format("<think>%s</think>", reasoningContent) + output.getText(),
                                output.getMetadata(),
                                output.getToolCalls(),
                                output.getMedia()
                        );
                        return new Generation(thinkAssistantMessage, generation.getMetadata());
                    }).toList();

            ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
            return AdvisedResponse.from(advisedResponse).response(thinkChatResp).build();

        }

        return advisedResponse;
    }

    /**
     * `getOrder()` 方法：定义 Advisor 的执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return this.order;
    }

}
