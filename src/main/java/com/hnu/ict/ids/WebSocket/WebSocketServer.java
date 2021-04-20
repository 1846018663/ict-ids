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
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Component
@ServerEndpoint(value = "/localPathWebsocket/{clientId}")
@Log4j2
public class WebSocketServer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //在线clientId数
    public static int onlineNumber = 0;

    //以用户的clientId，WebSocket为对象保存起来
    public static Map<String, WebSocketServer> clients = new ConcurrentHashMap<>();

    //会话
    private Session session;

    //客户端名称
    private String clientId;

    /**
     * 链接websocket
     *
     * @param session 会话
     */
    @OnOpen
    public void onOpen(@PathParam("clientId") String clientId, Session session) {
        for (String s : clients.keySet()) {
            this.clientId = clientId;
            if (s.equals(clientId)) {
                onClose();
            }
        }
        log.info(clientId + "    连接成功!!!!!!");
        this.session = session;
        this.clientId = clientId;
        try {
            //把自己的信息加入到map当中去
            clients.put(clientId, this);
//            sendMessageAll("success");
        } catch (Exception e) {
            logger.info(clientId + "链接发生了错误" + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.info("服务端发生了错误" + error.getMessage());
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        onlineNumber--;
        clients.remove(clientId);
        try {
            if (clients.size() > 0) {
                sendMessageAll(clientId + "退出");
            }
        } catch (IOException e) {
            logger.info(clientId + "发生了错误");
        }
    }

    /**
     * 收到客户端的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            logger.info("来自客户端消息：" + message + "客户端的id是：" + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("发生了错误了");
        }
    }

    public void sendMessageAll(String message) throws IOException {
        if (clients != null && clients.size() > 0) {
            for (WebSocketServer item : clients.values()) {
                item.session.getAsyncRemote().sendText(message);
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineNumber;
    }
}