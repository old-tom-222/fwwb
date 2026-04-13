#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
DOCX文件美化工具
用于美化生成的docx文件内容，包括格式调整、标题处理等
"""

import re

def format_docx(content):
    """
    美化生成的docx文件内容
    :param content: 原始文本内容
    :return: 美化后的文本内容
    """
    if not content:
        return ""
    
    # 处理Markdown格式，转换为正常的文本格式
    content = format_markdown(content)
    
    # 处理段落，添加适当的换行和缩进
    content = format_paragraphs(content)
    
    # 处理标题，添加适当的格式
    content = format_headings(content)
    
    return content

def format_markdown(text):
    """
    处理Markdown格式，转换为正常的文本格式
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 处理HTML标签
    # 移除HTML标签，保留标签内的内容
    text = re.sub(r'<[^>]+>', r'', text)
    
    # 处理标题（#、##、###和####）
    # 将# 标题 转换为 标题
    text = re.sub(r'^#\s+(.*)$', r'\1\n', text, flags=re.MULTILINE)
    # 将## 标题 转换为 标题
    text = re.sub(r'^##\s+(.*)$', r'\1\n', text, flags=re.MULTILINE)
    # 将### 标题 转换为 标题（加粗）
    text = re.sub(r'^###\s+(.*)$', r'\1\n', text, flags=re.MULTILINE)
    # 将#### 标题 转换为 标题
    text = re.sub(r'^####\s+(.*)$', r'\1\n', text, flags=re.MULTILINE)
    
    # 处理使用** **作为小标题的情况
    # 将** 小标题 **转换为 小标题
    text = re.sub(r'^\*\*\s+(.*?)\s+\*\*$', r'\1\n', text, flags=re.MULTILINE)
    
    # 处理粗体（** **）
    text = re.sub(r'\*\*(.*?)\*\*', r'\1', text)
    
    # 处理缩进（每行前的单个*）
    text = re.sub(r'^\*\s+(.*)$', r'    \1', text, flags=re.MULTILINE)
    
    return text

def format_paragraphs(text):
    """
    处理段落，添加适当的换行和缩进
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 处理连续的空格和制表符
    text = re.sub(r'\s+', ' ', text)
    
    # 处理句子之间的分隔
    # 在句号、问号、感叹号后添加换行
    text = re.sub(r'([。！？.!?])\s*', r'\1\n', text)
    
    # 处理连续的换行，转换为单个换行
    text = re.sub(r'\n{3,}', '\n\n', text)
    
    # 确保每个段落之间有适当的空行
    lines = text.split('\n')
    formatted_lines = []
    for line in lines:
        line = line.strip()
        if line:
            formatted_lines.append(line)
        else:
            if formatted_lines and formatted_lines[-1]:
                formatted_lines.append('')
    
    return '\n'.join(formatted_lines)

def format_headings(text):
    """
    处理标题，添加适当的格式
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 简单的标题处理，确保标题独占一行
    lines = text.split('\n')
    formatted_lines = []
    for i, line in enumerate(lines):
        line = line.strip()
        if line:
            # 检查是否是标题（可以根据实际情况调整判断逻辑）
            if len(line) < 50 and not line.startswith('    '):
                # 可能是标题，确保前后有空行
                if formatted_lines and formatted_lines[-1]:
                    formatted_lines.append('')
                formatted_lines.append(line)
                if i < len(lines) - 1 and lines[i + 1].strip():
                    formatted_lines.append('')
            else:
                formatted_lines.append(line)
        else:
            formatted_lines.append('')
    
    return '\n'.join(formatted_lines)

if __name__ == "__main__":
    # 从标准输入读取内容
    import sys
    content = sys.stdin.read()
    formatted = format_docx(content)
    print(formatted)