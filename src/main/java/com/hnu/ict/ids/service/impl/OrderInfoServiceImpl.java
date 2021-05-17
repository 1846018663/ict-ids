package com.hnu.ict.ids.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.Kafka.KafkaProducera;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.mapper.OrderInfoMapper;

import com.hnu.ict.ids.mapper.OrderUserLinkMapper;
import com.hnu.ict.ids.mapper.TravelInfoMapper;
import com.hnu.ict.ids.service.NetworkLogService;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelTicketInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
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

    @Autowired
    NetworkLogService networkLogServer;
    @Autowired
    TravelTicketInfoService travelTicketInfoService;

    @Autowired
    KafkaProducera kafkaProducera;


    @Value("${travel.algorithm.cancels.url}")
    private String URL;

    @Value("${travel.algorithm.cancel.car.url}")
    private String cancel_car_url;

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
        JSONArray array=JSONArray.parseArray(uIds);
        for (int i=0;i<array.size();i++){
            JSONObject jsonObjec=array.getJSONObject(i);
            OrderUserLink orderUserLink=new OrderUserLink();
            orderUserLink.setOrderNo(orderInfo.getOrderNo());
            orderUserLink.setUserId(BigInteger.valueOf(jsonObjec.getInteger("u_id")));
            if(jsonObjec.getString("seat_preference")!=null){
                orderUserLink.setSeatPreference(jsonObjec.getString("seat_preference"));
            }
            orderUserLink.setState(1);
            orderUserLinkMapper.insert(orderUserLink);
        }


       orderMapper.insert(orderInfo);


    }

    @Transactional
    @Override
    public ResultEntity deleteSourceOrderId(OrderInfo order, OrderInfoHistotry orderInfoHistotry, String uIds){
        ResultEntity resultEntity=new ResultEntity();
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



        //业务判断如果未生成行程不做一下操作    如果已经行程存在取消订单需调用算法完成行程取消流程
        //追加条件如果是包车取消走新业务流程
        if(order.getCharteredBus()!=null || (order.getTravelId()==null && order.getStatus()==1)){
            JSONObject charteredJSON=new JSONObject();
            charteredJSON.put("o_id",order.getSourceOrderId());
            charteredJSON.put("from_p_id",order.getBeginStationId().shortValue());
            charteredJSON.put("to_p_id",order.getEndStationId());
            charteredJSON.put("start_time", DateUtil.getCurrentTime(order.getStartTime()));
            if(order.getCharteredBus()==null){
                charteredJSON.put("chartered_bus","-1");
            }else{
                charteredJSON.put("chartered_bus",order.getCharteredBus());
            }

            charteredJSON.put("it_number",ids.length);

            //包车业务取消   删除订单数据并且通知算法是否车辆信息资源
            try {
                logger.info("包车运力删除"+charteredJSON.toJSONString());
                String delCHartered=HttpClientUtil.doPostJson(cancel_car_url,charteredJSON.toJSONString());
                logger.info("包车运力删除结果"+delCHartered);
                JSONObject delCHarteredJson=JSONObject.parseObject(delCHartered);
                resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
                resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            } catch (Exception e) {
                resultEntity.setMessage(ResutlMessage.FAIL.getValue());
                resultEntity.setCode(ResutlMessage.FAIL.getName());
                e.printStackTrace();
            }
        }else{

            try {
                if(!StringUtils.isEmpty(order.getTravelId()) && order.getTicketNumber()>0){
                    TravelInfo travelInfo=travelInfoMapper.findTravelId(order.getTravelId());

                    JSONArray seat_info=new JSONArray();
                    JSONObject seatJson=new JSONObject();
                    seatJson.put("car_id",travelInfo.getCarId());
                    //查询座位
                    List<TravelTicketInfo> travelTicketInfoList= travelTicketInfoService.findPassengerSeating(travelInfo.getTravelId());
                    JSONArray correspond_seat_id=new JSONArray();
                    for (int i=0; i<travelTicketInfoList.size();i++){
                        JSONObject ob=new JSONObject();
                        TravelTicketInfo info=travelTicketInfoList.get(i);
                        ob.put("u_id",info.getUserId());
                        ob.put("seat",info.getSeatNum());
                        correspond_seat_id.add(ob);
                    }
                    seatJson.put("correspond_seat_id",correspond_seat_id);
                    seat_info.add(seatJson);
                    travelInfoCancels(order,ids,travelInfo.getItNumber(),seat_info);
                    //删除座位数据
                    for (int i=0;i<ids.length;i++){
                        logger.info("======="+ids[i]);
                        Integer id=Integer.parseInt(ids[i]);
                        logger.info(travelInfo.getTravelId()+"操作座位表"+id);
                        travelTicketInfoService.delTraveIdSeat(travelInfo.getTravelId(),id);
                    }

                }
                resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
                resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            } catch (NumberFormatException e) {
                resultEntity.setMessage(ResutlMessage.FAIL.getValue());
                resultEntity.setCode(ResutlMessage.FAIL.getName());
                e.printStackTrace();
            }
        }



        //判断订单所属成功是否全部移除
        int count=orderUserLinkMapper.findRemove(order.getOrderNo());

        if(count==0){
            orderMapper.deleteBySourceOrderId(order.getSourceOrderId());
            travelInfoMapper.updateTravlInfoStatus(order.getTravelId());
        }

        //通知大数据删除
       TravelInfo info= travelInfoMapper.findTravelId(order.getTravelId());
        if(info!=null){
            boolean bool=true;
            List<OrderInfo > list=orderMapper.findOrderTravelId(info.getTravelId());
            for (OrderInfo orderInfo:list){
                if(orderInfo.getCharteredBus()==null){
                    bool=false;
                }
            }

            if(bool){
                //包车
                kafkaProducera.getTripInfo(info,2);
            }else{
                //包车
                kafkaProducera.getTripInfo(info,1);
            }

        }
        return resultEntity;
    }




    @Override
    public OrderInfo getById(int id){
        return orderMapper.getById(id);
    }


    /**
     * 取消行程算法调用
     * @param order
     * @param ids
     */
    public void travelInfoCancels(OrderInfo order,String[] ids,Integer ticketNumber,JSONArray seatJson){
        //调用算法接口
        JSONObject json=new JSONObject();

        JSONObject dl=new JSONObject();
        dl.put("o_id",order.getSourceOrderId());
        dl.put("from_p_id",order.getBeginStationId());
        dl.put("to_p_id",order.getEndStationId());
        dl.put("start_time", DateUtil.getCurrentTime(order.getStartTime()));
        dl.put("ticket_number",ids.length+"/"+ticketNumber);
        String order_u_id="";
        for (int i=0;i<ids.length;i++){
            order_u_id=order_u_id+ids[i]+",";
        }
        order_u_id=order_u_id.substring(0,order_u_id.length()-1);
        dl.put("order_u_id",order_u_id);
        JSONArray taskJson=new JSONArray();
        JSONObject task=new JSONObject();
        TravelInfo info=travelInfoMapper.findTravelId(order.getTravelId());
        task.put("correspond_order_number",info.getCorrespondOrderNumber());
        task.put("driver_id",info.getDriverId());
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

        task.put("start_time",DateUtil.getCurrentTime(info.getStartTime()));
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


        logger.info("取消行程id查询"+order.getTravelId()+"/");
        List<OrderInfo> orderList= orderMapper.findOrderTravelId(order.getTravelId());
        String oIds="";
        if(orderList!=null &&orderList.size()>0){
            for (int i=0;i<orderList.size();i++){
                oIds=oIds+orderList.get(i).getSourceOrderId()+",";
            }
            oIds= oIds.substring(0,oIds.length()-1);
            task.put("correspond_order_id",oIds);
            taskJson.add(task);
        }



        //封装最后传参
        json.put("dl_1",dl);
        json.put("task_record",taskJson);
        json.put("seat_info",seatJson);
        logger.info("发送请求数据"+json.toString());
        //保存预警信息
        NetworkLog networkLog=new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setInterfaceInfo(NetworkEnum.ALGORITHM_INRERFACE_DEL.getValue());
        networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(URL);
        networkLog.setAccessContent(json.toString());

        try {
            String body= HttpClientUtil.doPostJson(URL,json.toString());
            logger.info("接收请求数据"+body);
            JSONObject jsonObject=JSONObject.parseObject(body);
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            //status 1为成功
            if(jsonObject.getInteger("status")==1){
                //解析入库
                jsonFind(jsonObject);
            }



        } catch (Exception e) {
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }
        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);
    }


    /**
     * 取消行程算法  返回内容入库
     * @param jsonObject
     */
    public  void jsonFind(JSONObject jsonObject){
        JSONArray array=jsonObject.getJSONArray("task");
        //对数据进行解析
        List<TravelInfo> travelInfoList=new ArrayList<>();
        Map<String,String > map=new HashMap<>();
        for (int i=0;i<array.size();i++){
            JSONObject object=array.getJSONObject(i);
            TravelInfo info=new TravelInfo();
            if(object.getInteger("from_p_id")==null){
                info.setBeginStationId(null);
            }else{
                info.setBeginStationId(object.getInteger("from_p_id"));
            }

            if(object.getInteger("to_p_id")==null){
                info.setEndStationId(null);
            }else{
                info.setEndStationId(object.getInteger("to_p_id"));
            }


            info.setTravelStatus(1);
            info.setItNumber(object.getInteger("it_number"));
            info.setStartTime(DateUtil.strToDate(object.getString("start_time")));
            if(object.getDouble("distance")==null){
                info.setDistance(null);
            }else{
                info.setDistance(new BigDecimal(object.getDouble("distance")));
            }

            if(object.getInteger("expected_time")==null){
                info.setExpectedTime(null);
            }else{
                info.setExpectedTime(object.getInteger("expected_time").toString());
            }


            info.setDriverContent(object.getString("driver_content"));
            info.setAllTravelPlat(object.getString("all_travel_plat"));
            if(object.getInteger("car_id")==null){
                info.setCarId(null);
            }else{
                info.setCarId(object.getInteger("car_id"));
            }

            if (object.getString("from_order_name")==null){
                info.setBeginStationName(null);
            }else{
                info.setBeginStationName(object.getString("from_order_name"));
            }

            if (object.getString("to_order_name")==null){
                info.setEndStationName(null);
            }else{
                info.setEndStationName(object.getString("to_order_name"));
            }

            if (object.getInteger("park_id")==null){
                info.setParkId(null);
            }else{
                info.setParkId(object.getInteger("park_id"));
            }

            if (object.getString("park_name")==null){
                info.setParkName(null);
            }else{
                info.setParkName(object.getString("park_name"));
            }




            info.setWarning(object.getString("warning"));
            String taskerId =object.getString("travel_id");
            info.setTravelId(taskerId);
            //info.setDriverId(object.getInteger()); //司机id
            info.setUpdateTime(new Date());
            //获取订单与行程对应数据关联
            if(object.getString("correspond_order_id")!=null) {
                String orderIds=object.getString("correspond_order_id");
                String[] ids=orderIds.split(",");
                for (int k=0;k<ids.length;k++){
                    map.put(ids[k],taskerId);
                }
                System.out.println("解析车辆对应关系"+ orderIds);
            }



            info.setModifyId(object.getString("modify_id"));

            System.out.println("封装"+ map.toString());
            TravelInfo travelInfo=null;
            if(info.getModifyId()!=null && info.getModifyId().length()>0){
                //添加行程数据
                travelInfo= travelInfoMapper.findTravelId(info.getModifyId());
                info.setTravelId(info.getModifyId());
            }else{
                //添加行程数据
                travelInfo= travelInfoMapper.findTravelId(info.getTravelId());
            }

            info.setId(travelInfo.getId());
            travelInfoMapper.updateById(info);
        }



    }

    @Transactional
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



    public List<OrderInfo> findNotTransportCapacity(String statDate, String endDate){
        return orderMapper.findNotTransportCapacity(statDate,endDate);
    }


   public int deleteBySourceOrderId(String  sourceOrderId){
        return orderMapper.deleteBySourceOrderId(sourceOrderId);
    }


    public void updateById(OrderInfo orderInfo){
        orderMapper.updateById(orderInfo);
    }



    public List<OrderInfo> findPushFailedOrderIinfo(){
        return orderMapper.findPushFailedOrderIinfo();
    }


    public OrderInfo findOrderNo(String orderNo){
        return orderMapper.findOrderNo(orderNo);
    }
}
