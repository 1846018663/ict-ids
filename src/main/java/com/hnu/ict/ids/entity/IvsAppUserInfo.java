package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "ivs_app_user_info")//司机用户表
public class IvsAppUserInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;

    /**
     * 账号名称
     */
    private String uName;

    /**
     * 登录密码
     */
    private String loginPwd;


    /**
     * 交易密码
     */
    private String transPwd;


    /**
     *账户类型  '1--一般账户，2--实名账户，3--司机
     */
    private int type;


    /**
     * 账号等级
     */
    private  int level;


    /**
     * 性别
     */
    private int gender;


    /**
     * 手机号
     */
    private String phoen;


    /**
     *邮箱
     */
    private String email;


    /**
     * 用户地址
     */
    private  String  address;


    /**
     * 用户签名
     */
    private String signature;


    /**
     * 用户头像
     */
    private String  avatar;


    /**
     * 二维码
     */
    private String qrCode;


    /**
     * 注册来源   1--App注册，2--微信小程序注册，3--支付宝注册，4--数据初始化
     */
    private int registerFrom;


    /**
     * 最后一次登录时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date lastLoginTime;


    /**
     * 用户状态  1--可用，2--注销，3--暂停使用
     */
    private int status;


    /**
     * 用户的第三方id
     */
    private String opengid;


    /**
     * 用户昵称
     */
    private String nickName;


    /**
     * 血型:\\r\\n1：A型\\r\\n2：B型\\r\\n3：AB型\\r\\n4：O型\\r\\n5：HR型
     */
    private int bloodType;

}
