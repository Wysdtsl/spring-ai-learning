package com.darklands.chat.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程预约查询条件。
 *
 * 注意：这是“查询”条件，不是创建预约的请求对象。
 * 创建预约建议单独定义 CourseReservationRequest。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseReservationQuery extends PageQuery {

    /**
     * 预约人姓名
     */
    private String userName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 课程 ID
     */
    private Long courseId;

    /**
     * 校区 ID
     */
    private Long schoolId;

    /**
     * 状态：PENDING、CONFIRMED、CANCELLED
     */
    private String status;
}
