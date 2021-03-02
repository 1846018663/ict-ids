# 智慧车列-只能调度系统
## 表结构设计
### 车辆信息表
```
    CREATE TABLE 'ivs_app_car_info' (
        'c_id' int(11) NOT NULL AUTO_INCREMENT,
        'car_type' int(11) DEFAULT NULL COMMENT '车辆类型',
        'car_seat_number' int(3) DEFAULT NULL COMMENT '车辆最大载客量',
        'license_number' varchar(45) DEFAULT NULL COMMENT '车牌号',
        'c_code' varchar(150) DEFAULT NULL COMMENT '城市编码',
        'c_status' varchar(10) DEFAULT '0' COMMENT '车辆自身状态(0=不可用,1=可用)',
        PRIMARY KEY ('c_id') USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='车辆信息';
```
    
### 城市信息表
    CREATE TABLE 'ivs_app_city_info' (
        'c_id' int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识ID',
        'c_name' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '城市名称',
        'c_code' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '城市统一编码',
        'longitude' varchar(50) DEFAULT NULL COMMENT '城市中心经度',
        'latitude' varchar(50) DEFAULT NULL COMMENT '城市中心纬度',
        PRIMARY KEY ('c_id') USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='城市信息表';

### 站点信息表
    CREATE TABLE 'ivs_app_platform_info' (
        'p_id' int(11) NOT NULL AUTO_INCREMENT COMMENT '唯一标识ID',
        'c_code' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '所属城市统一编码',
        'p_name' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '站台名称',
        'p_type' int(11) DEFAULT NULL COMMENT '站台类型（1--大站台，2--中站台，3--小站台）',
        'p_route_type' int(11) DEFAULT NULL COMMENT '站台路线类型（1--主线站点，2--支线站点）',
        'p_group_type' int(11) DEFAULT NULL COMMENT '站点群类型（1--集中式站点群，2--集约式站点群）',
        'longitude' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '经度',
        'latitude' varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '纬度',
        'next_p_ids' varchar(200) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '最近下一个站点ID',
        'next_p_distances' varchar(500) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '最近下一个站点距离',
        'p_status' int(11) DEFAULT '1' COMMENT '站点状态（1--可用，2--不可用）',
        'p_from_code' int(20) DEFAULT NULL COMMENT '算法使用的from-code',
        'p_to_code' int(20) DEFAULT NULL COMMENT '算法使用的to--code',
        'p_branch_area' int(20) DEFAULT NULL COMMENT '支线分区',
        'p_map_code' int(20) DEFAULT NULL COMMENT '主站点映射编码',
        'p_road_group' int(20) DEFAULT NULL COMMENT '主线道路分组(城市ID+4位序列)',
        'p_road_side' int(1) DEFAULT NULL COMMENT '线路来回侧类型（0--来侧，1--回侧）',
        PRIMARY KEY ('p_id') USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC COMMENT='站台信息表';

### 司机、用户信息表
    CREATE TABLE 'ivs_app_user_info' (
        'u_id' int(11) NOT NULL AUTO_INCREMENT,
        'u_name' varchar(150) DEFAULT NULL COMMENT '账号名称',
        'login_pwd' varchar(60) DEFAULT NULL COMMENT '登录密码',
        'trans_pwd' varchar(150) DEFAULT NULL COMMENT '交易密码',
        'type' int(11) DEFAULT '1' COMMENT '1--一般账户，2--实名账户，3--司机',
        'level' int(11) DEFAULT '1' COMMENT '账号等级',
        'gender' int(11) DEFAULT '0' COMMENT '性别',
        'phone' varchar(60) DEFAULT NULL COMMENT '手机号',
        'email' varchar(300) DEFAULT NULL COMMENT '邮箱',
        'address' varchar(150) DEFAULT NULL COMMENT '用户地址',
        'signature' varchar(240) DEFAULT NULL COMMENT '用户签名',
        'avatar' varchar(300) DEFAULT NULL COMMENT '用户头像',
        'qr_code' varchar(300) DEFAULT NULL COMMENT '二维码',
        'register_from' int(11) DEFAULT '0' COMMENT '1--App注册，2--微信小程序注册，3--支付宝注册，4--数据初始化',
        'last_login_time' datetime DEFAULT NULL COMMENT '最后一次登录时间',
        'status' int(11) DEFAULT '1' COMMENT '1--可用，2--注销，3--暂停使用',
        'openid' varchar(100) DEFAULT NULL COMMENT '用户的第三方id',
        'nick_name' varchar(150) DEFAULT NULL COMMENT '用户昵称',
        'blood_type' int(2) DEFAULT NULL COMMENT '血型:\r\n1：A型\r\n2：B型\r\n3：AB型\r\n4：O型\r\n5：HR型',
        PRIMARY KEY ('u_id') USING BTREE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;

### 订单信息表
    CREATE TABLE 'order_info' (
        'id' bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
        'order_source' varchar(32) DEFAULT NULL COMMENT '订单来源，如乘客服务系统',
        'source_order_id' bigint(20) NOT NULL COMMENT '源订单id，如乘客服务系统订单id',
        'order_no' varchar(32) NOT NULL COMMENT '订单编号',
        'begin_station_id' int(11) NOT NULL COMMENT '出发站id',
        'end_station_id' int(11) NOT NULL COMMENT '终点站id',
        'ticket_number' int(11) NOT NULL COMMENT '车票数量',
        'buy_uid' bigint(11) NOT NULL COMMENT '购票用户id',
        'start_time' datetime NOT NULL COMMENT '开始时间',
        'travel_id' bigint(20) DEFAULT NULL COMMENT '行程id，关联travel_info表',
        'create_time' datetime NOT NULL COMMENT '订单创建时间',
        PRIMARY KEY ('id')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

### 订单用户信息拓展表
    CREATE TABLE 'order_user_link' (
        'id' bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
        'user_id' bigint(20) NOT NULL,
        'order_id' bigint(20) NOT NULL,
        PRIMARY KEY ('id')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单用户信息扩展表';

### 行程信息表
    CREATE TABLE 'travel_info' (
        'id' bigint(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
        'source_travel_id' varchar(50) DEFAULT NULL COMMENT '源行程id，如乘客服务系统行程id',
        'begin_station_id' int(11) NOT NULL COMMENT '出发站点id',
        'end_station_id' int(11) NOT NULL COMMENT '终点站点id',
        'travel_status' int(11) NOT NULL DEFAULT '0' COMMENT '0-预约中；1-预约成功；4-预约失败；9-取消预约',
        'start_time' datetime DEFAULT NULL COMMENT '行程开始时间',
        'distance' decimal(12,2) DEFAULT NULL COMMENT '行程距离',
        'expected_time' varchar(255) DEFAULT NULL COMMENT '全程预计时长',
        'all_travel_plat' varchar(255) DEFAULT NULL COMMENT '站点行程路线',
        'driver_content' varchar(255) DEFAULT NULL COMMENT '司机提示信息',
        'car_id' int(11) DEFAULT NULL COMMENT '车辆id',
        'driver_id' int(11) DEFAULT NULL COMMENT '司机id',
        'create_time' datetime DEFAULT NULL COMMENT '行程创建时间',
        'update_time' datetime DEFAULT NULL COMMENT '行程更新时间',
        PRIMARY KEY ('id')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程信息表';

### 行程票务信息
    CREATE TABLE 'travel_ticket_info' (
        'id' bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
        'travel_id' bigint(20) NOT NULL COMMENT '行程id',
        'user_id' bigint(20) NOT NULL COMMENT '乘车用户id',
        'seat_num' char(2) NOT NULL COMMENT '座位编号',
        PRIMARY KEY ('id')
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行程票务信息';