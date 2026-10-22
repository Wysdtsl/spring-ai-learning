package com.darklands.chat.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SchoolQuery extends PageQuery {

    /**
     * 城市，例如：北京、上海、广州
     */
    private String city;

    /**
     * 校区名称关键词
     */
    private String keyword;
}
