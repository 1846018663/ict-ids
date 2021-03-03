package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Data;


@Data
@TableName(value = "ivs_app_city_info")//城市信息
public class IvsAppCityInfo  {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;


    /**
     *城市名称
     */
    private String cName;


    /**
     * 城市编码
     */
    private String Ccode;


    /**
     * 城市中心经度
     */
    private String longitude;


    /**
     * 城市中心纬度
     */
    private String latitude;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getCcode() {
        return Ccode;
    }

    public void setCcode(String ccode) {
        Ccode = ccode;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
