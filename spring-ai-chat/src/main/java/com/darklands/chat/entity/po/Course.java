package com.darklands.chat.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 课程
 * </p>
 *
 * @author author1
 * @since 2026-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    private String name;

    /**
     * 课程方向：Java/Python/前端/测试/设计/AI 等
     */
    private String type;

    /**
     * 学历要求
     */
    private String education;

    /**
     * 学习周期
     */
    private String duration;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 课程简介
     */
    private String description;

    private LocalDateTime createdAt;


}
