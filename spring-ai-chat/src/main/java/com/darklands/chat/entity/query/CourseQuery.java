package com.darklands.chat.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.ai.tool.annotation.ToolParam;

import java.math.BigDecimal;

/**
 * 课程查询条件。
 *
 * 用于前端课程列表的筛选和分页。
 *
 * 智能客服的 queryCourse 工具使用显式的 @ToolParam 参数，
 * 不直接接收这个对象，避免嵌套结构让模型难填。
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

    @Data
    public static class Sort {
        @ToolParam(required = false, description = "排序字段，例如 price、sales")
        private String field;

        @ToolParam(required = false, description = "排序方式，例如 asc、desc")
        private String asc;
    }
}
