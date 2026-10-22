package com.darklands.chat.entity.po;

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
 * 课程预约
 * </p>
 *
 * @author author1
 * @since 2026-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("course_reservation")//用来指定表名
public class CourseReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    //用来指定表中的主键字段信息
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * PENDING/CONFIRMED/CANCELLED
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createdAt;


}
