package com.hnu.ict.ids.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.entity.OrderUserLink;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.mapper.OrderInfoMapper;

import com.hnu.ict.ids.mapper.OrderUserLinkMapper;
import com.hnu.ict.ids.mapper.TravelInfoMapper;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @description: 业务实现
 * @author: yuanhx
 * @create: 2021/01/07
 */

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    Logger logger=LoggerFactory.getLogger(OrderInfoServiceImpl.class);

    @Autowired
    OrderInfoMapper orderMapper;

    @Autowired
    OrderInfoHistotryMapper orderInfoHistotryMapper;

    @Autowired
    OrderUserLinkMapper orderUserLinkMapper;

    @Autowired
    TravelInfoMapper travelInfoMapper;


    @Value("${travel.algorithm.cancels.url}")
    private String URL;

    public List<OrderInfo> findNotTrave(String statDate, String endDate){
        return orderMapper.findNotTrave(statDate,endDate);
    }

    @Override
    public OrderInfo getBySourceOrderId(String sourceOrderId) {

        return orderMapper.getBySourceOrderId(sourceOrderId);
    }


    @Override
    @Transactional
    public void insertOrder(OrderInfo orderInfo,String uIds){

        //对关联   表进行添加
        String[]  ids=uIds.split(",");
        for (int i=0;i<ids.length;i++){
            OrderUserLink orderUserLink=new OrderUserLink();
            orderUserLink.setOrderNo(orderInfo.getOrderNo());
            orderUserLink.setUserId(new BigInteger(ids[i]));
            orderUserLink.setState(1);
            orderUserLinkMapper.insert(orderUserLink);
        }


       orderMapper.insert(orderInfo);


    }

    @Transactional
    @Override
    public void deleteSourceOrderId(OrderInfo order, OrderInfoHistotry orderInfoHistotry,String uIds){
        String[] ids=uIds.split(",");
        for (int i=0;i< ids.length;i++){
            logger.info(new BigInteger(ids[i])+"end of input integer"+order.getId());
            orderUserLinkMapper.updateOrderUserLinkState(new BigInteger(ids[i]),order.getOrderNo());
        }

        //修改订单表座位数
        orderMapper.updateOrderNumber(ids.length,order.getId());
        OrderInfoHistotry orderInfoHistotryRes= orderInfoHistotryMapper.getBySourceOrderId(order.getSourceOrderId());


        //如果订单历史表未创建  新增操作
        if(orderInfoHistotryRes==null){
            orderInfoHistotryMapper.insert(orderInfoHistotry);
        }

        //判断订单所属成功是否全部移除
        int count=orderUserLinkMapper.findRemove(order.getOrderNo());
        if(count==0){
            orderMapper.deleteBySourceOrderId(order.getSourceOrderId());
        }

        //业务判断如果未生成行程不做一下操作    如果已经行程存在取消订单需调用算法完成行程取消流程
        if(!StringUtils.isEmpty(order.getTravelId())){
            travelInfoCancels(order,ids.length,order.getTicketNumber());
        }

    }




    @Override
    public OrderInfo getById(int id){
        return orderMapper.getById(id);
    }


    /**
     * 取消行程算法调用
     * @param order
     * @param number
     */
    public void travelInfoCancels(OrderInfo order,Integer number,Integer ticketNumber){
        //调用算法接口
        JSONObject json=new JSONObject();

        JSONArray dlJSON=new JSONArray();
        JSONObject dl=new JSONObject();
        dl.put("o_id",order.getId());
        dl.put("from_p_id",order.getBeginStationId());
        dl.put("to_p_id",order.getEndStationId());
        dl.put("start_time", DateUtil.getCurrentTime(order.getStartTime()));
        dl.put("ticket_number",number+"/"+ticketNumber);
        dlJSON.add(dl);

        JSONArray taskJson=new JSONArray();
        JSONObject task=new JSONObject();
        TravelInfo info=travelInfoMapper.findTravelId(order.getTravelId());

        task.put("it_number",info.getItNumber());
        task.put("car_id",info.getCarId());
        task.put("from_p_id",info.getBeginStationId());
        task.put("from_order_name",info.getBeginStationName());
        task.put("to_p_id",info.getEndStationId());
        task.put("to_order_name",info.getEndStationName());
        if(info.getParkId()!=null){
            task.put("park_id",info.getParkId());
        }else{
            task.put("park_id","");
        }
        if(info.getParkName()!=null){
            task.put("park_name",info.getParkName());
        }else{
            task.put("park_name","");
        }

        task.put("start_time",info.getStartTime());
        task.put("travel_id",info.getTravelId());
        task.put("driver_content",info.getDriverContent());
        task.put("all_travel_plat",info.getAllTravelPlat());
        task.put("expected_time",info.getExpectedTime());
        task.put("distance",info.getDistance());
        if(info.getModifyId()!=null){
            task.put("modify_id",info.getModifyId());
        }else{
            task.put("modify_id","");
        }
        if(info.getWarning()!=null){
            task.put("warning",info.getWarning());
        }else{
            task.put("warning","");
        }


        logger.info("行程id查询"+order.getTravelId()+"/");
        List<OrderInfo> orderList= orderMapper.findOrderTravelId(order.getTravelId());
        String oIds="[";
        if(orderList!=null &&orderList.size()>0){
            for (int i=0;i<orderList.size();i++){
                oIds=oIds+orderList.get(i).getId()+",";
            }
            oIds.substring(0,oIds.length()-1);
            oIds=oIds+"]";
            task.put("correspond_order_id",oIds);
            taskJson.add(task);
        }

        //封装最后传参
        json.put("dl_1",dlJSON);
        json.put("task_record",taskJson);
        logger.info("发送请求数据"+json.toString());
        try {
            String body= HttpClientUtil.doPostJson(URL,json.toString());
            logger.info("接收请求数据"+body);
            JSONObject jsonObject=JSONObject.parseObject(body);

            if(jsonObject.getInteger("status")==0){
                //解析入库
                jsonFind(jsonObject);
                //修改订单人数
                order.setTicketNumber(order.getTicketNumber()-number);
                orderMapper.updateById(order);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 取消行程算法  返回内容入库
     * @param jsonObject
     */
    public  void jsonFind(JSONObject jsonObject){
        JSONArray array=jsonObject.getJSONArray("task");
        //对数据进行解析
        List<TravelInfo> travelInfoList=new ArrayList<>();
        Map<Integer,String > map=new HashMap<>();
        for (int i=0;i<array.size();i++){
            JSONObject object=array.getJSONObject(i);
            OrderInfo orderInfo=orderMapper.getById(object.getInteger("i_id"));
            TravelInfo info=new TravelInfo();
            info.setBeginStationId(object.getInteger("from_p_id"));
            info.setEndStationId(object.getInteger("to_p_id"));
            info.setTravelStatus(1);
            info.setItNumber(object.getInteger("it_number"));
            info.setStartTime(DateUtil.strToDate(object.getString("start_time")));
            info.setDistance(new BigDecimal(object.getDouble("distance")));
            info.setExpectedTime(object.getInteger("expected_time").toString());
            info.setDriverContent(object.getString("driver_content"));
            info.setAllTravelPlat(object.getString("all_travel_plat"));
            info.setCarId(object.getInteger("car_id"));
            info.setBeginStationName(object.getString("from_order_name"));
            info.setEndStationName(object.getString("to_order_name"));
            info.setParkName(object.getString("park_name"));
            info.setParkId(object.getInteger("park_id"));
            info.setWarning(object.getString("warning"));
            String taskerId =object.getString("travel_id");
            info.setTravelId(taskerId);
            //info.setDriverId(object.getInteger()); //司机id
            info.setCreateTime(new Date());
            //获取订单与行程对应数据关联
            String orderIds=object.getString("correspond_order_id").replace("[","").replace("]","").replace(" ","");
            String[] ids=orderIds.split(",");
            for (int k=0;k<ids.length;k++){
                map.put(Integer.parseInt(ids[k]),taskerId);
            }
            info.setModifyId(object.getString("modify_id"));


            System.out.println("解析车辆对应关系"+ orderIds);
            System.out.println("封装"+ map.toString());
            travelInfoList.add(info);
        }

        //添加行程数据
        travelInfoMapper.addTravelInfo(travelInfoList);
        //修改订单与行程对应关系
        for(Map.Entry<Integer,String> entry : map.entrySet()){
            //key为订单id    value为行程
            OrderInfo order= orderMapper.getById(entry.getKey());
            order.setTravelId(entry.getValue());
            order.setTravelSource(0);//默认算法来源  0
            orderMapper.updateById(order);

        }

    }


    public void updateByIdList(List<OrderInfo> list,Integer status){
        for (int i=0;i<list.size();i++){
            OrderInfo order=list.get(i);
            order.setStatus(status);
            orderMapper.updateById(order);
        }
    }


    public List<OrderInfo> findCompensatesOrderIinfo(){
        return  orderMapper.findCompensatesOrderIinfo();
    }

    public List<OrderInfo> findOrderTravelId(String travelId){
        return orderMapper.findOrderTravelId(travelId);
    }

}
