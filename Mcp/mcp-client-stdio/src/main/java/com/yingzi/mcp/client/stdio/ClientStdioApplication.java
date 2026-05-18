package com.yingzi.mcp.client.stdio;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

/**
 * MCP客户端控制台应用程序
 * 
 * 这个程序是一个基于Spring AI的聊天客户端，运行在命令行界面上。
 * 它可以连接到MCP（Model Context Protocol）服务，让用户通过命令行与AI模型进行交互。
 * 
 * 简单来说：这就是一个命令行版本的AI聊天机器人客户端
 * 
 * @author yingzi
 * @date 2025/3/27
 */
@SpringBootApplication
public class ClientStdioApplication {

    /**
     * 程序的入口方法
     * 
     * 当你运行这个程序时，JVM（Java虚拟机）会首先执行这个方法。
     * SpringApplication.run()会启动Spring Boot应用，加载所有需要的组件。
     * 
     * @param args 命令行参数（这里暂时用不到）
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(ClientStdioApplication.class, args);
    }

    /**
     * 定义聊天客户端的核心逻辑
     * 
     * 这个方法会被Spring自动调用，它创建一个"聊天循环"：
     * 1. 创建一个聊天客户端（ChatClient）
     * 2. 使用Scanner从命令行读取用户输入
     * 3. 将用户的问题发送给AI模型
     * 4. 显示AI的回答
     * 5. 循环往复，直到用户输入"exit"
     * 
     * @param chatClientBuilder Spring AI提供的聊天客户端构建器，用来创建聊天客户端
     * @param tools 工具回调提供者，用于调用MCP服务提供的工具
     * @param context Spring应用上下文，用于关闭应用
     * @return 返回一个CommandLineRunner，Spring会在启动后自动执行它
     */
    @Bean
    public CommandLineRunner predefinedQuestions(
            ChatClient.Builder chatClientBuilder,  // 聊天客户端构建器
            ToolCallbackProvider tools,           // 工具提供者（连接MCP服务）
            ConfigurableApplicationContext context) { // Spring应用上下文

        // 返回一个匿名函数，Spring会在启动后执行这个函数
        return args -> {
            // 1. 创建聊天客户端
            // 使用构建器模式，添加工具支持，然后构建出最终的ChatClient对象
            var chatClient = chatClientBuilder
                    .defaultTools(tools)  // 告诉客户端要使用MCP提供的工具
                    .build();             // 构建聊天客户端实例

            // 2. 创建一个Scanner对象，用于从命令行读取用户输入
            Scanner scanner = new Scanner(System.in);

            // 3. 开始聊天循环
            while (true) {
                // 提示用户输入问题
                System.out.print("\n>>> QUESTION: ");
                
                // 读取用户输入的一行文字
                String userInput = scanner.nextLine();
                
                // 如果用户输入"exit"（不区分大小写），就退出循环
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }
                
                // 4. 发送问题给AI并获取回答
                // chatClient.prompt(userInput) - 设置用户的问题
                // .call() - 发送请求并等待响应
                // .content() - 获取回答的文本内容
                String aiResponse = chatClient.prompt(userInput).call().content();
                
                // 5. 显示AI的回答
                System.out.println("\n>>> ASSISTANT: " + aiResponse);
            }
            
            // 6. 清理资源
            scanner.close();  // 关闭输入扫描器
            context.close();  // 关闭Spring应用上下文
        };
    }

}