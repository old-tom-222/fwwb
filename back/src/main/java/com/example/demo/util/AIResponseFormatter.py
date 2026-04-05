#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
AI回复美化工具
用于美化AI的回复，包括缩进、分行等，并且处理下载地址的点击功能
"""

import re

def format_response(response):
    """
    美化AI的回复
    :param response: AI的原始回复
    :return: 美化后的回复
    """
    if not response:
        return ""
    
    # 处理Markdown格式，转换为正常的美化格式
    response = format_markdown(response)
    
    # 处理下载地址，添加HTML链接
    response = add_download_links(response)
    
    # 处理代码块，添加适当的缩进和格式
    response = format_code_blocks(response)
    
    # 处理段落，添加适当的换行
    response = format_paragraphs(response)
    
    return response

def format_markdown(text):
    """
    处理Markdown格式，转换为正常的美化格式
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 处理标题（###和####）
    # 将### 标题 转换为 <h3>标题</h3>
    text = re.sub(r'^###\s+(.*)$', r'<h3>\1</h3>', text, flags=re.MULTILINE)
    # 将#### 标题 转换为 <h4>标题</h4>
    text = re.sub(r'^####\s+(.*)$', r'<h4>\1</h4>', text, flags=re.MULTILINE)
    
    # 处理粗体（** **）
    text = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', text)
    
    # 处理缩进（每行前的单个*）
    text = re.sub(r'^\*\s+(.*)$', r'<p style="margin-left: 20px;">\1</p>', text, flags=re.MULTILINE)
    
    return text

def add_download_links(text):
    """
    处理下载地址，添加HTML链接
    :param text: 原始文本
    :return: 添加了HTML链接的文本
    """
    # 匹配HTTP/HTTPS链接
    url_pattern = r'(https?://[\w\-._~:/?#[\]@!$&\'()*+,;=.]+)'
    
    def replace_url(match):
        url = match.group(1)
        # 检查是否是下载链接（简单判断，实际可能需要更复杂的逻辑）
        if any(ext in url.lower() for ext in ['.pdf', '.docx', '.xlsx', '.txt', '.md', '.zip', '.rar']):
            return f'<a href="{url}" target="_blank" rel="noopener noreferrer">下载文件</a>'
        else:
            return f'<a href="{url}" target="_blank" rel="noopener noreferrer">{url}</a>'
    
    return re.sub(url_pattern, replace_url, text)

def format_code_blocks(text):
    """
    处理代码块，添加适当的缩进和格式
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 匹配代码块（```开头和结尾）
    code_pattern = r'```(\w*?)\n([\s\S]*?)```'
    
    def replace_code(match):
        language = match.group(1)
        code = match.group(2)
        return f'<pre><code>{code}</code></pre>'
    
    return re.sub(code_pattern, replace_code, text)

def format_paragraphs(text):
    """
    处理段落，添加适当的换行
    :param text: 原始文本
    :return: 格式化后的文本
    """
    # 处理连续的换行，转换为段落
    text = re.sub(r'\n{3,}', '\n\n', text)
    
    # 只在非HTML标签内的换行转换为HTML换行标签
    # 这确保了我们添加的HTML标签不会被破坏
    
    # 定义一个函数来处理匹配到的内容
    def replace_newlines(match):
        # 如果匹配到的是HTML标签，直接返回
        if match.group(0).startswith('<') and match.group(0).endswith('>'):
            return match.group(0)
        # 否则，将换行转换为<br>
        return match.group(0).replace('\n', '<br>')
    
    # 使用正则表达式匹配HTML标签和非HTML标签部分
    # 这个正则表达式会匹配所有的HTML标签和非HTML标签部分
    pattern = r'(<[^>]+>)|([^<]+)'
    text = re.sub(pattern, replace_newlines, text)
    
    return text

if __name__ == "__main__":
    # 从标准输入读取AI的回复
    import sys
    response = sys.stdin.read()
    formatted = format_response(response)
    print(formatted)