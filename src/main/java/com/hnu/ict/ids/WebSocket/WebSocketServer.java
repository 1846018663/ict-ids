package com.hnu.ict.ids.WebSocket;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.annotation.Resource;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.CarPosition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Slf4j
@ServerEndpoint(value = "/websocket/{appId}")
@Component
public class WebSocketServer {

    private static int onlineCount = 0;

    private static ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketServer>> webSocketSetMap = new ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketServer>>();

    private Session session;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;




    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("appId") String appId) {
        this.session = session;
        CopyOnWriteArraySet<WebSocketServer> webSocketSet = webSocketSetMap.get(appId);
        if (CollectionUtils.isEmpty(webSocketSet)) {
            webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();
        }
        webSocketSet.add(this);
        webSocketSetMap.put(appId, webSocketSet);
        addOnlineCount();
        log.info("有新连接加入,门店号:{}, sessionId:{}, 当前在线人数为:{}", appId, session.getId(), getOnlineCount());
        List<CarPosition> list=new ArrayList<>();

        Map<String, String> map = new HashMap<>();
        map.put("1","31.343375,121.280737");
        map.put("12","31.335508,121.281756");
        map.put("21","31.371762,121.255854");


        for (Map.Entry<String, String> entry : map.entrySet()) {
            CarPosition car=new CarPosition();
            car.setId(entry.getKey());
            String[] arr=entry.getValue().split(",");
            car.setLongitude(arr[1]);
            car.setLatitude(arr[0]);
            list.add(car);
        }

        String str= JSON.toJSONString(list);

        try {
            sendMessage(str);
        } catch (IOException e) {
            log.error("websocket IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("shopId") String shopId) {
        if(StringUtils.hasText(shopId)){
            CopyOnWriteArraySet<WebSocketServer> webSocketSet = webSocketSetMap.get(shopId);
            if (!CollectionUtils.isEmpty(webSocketSet)) {
                webSocketSet.remove(this);
            }
            subOnlineCount();
            log.info("有一连接关闭,门店号:{}, sessionId:{},当前在线人数为", shopId, this.session.getId(), getOnlineCount());

        }

    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("来自客户端的消息:" + message);
    }

    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(String message) throws IOException {
        if(StringUtils.hasText(message)){
            this.session.getBasicRemote().sendText(message);
        }

    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo(String message, String shopId) throws IOException {
        log.info(message);
        CopyOnWriteArraySet<WebSocketServer> webSocketSet = webSocketSetMap.get(shopId);
        if (CollectionUtils.isEmpty(webSocketSet)) {
            return;
        }
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
