package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value = "ivs_app_platform_info")//站台信息表
public class IvsAppPlatformInfo {


    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;

    /**
     * 所属城市统一编码
     */
    private String cCode;


    /**
     * 站台名称
     */
    private String pName;


    /**
     *站台类型 1--大站台，2--中站台，3--小站台
     */

    private int pType;


    /**
     *站台路线类型
     */

    private int pRouteType;


    /**
     * 站点群类型（1--集中式站点群，2--集约式站点群）
     */
    private int pGroupType;


    /**
     * 经度
     */
    private String longitude;


    /**
     * 纬度
     */
    private String latitude;


    /**
     * 最近下一个站点ID
     */
    private String nextPIds;


    /**
     * 最近下一个站点距离
     */
    private String nextPDistances;


    /**
     * 站点状态（1--可用，2--不可用）
     */

    private int pStatus;


    /**
     * 算法使用的from-code
     */
    private int pFromCode;


    /**
     * 算法使用的to--code
     */
    private int pToCode;


    /**
     *支线分区
     */
    private int pBranchArea;


    /**
     * 主站点映射编码
     */
    private int pMapCode;


    /**
     * 主线道路分组(城市ID+4位序列)
     */
    private int pRoadGroup;


    /**
     * 线路来回侧类型（0--来侧，1--回侧）
     */
    private int pRoadSide;





}
