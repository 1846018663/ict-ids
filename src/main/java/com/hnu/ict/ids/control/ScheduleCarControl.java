package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.ReqCallbackAsync;
import com.hnu.ict.ids.bean.*;
import com.hnu.ict.ids.config.UtilConf;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(tags = "包车业务API")
@RestController
@RequestMapping("/schedule")
public class ScheduleCarControl {
    Logger logger = LoggerFactory.getLogger(ScheduleCarControl.class);

    @Value("${travel.algorithm.charter.url}")
    private String URL;
    @Value("${travel.algorithm.charterCar.seat.url}")
    private String charterCar_seat_url;


    @Autowired
    NetworkLogService networkLogServer;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    OrderUserLinkService orderUserLinkService;

    @Autowired
    TravelTicketInfoService travelTicketInfoService;

    @Autowired
    ReqCallbackAsync reqCallbackAsync;


    @RequestMapping(value = "/addCharteredBus", method = RequestMethod.POST)
    public ResultEntity addOrder(@RequestBody String body) {
        logger.info("包车业务发送数据"+body);
        ResultEntity result = new ResultEntity();
        JSONObject json = JSONObject.parseObject(body);
        //验证o_id    SourceOrderId是否已经存在
        String oId = json.getString("o_id");

        OrderInfo orderInfo = orderInfoService.getBySourceOrderId(oId);

        if (orderInfo != null) {
            result.setCode(ResutlMessage.FAIL.getName());
            result.setMessage("该订单已经存在，请勿重复提交");
            logger.info("新增包车订单" + result.toString());
            return result;
        }
        //创建数据对象
        OrderInfo order = new OrderInfo();
        order.setSourceOrderId(oId);
        order.setBeginStationId(json.getInteger("from_p_id"));
        order.setEndStationId(json.getInteger("to_p_id"));
        order.setTicketNumber(json.getInteger("ticket_number"));
        order.setBuyUid(json.getBigInteger("buy_uid"));
        String startTime = DateUtil.strToDateLong(json.getString("start_time"));
        String createTime = DateUtil.strToDateLong(json.getString("create_time"));
        order.setStartTime(DateUtil.strToDate(startTime));
        order.setCreateTime(DateUtil.strToDate(createTime));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSource("乘客服务系统");
        order.setStatus(0);//初始化
        order.setTravelSource(null);
        //是否包车属性
        order.setCharteredBus(json.getString("chartered_bus"));
        JSONArray jsonArray = new JSONArray();
        if (json.getJSONArray("seat_preferences") != null) {
            jsonArray = json.getJSONArray("seat_preferences");
        } else {
            String ids = json.getString("u_ids");
            String[] id = ids.split(",");
            for (int i = 0; i < id.length; i++) {
                JSONObject object = new JSONObject();
                object.put("u_id", id[i]);
                object.put("seat_preference", "");
                jsonArray.add(object);

            }
        }
        orderInfoService.insertOrder(order, jsonArray.toString());

        //查询是否存在乘客id数据
        OrderInfo reqOrder= orderInfoService.getBySourceOrderId(oId);
        List<OrderUserLink>  userList=orderUserLinkService.findOrderNo(reqOrder.getOrderNo());

        //订单数据保存完成  进行数据算法调用
        JSONArray bodyJson = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("oId", order.getSourceOrderId().trim());
        object.put("fromId", order.getBeginStationId().toString());
        object.put("toId", order.getEndStationId().toString());
        object.put("startTime", DateUtil.getCurrentTime(order.getStartTime()));
        object.put("charteredBus", order.getCharteredBus());
        if(userList!=null && userList.size()>0){
            object.put("ticketNumber",userList.size()+"");
        }else{
            object.put("ticketNumber","");
        }
        bodyJson.add(object);

        //接口访问日志操作
        NetworkLog networkLog = new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setInterfaceInfo(NetworkEnum.ALGORITHM_SCHEDULE.getValue());
        networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(URL);
        networkLog.setAccessContent(object.toJSONString());
        try {
            logger.info("向算法发送包车业务数据" + object.toJSONString());
            body = HttpClientUtil.doPostJson(URL, object.toJSONString());
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        } catch (Exception e) {
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            result.setCode("301");
            result.setMessage("算法服务异常，请稍后再试。。。");
            result.setResult("");
            //异步处理
            reqCallbackAsync.reqCallback(result,null);
            e.printStackTrace();
        }

        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);

