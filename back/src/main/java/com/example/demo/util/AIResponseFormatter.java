package com.example.demo.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AIResponseFormatter {

    public static String formatResponse(String response) {
        return executePythonScript("src/main/java/com/example/demo/util/AIResponseFormatter.py", response);
    }

    public static String formatDocx(String content) {
        return executePythonScript("src/main/java/com/example/demo/util/DocxFormatter.py", content);
    }

    public static String formatXlsx(String content) {
        return executePythonScript("src/main/java/com/example/demo/util/XlsxFormatter.py", content);
    }

    private static String executePythonScript(String scriptPath, String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            process.getOutputStream().write(input.getBytes("UTF-8"));
            process.getOutputStream().flush();
            process.getOutputStream().close();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8")
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return input;
        }
    }
}