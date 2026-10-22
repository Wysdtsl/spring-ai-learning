package com.darklands.chat.entity.query;

import lombok.Data;

/**
 * 分页查询基类。
 *
 * 只放“查询条件”，不放数据库字段映射。
 */
@Data
public class PageQuery {

    /**
     * 页码，从 1 开始
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}
