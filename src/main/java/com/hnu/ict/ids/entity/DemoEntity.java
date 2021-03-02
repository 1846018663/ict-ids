package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @description: 短信实体对象
 * @author: yuanhx
 * @create: 2021/01/07
 */

@Data
@TableName(value = "sms_info")
public class DemoEntity {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 手机号码 **/
    private String mobile;

    /** 验证码 **/
    private String validCode;

    /** 验证次数 **/
    private int validNum;

    /** 创建时间 **/
    private Date creatTime;
}
