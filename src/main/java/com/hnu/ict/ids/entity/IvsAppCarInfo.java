package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value = "ivs_app_car_info")//车辆信息
public class IvsAppCarInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;


    /**
     * 车辆类型
     */
    private int catType;


    /**
     * 车辆最大载客量
     */
    private int carSeatNumber;


    /**
     * 车牌号
     */
    private String licenseNumber;


    /**
     * 城市编码
     */
    private String cCoed;


    /**
     * 车辆状态  0=不可用,1=可用
     */
    private String cStatus;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCatType() {
        return catType;
    }

    public void setCatType(int catType) {
        this.catType = catType;
    }

    public int getCarSeatNumber() {
        return carSeatNumber;
    }

    public void setCarSeatNumber(int carSeatNumber) {
        this.carSeatNumber = carSeatNumber;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getcCoed() {
        return cCoed;
    }

    public void setcCoed(String cCoed) {
        this.cCoed = cCoed;
    }

    public String getcStatus() {
        return cStatus;
    }

    public void setcStatus(String cStatus) {
        this.cStatus = cStatus;
    }
}
