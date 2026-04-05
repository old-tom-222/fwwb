#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
XLSX文件美化工具
用于美化生成的xlsx文件内容，包括格式调整、表格结构优化等
"""

import re

def format_xlsx(content):
    """
    美化生成的xlsx文件内容
    :param content: 原始文本内容
    :return: 美化后的文本内容
    """
    if not content:
        return ""
    
    # 处理文本，提取表格数据
    content = extract_table_data(content)
    
    # 确保表格数据格式正确
    content = format_table_data(content)
    
    # 过滤掉非表格内容
    content = filter_table_content(content)
    
    return content

def extract_table_data(text):
    """
    提取表格数据
    :param text: 原始文本
    :return: 提取后的表格数据
    """
    # 移除Markdown格式
    text = re.sub(r'^#\s+(.*)$', r'\1', text, flags=re.MULTILINE)
    text = re.sub(r'^##\s+(.*)$', r'\1', text, flags=re.MULTILINE)
    text = re.sub(r'^###\s+(.*)$', r'\1', text, flags=re.MULTILINE)
    text = re.sub(r'^####\s+(.*)$', r'\1', text, flags=re.MULTILINE)
    text = re.sub(r'^\*\*\s+(.*?)\s+\*\*$', r'\1', text, flags=re.MULTILINE)
    text = re.sub(r'\*\*(.*?)\*\*', r'\1', text)
    text = re.sub(r'^\*\s+(.*)$', r'\1', text, flags=re.MULTILINE)
    
    # 移除Markdown表格的分隔线
    text = re.sub(r'^\|?\s*[-:]+\s*\|?\s*$', '', text, flags=re.MULTILINE)
    
    return text

def format_table_data(text):
    """
    确保表格数据格式正确
    :param text: 原始文本
    :return: 格式化后的表格数据
    """
    lines = text.split('\n')
    formatted_lines = []
    
    for line in lines:
        line = line.strip()
        if line:
            # 确保每行数据使用适当的分隔符
            # 这里使用逗号作为分隔符，Excel默认支持
            # 可以根据实际情况调整
            if '|' in line:
                # 如果使用竖线分隔，转换为逗号
                line = line.replace('|', ',')
            elif '\t' in line:
                # 如果使用制表符分隔，转换为逗号
                line = line.replace('\t', ',')
            elif '，' in line:
                # 如果使用中文逗号，转换为英文逗号
                line = line.replace('，', ',')
            
            # 移除可能的前后空格
            line = re.sub(r'\s*,\s*', ',', line)
            line = re.sub(r'^\s+|\s+$', '', line)
            
            # 确保每行数据格式一致
            formatted_lines.append(line)
    
    return '\n'.join(formatted_lines)

def filter_table_content(text):
    """
    过滤掉非表格内容，只保留真正的表格数据
    :param text: 原始文本
    :return: 过滤后的表格数据
    """
    lines = text.split('\n')
    table_lines = []
    
    # 计算每行的列数，找到最可能是表格的部分
    line_columns = []
    for line in lines:
        if line.strip():
            columns = len(line.split(','))
            line_columns.append(columns)
        else:
            line_columns.append(0)
    
    # 找到列数最一致的连续行
    if line_columns:
        max_count = 0
        max_columns = 0
        current_count = 0
        current_columns = 0
        
        for columns in line_columns:
            if columns > 1:
                if columns == current_columns:
                    current_count += 1
                else:
                    if current_count > max_count:
                        max_count = current_count
                        max_columns = current_columns
                    current_count = 1
                    current_columns = columns
            else:
                if current_count > max_count:
                    max_count = current_count
                    max_columns = current_columns
                current_count = 0
                current_columns = 0
        
        # 再次遍历，只保留列数等于max_columns的行
        for line in lines:
            if line.strip():
                columns = len(line.split(','))
                if columns == max_columns and max_columns > 1:
                    table_lines.append(line)
            # 保留空行作为分隔
            # else:
            #     table_lines.append('')
    
    # 如果没有找到合适的表格数据，尝试寻找可能的表格结构
    if not table_lines:
        for line in lines:
            if line.strip() and ',' in line:
                table_lines.append(line)
    
    return '\n'.join(table_lines)

if __name__ == "__main__":
    # 从标准输入读取内容
    import sys
    content = sys.stdin.read()
    formatted = format_xlsx(content)
    print(formatted)