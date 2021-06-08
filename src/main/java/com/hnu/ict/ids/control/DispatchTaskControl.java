package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.Kafka.KafkaProducera;
import com.hnu.ict.ids.bean.*;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@Api(tags = "调度任务API")
@RestController
@RequestMapping("/schedulingTask")
public class DispatchTaskControl {

    Logger logger = LoggerFactory.getLogger(DispatchTaskControl.class);

    @Autowired
    OrderInfoService orderInfoService;

    @Value("${travel.algorithm.url}")
    private String URL;
    @Value("${passenger.service.callback.url}")
    private String callback_URL;
    @Value("${travel.algorithm.response.url}")
    private String response_URL;

    @Value("${passenger.service.capacity.test.url}")
    private String capacity_url;

    @Value("${travel.algorithm.seat.url}")
    private String seat_url;


    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    NetworkLogService networkLogServer;

    @Autowired
    OrderUserLinkService orderUserLinkService;

    @Autowired
    TravelTicketInfoService travelTicketInfoService;

    @Autowired
    CarFormationService carFormationService;

    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;


    @Autowired
    KafkaProducera kafkaProducera;

    /**
     * 调用新增行程算法接口
     *
     * @return
     */
    @RequestMapping(value = "/findNotTrave", method = RequestMethod.GET)
    public PojoBaseResponse findNotTrave() {
        PojoBaseResponse result = new PojoBaseResponse();
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate = new Date();
        long time = 1000 * 60 * 30 + stateDate.getTime();
        Date endDate = DateUtil.millisecondToDate(time);
        List<OrderInfo> listOrder = orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate), DateUtil.getCurrentTime(endDate));
        //redis获取有效机制时间
        Map<String, Object> resultMap = redisTemplate.opsForHash().entries("setTimeConfig");
        if (listOrder.size() > 0) {
            //访问算法接口   创建行程
            JSONObject jsonObject = addTrave(listOrder, resultMap);
            List<TravelInfo> travelInfoList = new ArrayList<>();

            //有效数据
            if (jsonObject.getInteger("status") == 1) {
                Map<String, String> map = new HashMap<>();
                Map<String, String> mapResult = new HashMap<>();
                //车辆编队信息保存
//                saveLine(jsonObject);

                //行程数据保存
                JSONArray array = jsonObject.getJSONArray("task");
                List<TraveOrderTaskResult> list = JSONObject.parseArray(array.toJSONString(), TraveOrderTaskResult.class);
                Boolean bool = intoResultSaveTrave(list, travelInfoList, map, mapResult);

                if (bool) {
                    //数据操作成功回调乘客服务系统
                    String str = successfulTrip(mapResult, travelInfoList, listOrder);
                    logger.info("回调乘客服务系统接收参数" + str);
                }
            }

        }
        logger.info("执行任务完毕：" + new Date());

        return result;
    }


    /**
     * 解析行程数据并保存
     *
     * @param list
     * @param map
     * @param mapResult
     * @return
     */
    public Boolean intoResultSaveTrave(List<TraveOrderTaskResult> list, List<TravelInfo> travelInfoList, Map<String, String> map, Map<String, String> mapResult) {
        for (TraveOrderTaskResult taskResult : list) {
            TravelInfo info = new TravelInfo();
            info.setBeginStationId(Integer.parseInt(taskResult.getFromId()));
            //根据出发站台查询归属城市
            if (taskResult.getFromId() != null) {
                IvsAppPlatformInfo appPlatformInfo = ivsAppPlatformInfoService.getByPlatformId(taskResult.getFromId());
                info.setCCode(appPlatformInfo.getCCode());
            }
            String taskerId = null;
            info.setEndStationId(Integer.parseInt(taskResult.getToId()));
            info.setTravelStatus(1);//预约成功
            info.setItNumber(taskResult.getItNumber());
            info.setStartTime(DateUtil.strToDate(taskResult.getStartTime()));
            info.setDistance(new BigDecimal(taskResult.getDistance()));
            info.setExpectedTime(taskResult.getExpectedTime().toString());
            info.setDriverContent(taskResult.getDriverContent());
            info.setAllTravelPlat(taskResult.getTravelPlat());
            info.setArriveTime(taskResult.getStartTime() + "," + taskResult.getArriveTime());
            info.setCarId(Integer.parseInt(taskResult.getCarId()));
            info.setBeginStationName(taskResult.getFromName());
            info.setEndStationName(taskResult.getToName());
            info.setParkId(Integer.parseInt(taskResult.getParkId()));
            info.setDriverId(Integer.parseInt(taskResult.getDriverId()));
            info.setWarning(taskResult.getWarning());
            info.setCorrespondOrderNumber(taskResult.getCorrespondNumber());
            info.setModifyOrderId(taskResult.getModifyOrderId());
            taskerId = taskResult.getTravelId();

            info.setTravelId(taskerId);
            info.setCreateTime(new Date());
            //获取订单与行程对应数据关联 .replace("[","").replace("]","").replace(" ","")
            String orderIds = taskResult.getCorrespondOrderId();
            info.setCorrespondOrderId(orderIds);
            String[] ids = orderIds.split(",");
            for (int k = 0; k < ids.length; k++) {
                map.put(ids[k], taskerId);
                OrderInfo orderInfo = orderInfoService.getBySourceOrderId(ids[k]);
                mapResult.put(orderInfo.getSourceOrderId(), taskerId);
            }
            travelInfoList.add(info);
        }
        //解析数据封装后   service批量处理
        boolean bool = travelInfoService.addTravelInfoList(travelInfoList, map);

        return bool;
    }


    /**
     * 编队信息保存
     *
     * @param jsonObject
     */
    public void saveLine(JSONObject jsonObject) {
        JSONArray line = jsonObject.getJSONArray("line");
        if (line.size() > 0) {
            for (int k = 0; k < line.size(); k++) {
                JSONObject object = line.getJSONObject(k);
                CarFormation carFormation = new CarFormation();
                carFormation.setFId(object.getString("f_id"));
                carFormation.setFromPId(object.getInteger("from_p_id"));
                carFormation.setCarList(object.getString("car_list"));
                carFormation.setStartTime(DateUtil.strToDate(object.getString("start_time")));
                carFormation.setCreateTime(new Date());
                carFormationService.insert(carFormation);
            }
        }
    }


    /**
     * @param listOrder
     * @param resultMap
     * @return
     */
    public JSONObject addTrave(List<OrderInfo> listOrder, Map<String, Object> resultMap) {
        String body = null;
        List<TraveOrderTaskRequest> list = new ArrayList<>();
        for (OrderInfo info : listOrder) {
            if (info.getTicketNumber() == 0) {//订单已被取消
                return null;
            }
            TraveOrderTaskRequest taskRequest = intoTraveOrderTaskRequest(info, resultMap);
            list.add(taskRequest);
        }

        //对数据进行解析
        String json = JSON.toJSONString(list);
        //接口访问日志操作
        NetworkLog networkLog = intoNetworkLog(URL, NetworkEnum.ALGORITHM_INRERFACE_ADD.getValue(), json);
        try {
            logger.info("向算法发送请求数据" + json);
            body = HttpClientUtil.doPostJson(URL, json);
            logger.info("接收算法放回数据" + body);
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        } catch (Exception e) {
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }
        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);

        return JSONObject.parseObject(body);
    }


    /**
     * 网络请求日志对象封装
     *
     * @param url
     * @param value
     * @param json
     * @return
     */
    public NetworkLog intoNetworkLog(String url, String value, String json) {
        NetworkLog networkLog = new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setInterfaceInfo(value);
        networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(url);
        networkLog.setAccessContent(json);
        return networkLog;
    }


    /**
     * 初始化   新增行程算法输出对象
     *
     * @param info
     * @param resultMap
     * @return
     */
    public TraveOrderTaskRequest intoTraveOrderTaskRequest(OrderInfo info, Map<String, Object> resultMap) {
        TraveOrderTaskRequest task = new TraveOrderTaskRequest();
        task.setTicketNumber(info.getTicketNumber());
        task.setOId(info.getSourceOrderId());
        task.setFromId(info.getBeginStationId().toString());
        task.setToId(info.getEndStationId().toString());
        task.setStartTime(DateUtil.getCurrentTime(info.getStartTime()));
        task.setOrderTime(DateUtil.getCurrentTime(info.getCreateTime()));
//        if (info.getStatus() == 1) {
//            task.setOrderStatus(info.getStatus());
//        } else {
//            task.setOrderStatus(0);
//        }

        int DateTime = Integer.parseInt(resultMap.get(ConfigEnum.CARTIMECONFIG.getValue()).toString()) * 60;
        task.setSetTime(60);

        return task;
    }


    /**
     * 获取新增行程座位信息  访问算法API
     * @param travelInfo
     */
    public void getSeat(TravelInfo travelInfo) {
        List<SeatBeanRequest> beatLis = intoSeatBeanRequest(travelInfo);
        String seatJson = JSON.toJSONString(beatLis);
        String seatBody = null;

        NetworkLog networkLog = intoNetworkLog(seat_url, NetworkEnum.ALGORITHM_SEAT.getValue(), seatJson);
        try {
            logger.info("发送获取乘客座位信息参数：" + seatJson);
            seatBody = HttpClientUtil.doPostJson(seat_url, seatJson);
            logger.info("接收获取乘客座位信息参数：" + seatBody);
            networkLog.setResponseResult(seatBody);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        } catch (Exception e) {
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }
        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);
        JSONArray arrSeat = JSONArray.parseArray(seatBody);
        saveSeat(arrSeat);
    }


    /**
     * 保存行程分配座位信息
     * @param arrSeat
     */
    public void saveSeat(JSONArray arrSeat){
        List<TravelTicketInfo> travelTicketInfoList = new ArrayList<>();
        for (int i = 0; i < arrSeat.size(); i++) {
            JSONObject json = arrSeat.getJSONObject(i);
            JSONArray arr = json.getJSONArray("correspondSeatId");
            for (int k = 0; k < arr.size(); k++) {

                JSONObject ob = arr.getJSONObject(k);
                String sourceOrderId = ob.getString("orderId");
                OrderInfo orderInfo = orderInfoService.getBySourceOrderId(sourceOrderId);
                JSONArray seatID = ob.getJSONArray("seatId");
                for (int j = 0; j < seatID.size(); j++) {
                    TravelTicketInfo travelTicketInfo = new TravelTicketInfo();
                    JSONObject seatOb = seatID.getJSONObject(j);
                    travelTicketInfo.setTravelId(orderInfo.getTravelId());
                    travelTicketInfo.setUserId(seatOb.getInteger("userId"));
                    travelTicketInfo.setSeatNum(seatOb.getString("seat"));
                    travelTicketInfoList.add(travelTicketInfo);
                }

            }

        }

        travelTicketInfoService.insertTravelTicketInfoList(travelTicketInfoList);
    }


    /**
     * 封装新增行程座位分配接口输出参数
     *
     * @param travelInfo
     * @return
     */
    public List<SeatBeanRequest> intoSeatBeanRequest(TravelInfo travelInfo) {
        //调用算法获取座位信息
        List<SeatBeanRequest> beatLis = new ArrayList<>();
        SeatBeanRequest seatBean = new SeatBeanRequest();
        seatBean.setTravelId(travelInfo.getTravelId());
        seatBean.setCarId(travelInfo.getCarId().toString());
        seatBean.setCorrespondOrderId(travelInfo.getCorrespondOrderId());
        seatBean.setCorrespondNumber(travelInfo.getCorrespondOrderNumber());
        seatBean.setModifyOrderId(travelInfo.getModifyOrderId()+"");
        //查询数据该行程所属乘客
        List<SeatUserRequset> userList = new ArrayList<>();
        List<SeatPreferenceRequset> seatPreferenceList = new ArrayList<>();
        intoUserPreference(travelInfo.getTravelId(), userList, seatPreferenceList);

        //查询座位表
        seatBean.setUserPreference(seatPreferenceList);
        seatBean.setOrderUserId(userList);
        seatBean.setCarType(0);
        beatLis.add(seatBean);
        return beatLis;
    }


    public void intoUserPreference(String travelId, List<SeatUserRequset> userList, List<SeatPreferenceRequset> seatPreferenceList) {
        List<OrderInfo> orderList = orderInfoService.findOrderTravelId(travelId);
        for (int i = 0; i < orderList.size(); i++) {
            SeatUserRequset userBean = new SeatUserRequset();
            OrderInfo order = orderList.get(i);
            userBean.setOrderId(order.getSourceOrderId());
            List<OrderUserLink> userLinks = orderUserLinkService.findOrderNo(order.getOrderNo());
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
            userList.add(userBean);
        }
    }


    /**
     * 回调乘客服务系统封装  预约行程成功回调接口
     *
     * @param map
     * @param orderInfoList
     * @return
     */
    public String successfulTrip(Map<String, String> map, List<TravelInfo> travelInfoList,
                                 List<OrderInfo> orderInfoList) {
        List<String> travelIdList = new ArrayList<>();
        List<CustomerTravelRequset>  customerHttpAPIBeanList=intoCustomerTravel(travelInfoList,map,orderInfoList,travelIdList);

        String json = JSON.toJSONString(customerHttpAPIBeanList);
        //接口访问日志操作
        NetworkLog networkLog=intoNetworkLog(callback_URL,NetworkEnum.PASSENGRT_SERVICE_CALL_BACK.getValue(),json);
        String body = "";
        try {
            logger.info("行程预约成功传参内容" + json);
            body = HttpClientUtil.doPostJson(callback_URL, json);
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            logger.info("行程预约成功返回结果:" + body);
            updateTravel(body,travelInfoList,travelIdList);
        } catch (Exception e) {
            //失败   数据信息做修改  失败status 为2
            orderInfoService.updateByIdList(orderInfoList, 2);
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }

        networkLogServer.insertNetworkLog(networkLog);
        return body;
    }

    /**
     * 保存修改  乘客服务系统行程预约成功回调结果
     * @param result
     * @param travelInfoList
     * @param travelIdList
     */
    public void  updateTravel(String result,List<TravelInfo> travelInfoList,List<String> travelIdList){
        JSONObject resultJson = JSONObject.parseObject(result);
        String code = resultJson.getString("code");

        if (!code.equals("00008")) {
            //失败   数据信息做修改  失败status 为2
            travelInfoService.updateByIdList(travelInfoList, 2);
        } else {
            //成功   数据信息做修改  成功status 为1
            logger.info("kafka准备发消息");
            for (String id : travelIdList) {
                TravelInfo info = travelInfoService.findTravelId(id);
                kafkaProducera.getTripInfo(info, 1);
            }
            travelInfoService.updateByIdList(travelInfoList, 1);
        }
    }


    /**
     * 初始化  回调乘客服务系统预约成功   输出参数对象封装
     * @param travelInfoList
     * @param map
     * @param orderInfoList
     * @param travelIdList
     * @return
     */
    public  List<CustomerTravelRequset> intoCustomerTravel(List<TravelInfo> travelInfoList,Map<String, String> map,List<OrderInfo> orderInfoList,List<String> travelIdList){
        List<CustomerTravelRequset> customerHttpAPIBeanList = new ArrayList<>();
        for (TravelInfo travelInfo : travelInfoList) {
            CustomerTravelRequset customerHttpAPIBean = new CustomerTravelRequset();
            customerHttpAPIBean.setDistance(travelInfo.getDistance().doubleValue());
            travelIdList.add(travelInfo.getTravelId());

            String oIds = "";
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String mapKey = entry.getKey();
                oIds = oIds + mapKey + ",";
            }
            oIds = oIds.substring(0, oIds.length() - 1);
            customerHttpAPIBean.setO_ids(oIds);
            customerHttpAPIBean.setTravel_id(travelInfo.getTravelId());
            customerHttpAPIBean.setExpected_time(Integer.parseInt(travelInfo.getExpectedTime()));
            customerHttpAPIBean.setAll_travel_plat(travelInfo.getAllTravelPlat());
            customerHttpAPIBean.setDriver_content(travelInfo.getDriverContent());
            customerHttpAPIBean.setC_id(travelInfo.getCarId());
            if (travelInfo.getDriverId() == null) {
                customerHttpAPIBean.setDriver_id(0);
            } else {
                customerHttpAPIBean.setDriver_id(travelInfo.getDriverId());
            }

            customerHttpAPIBean.setReservation_status(travelInfo.getTravelStatus());
            customerHttpAPIBean.setIt_number(travelInfo.getItNumber());
            customerHttpAPIBean.setRet_status(1);
            customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));
            //向算法获取座位分配信息
            getSeat(travelInfo);

            //乘客座位信息获取封装
            List<CustomerTicketInfoRequset> ticketInfoList =getCustomerTicketInfo(orderInfoList,travelInfo.getTravelId());
            customerHttpAPIBean.setTicket_info(ticketInfoList);
            customerHttpAPIBeanList.add(customerHttpAPIBean);

        }
        return  customerHttpAPIBeanList;
    }


    /**
     * 封装读取从算法已分配座位信息  输出乘客服务系统座位信息
     * @param orderInfoList
     * @return
     */
    public List<CustomerTicketInfoRequset> getCustomerTicketInfo(List<OrderInfo> orderInfoList,String travelId){
        List<CustomerTicketInfoRequset> ticketInfoList = new ArrayList<>();
        for (OrderInfo info : orderInfoList) {
            List<OrderUserLink> ticket_info = orderUserLinkService.findOrderNo(info.getOrderNo());
            CustomerTicketInfoRequset ticketInfo = new CustomerTicketInfoRequset();
            ticketInfo.setO_id(info.getSourceOrderId());
            List<Tickets> ticketsList = new ArrayList<>();
            for (OrderUserLink user : ticket_info) {
                Tickets tickets = new Tickets();
                //根据订单查出乘客user_id   通过user_id与行程查询用户座位号
                TravelTicketInfo travelTicketInfo = travelTicketInfoService.findTraveIdSeat(travelId, user.getUserId().intValue());
                if (travelTicketInfo != null) {
                    tickets.setU_id(travelTicketInfo.getUserId().intValue());
                    tickets.setSeat_number(travelTicketInfo.getSeatNum());
                    ticketsList.add(tickets);
                }

            }
            ticketInfo.setTickets(ticketsList);
            ticketInfoList.add(ticketInfo);
        }

        return  ticketInfoList;
    }


    /**
     * 运力情况  回调乘客服务系统任务
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/quickResponse", method = RequestMethod.GET)
    public PojoBaseResponse getQuickResponse() {
        PojoBaseResponse result = new PojoBaseResponse();
        Date stateDate = new Date();
        long time = 1000 * 60 * 30 + stateDate.getTime();
        Date endDate = DateUtil.millisecondToDate(time);
        List<OrderInfo> orderList = orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate), DateUtil.getCurrentTime(endDate));

        for (OrderInfo order : orderList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("oId", order.getId() + "");
            jsonObject.put("fromId", order.getBeginStationId() + "");
            jsonObject.put("toId", order.getEndStationId() + "");
            jsonObject.put("startTime", DateUtil.getCurrentTime(order.getStartTime()));
            jsonObject.put("ticketNumber", order.getTicketNumber());
            logger.info("快速响应算法发送请求" + jsonObject.toJSONString());
            String body = null;
            NetworkLog networkLog= intoNetworkLog(response_URL, NetworkEnum.ALGORITHM_TRANSPORT.getValue(), jsonObject.toJSONString());
            try {
                body = HttpClientUtil.doPostJson(response_URL, jsonObject.toJSONString());
                logger.info("快速响应算法接收返回" + body);
                networkLog.setResponseResult(body);
                networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
                if (StringUtils.hasText(body)) {
                    JSONObject json = JSON.parseObject(body);
                    Integer status = json.getInteger("status");
                    Map<String, String> map = new HashMap<>();
                    map.put("o_id", order.getSourceOrderId());

                    map.put("message", json.getString("suggest"));
                    map.put("code", status.toString());
                    String jsonString = JSON.toJSONString(map);
                    logger.info("运力检测乘客服务系统发送参数" + jsonString);
                    String resultBody = null;
                    try {
                        resultBody = HttpClientUtil.doPostJson(capacity_url, jsonString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    logger.info("运力检测乘客服务系统返回结果" + resultBody);


                } else {
                    result.setData("");
                }
            } catch (Exception e) {
                networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
                e.printStackTrace();
            }
            networkLogServer.insertNetworkLog(networkLog);

        }
        return result;


    }


}
