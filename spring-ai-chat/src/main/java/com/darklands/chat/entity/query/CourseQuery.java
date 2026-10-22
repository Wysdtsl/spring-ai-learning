package com.darklands.chat.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;

/**
 * 课程查询条件。
 *
 * 用于：
 * 1. 前端课程列表的筛选和分页
 * 2. 智能客服 queryCourse 工具的输入
 */
/*query 类就是专门接收查询条件的对象*/
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQuery extends PageQuery {

    @ToolParam(description = "课程名称或简介关键词，可为空")
    private String keyword;

    @ToolParam(description = "课程方向，例如 Java、Python、前端、测试、设计、AI")
    private String type;

    @ToolParam(description = "学历要求，例如 不限、大专及以上、本科及以上")
    private String education;

    @ToolParam(description = "最低价格，可为空")
    private BigDecimal minPrice;

    @ToolParam(description = "最高价格，可为空")
    private BigDecimal maxPrice;
}
