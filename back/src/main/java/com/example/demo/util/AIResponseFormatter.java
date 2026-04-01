package com.example.demo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AIResponseFormatter {

    /**
     * 美化AI的回复
     * @param response AI的原始回复
     * @return 美化后的回复
     */
    public static String formatResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "";
        }

        try {
            // 构建Python进程
            ProcessBuilder pb = new ProcessBuilder("python", "src/main/java/com/example/demo/util/AIResponseFormatter.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 向Python脚本写入输入
            process.getOutputStream().write(response.getBytes());
            process.getOutputStream().flush();
            process.getOutputStream().close();

            // 读取Python脚本的输出
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // 等待进程完成
            process.waitFor();

            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // 如果执行Python脚本失败，返回原始回复
            return response;
        }
    }
}
