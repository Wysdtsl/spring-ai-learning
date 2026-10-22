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
 * 校区
 * </p>
 *
 * @author author1
 * @since 2026-10-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("school")
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 校区名称
     */
    private String name;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 校区地址
     */
    private String address;

    /**
     * 咨询电话
     */
    private String phone;

    private LocalDateTime createdAt;


}