        //解析算法返回数据内容
        logger.info("包车业务算法返回结果" + body);
        JSONObject jsonObject = JSONObject.parseObject(body);
        int code = jsonObject.getInteger("status");
        if (code == 201) {
            //行程数据保存
            JSONArray array = jsonObject.getJSONArray("task");

            for (int i = 0; i < array.size(); i++) {
                JSONObject reObject = array.getJSONObject(i);
                TravelInfo info = new TravelInfo();
                info.setBeginStationId(reObject.getInteger("fromId"));
                //根据出发站台查询归属城市
                if (reObject.getInteger("fromId") != null) {
                    IvsAppPlatformInfo appPlatformInfo = ivsAppPlatformInfoService.getByPlatformId(reObject.getInteger("fromId").toString());
                    info.setCCode(appPlatformInfo.getCCode());
                }
                String taskerId = null;
                info.setEndStationId(reObject.getInteger("toId"));
                info.setTravelStatus(1);//预约成功
                info.setItNumber(reObject.getInteger("itNumber"));
                info.setStartTime(DateUtil.strToDate(reObject.getString("startTime")));
                info.setDistance(new BigDecimal(reObject.getDouble("distance")));
                info.setExpectedTime(reObject.getInteger("expectedTime").toString());
                info.setDriverContent(reObject.getString("driverContent"));
                info.setAllTravelPlat(reObject.getString("travelPlat"));
                info.setArriveTime(reObject.getString("startTime") + "," + reObject.getString("arriveTime"));
                info.setCarId(Integer.parseInt(reObject.getString("carId")));
                info.setBeginStationName(reObject.getString("fromName"));
                info.setEndStationName(reObject.getString("toName"));
                info.setParkName(reObject.getString("parkName"));
                info.setParkId(reObject.getInteger("parkId"));
                info.setDriverId(reObject.getInteger("driverId"));
                info.setWarning(reObject.getString("warning"));
                info.setCorrespondOrderNumber(reObject.getString("correspondNumber"));
//                info.setCorrespondOrderId(reObject.getString("correspondOrderId"));
                taskerId = reObject.getString("travelId");
                info.setTravelId(taskerId);
                info.setCreateTime(new Date());
                //保存行程
                travelInfoService.insert(info);
                OrderInfo updateOrder = orderInfoService.getBySourceOrderId(order.getSourceOrderId());
                updateOrder.setTravelId(taskerId);
                orderInfoService.updateById(updateOrder);
                //返回乘客服务系统
                JSONObject resultObject = new JSONObject();
                resultObject.put("travel_id", info.getTravelId());
                resultObject.put("distance", info.getDistance());
                resultObject.put("expected_time", info.getExpectedTime());
                resultObject.put("all_travel_plat", info.getAllTravelPlat());
                resultObject.put("driver_content", info.getDriverContent());
                resultObject.put("c_id", info.getCarId());
                resultObject.put("driver_id", info.getDriverId());
                resultObject.put("chartered_bus", order.getCharteredBus());
                resultObject.put("reservation_status", 1);
                resultObject.put("it_number", info.getItNumber());
                resultObject.put("ret_status", 1);
                resultObject.put("o_id", order.getSourceOrderId());
                resultObject.put("waiting_space", null);
                resultObject.put("oper_time", DateUtil.strToDayDate(new Date()));

                //座位信息
                TravelInfo travelInfo = travelInfoService.findTravelId(taskerId);
                //乘客座位信息获取封装
                List<CustomerTicketInfoRequset> ticketInfoList = new ArrayList<>();
                if (userList != null) {
                    //调用算法获取座位信息
                    List<SeatBeanRequest> beatLis = new ArrayList<>();

                    SeatBeanRequest seatBean = new SeatBeanRequest();
                    seatBean.setTravelId(travelInfo.getTravelId());
                    seatBean.setCarId(travelInfo.getCarId().toString());
                    seatBean.setCorrespondOrderId(order.getSourceOrderId());
                    seatBean.setCorrespondNumber(travelInfo.getCorrespondOrderNumber());
                    //查询数据该行程所属乘客
                    List<OrderInfo> orderList = orderInfoService.findOrderTravelId(travelInfo.getTravelId());
                    List<SeatUserRequset> userLists = new ArrayList<>();
                    List<SeatPreferenceRequset> seatPreferenceList = new ArrayList<>();
                    for (int p = 0; p < orderList.size(); p++) {
                        SeatUserRequset userBean = new SeatUserRequset();
                        OrderInfo orders = orderList.get(p);
                        userBean.setOrderId(orders.getSourceOrderId());
                        List<OrderUserLink> userLinks = orderUserLinkService.findOrderNo(orders.getOrderNo());
                        String userId = "";
                        for (OrderUserLink orderUser : userLinks) {
                            SeatPreferenceRequset seatPreference = new SeatPreferenceRequset();
                            seatPreference.setUserId(orderUser.getUserId().toString());
                            seatPreference.setSeatPreference(orderUser.getSeatPreference());
                            seatPreferenceList.add(seatPreference);

                            userId = userId + orderUser.getUserId().toString() + ",";

                        }
                        userId = userId.substring(0, userId.length() - 1);
                        userBean.setUserId(userId);
                        userLists.add(userBean);
                    }
                    //查询座位表
                    seatBean.setUserPreference(seatPreferenceList);
                    seatBean.setOrderUserId(userLists);
                    beatLis.add(seatBean);

                    String seatJson = JSON.toJSONString(beatLis);
                    String seatBody;
                    try {
                        List<TravelTicketInfo> travelTicketInfoList = new ArrayList<>();
                        logger.info("发送获取乘客座位信息参数：" + seatJson);
                        seatBody = HttpClientUtil.doPostJson(charterCar_seat_url, seatJson);
                        logger.info("接收获取乘客座位信息参数：" + seatBody);
                        JSONArray arrSeat = JSONArray.parseArray(seatBody);
                        for (int j = 0; j < arrSeat.size(); j++) {
                            JSONObject objectJons = arrSeat.getJSONObject(j);
                            JSONArray arrayJson = objectJons.getJSONArray("correspondSeatId");
                            for (int k = 0; k < arrayJson.size(); k++) {
                                JSONObject ob = arrayJson.getJSONObject(k);
                                String sourceOrderId = ob.getString("orderId");
                                OrderInfo infoOrder = orderInfoService.getBySourceOrderId(sourceOrderId);
                                JSONArray seatID = ob.getJSONArray("seatId");
                                for (int z = 0; z < seatID.size(); z++) {
                                    TravelTicketInfo travelTicketInfo = new TravelTicketInfo();
                                    JSONObject seatOb = seatID.getJSONObject(z);

                                    travelTicketInfo.setTravelId(infoOrder.getTravelId());
                                    travelTicketInfo.setUserId(seatOb.getInteger("userId"));
                                    travelTicketInfo.setSeatNum(seatOb.getString("seat"));
                                    travelTicketInfoList.add(travelTicketInfo);
                                }

                            }

                        }

                        travelTicketInfoService.insertTravelTicketInfoList(travelTicketInfoList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    for (OrderInfo infos : orderList) {
                        List<OrderUserLink> ticket_info = orderUserLinkService.findOrderNo(infos.getOrderNo());
                        CustomerTicketInfoRequset ticketInfo = new CustomerTicketInfoRequset();
                        ticketInfo.setO_id(infos.getSourceOrderId());
                        List<Tickets> ticketsList = new ArrayList<>();
                        for (OrderUserLink user : ticket_info) {
                            Tickets tickets = new Tickets();
                            //根据订单查出乘客user_id   通过user_id与行程查询用户座位号
                            TravelTicketInfo travelTicketInfo = travelTicketInfoService.findTraveIdSeat(travelInfo.getTravelId(), user.getUserId().intValue());
                            if (travelTicketInfo != null) {
                                tickets.setU_id(travelTicketInfo.getUserId().intValue());
                                tickets.setSeat_number(travelTicketInfo.getSeatNum());
                                ticketsList.add(tickets);
                            }

                        }
                        ticketInfo.setTickets(ticketsList);
                        ticketInfoList.add(ticketInfo);
                    }
                }
                JSONArray arr=new JSONArray();
                for (CustomerTicketInfoRequset ticketInfo: ticketInfoList){
                    List<Tickets> list=ticketInfo.getTickets();
                    for (Tickets tickets:list){
                        JSONObject ticJson=new JSONObject();
                        ticJson.put("seat_number",tickets.getSeat_number());
                        ticJson.put("u_id",tickets.getU_id()) ;
                        arr.add(ticJson);
                    }
                }
                resultObject.put("tickets", arr);

                result.setCode("201");
                result.setMessage(jsonObject.getString("suggest"));
                result.setResult(resultObject);


                logger.info("包车预约结果"+JSON.toJSONString(result));
                //异步处理
                reqCallbackAsync.reqCallback(result,info);
            }

            } else {
                result.setCode("301");
                result.setMessage(jsonObject.getString("suggest"));
                result.setResult(order.getSourceOrderId());
                //异步处理
                reqCallbackAsync.reqCallback(result,null);
        }
        return result;
    }

    @RequestMapping(value = "/queryAvailableGroups", method = RequestMethod.POST)
    public ResultEntity queryAvailableGroups(@RequestBody String body){
        ResultEntity resultEntity=new ResultEntity();
        JSONObject json = JSONObject.parseObject(body);
        Date stime=DateUtil.strToDateyyyyMMddHHmmss(json.getString("stime"));
        Date etime=DateUtil.strToDateyyyyMMddHHmmss(json.getString("etime"));
        String routeCode=json.getString("routeCode");
        String fromPId=json.getString("from_p_id");
        String toPId=json.getString("to_p_id");

        //获得数据调用算法接口



        return resultEntity;
    }

}

