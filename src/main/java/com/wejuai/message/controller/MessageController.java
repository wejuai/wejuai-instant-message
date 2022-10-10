package com.wejuai.message.controller;

import com.endofmaster.commons.util.json.JsonUtils;
import com.wejuai.entity.mongo.SendMessage;
import com.wejuai.entity.mysql.ChatUserRecord;
import com.wejuai.entity.mysql.User;
import com.wejuai.message.config.Constant;
import com.wejuai.message.controller.request.SendMessageRequest;
import com.wejuai.message.controller.request.SendMessageResponse;
import com.wejuai.message.repository.AccountsRepository;
import com.wejuai.message.repository.ChatUserRecordRepository;
import com.wejuai.message.repository.SendMessageRepository;
import com.wejuai.message.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Optional;

import static com.wejuai.message.config.Constant.MAPPER;
import static com.wejuai.message.config.Constant.USER_CONNECTIONS;
import static com.wejuai.message.config.Constant.USER_KEY;

/**
 * @author ZM.Wang
 */
public class MessageController extends TextWebSocketHandler {

    private final static Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final SendMessageRepository sendMessageRepository;
    private final UserRepository userRepository;
    private final ChatUserRecordRepository chatUserRecordRepository;
    private final AccountsRepository accountsRepository;

    public MessageController(SendMessageRepository sendMessageRepository, UserRepository userRepository, ChatUserRecordRepository chatUserRecordRepository, AccountsRepository accountsRepository) {
        this.sendMessageRepository = sendMessageRepository;
        this.userRepository = userRepository;
        this.chatUserRecordRepository = chatUserRecordRepository;
        this.accountsRepository = accountsRepository;
    }

    /**
     * 链接进入
     * 连接地址格式为 /webSocket/{发送人}
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.debug("从请求获取的信息: " + session.getHandshakeHeaders().entrySet());
        String uri;
        if (session.getUri() == null || session.getUri().isOpaque()) {
            logger.warn("获取不到连接地址");
            sendErrorMsg(HttpStatus.SC_BAD_REQUEST, "获取不到连接地址", session);
            session.close();
            return;
        }
        uri = session.getUri().getPath();
        logger.debug("打开连接sessionId: " + session.getId() + ",获取连接地址为: " + uri);
        String accountsId = uri.substring(11);
        if (!accountsRepository.existsById(accountsId)) {
            sendErrorMsg(HttpStatus.SC_BAD_REQUEST, "没有该帐号: " + accountsId, session);
            session.close();
            return;
        }
        User user = getUserByAccountsId(accountsId);
        if (user.getBan()) {
            sendErrorMsg(HttpStatus.SC_FORBIDDEN, "该账号已被封禁: " + accountsId, session);
            session.close();
            return;
        }
        USER_CONNECTIONS.put(session.getId(), session);
        USER_KEY.put(session.getId(), accountsId);
        USER_KEY.put(accountsId, session.getId());
    }

    /**
     * 接收消息处理
     */
    @Override
    @Transactional
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String json = new String(message.asBytes());
        if (StringUtils.equals("heart", json)) {
            return;
        }
        logger.debug("接收消息体: " + json);
        if (!JsonUtils.isJson(json)) {
            sendErrorMsg(HttpStatus.SC_BAD_REQUEST, "消息格式错误", session);
            return;
        }
        //获取请求体
        SendMessageRequest request = MAPPER.readValue(json, SendMessageRequest.class);

        if (StringUtils.isBlank(request.getMessage()) && request.getMediaType() == null) {
            return;
        }
        //获取发送者和接收者以及聊天关系
        String accountsId = USER_KEY.get(session.getId());
        User sender = getUserByAccountsId(accountsId);
        logger.debug("查询到的sender: " + sender);
        if (sender.getBan()) {
            sendErrorMsg(HttpStatus.SC_FORBIDDEN, "该账号已被封禁: " + accountsId, session);
            session.close();
            return;
        }
        User recipient = getUser(request.getRecipient());
        logger.debug("查询到的recipient: " + recipient);
        if (recipient.getDel()) {
            sendErrorMsg(HttpStatus.SC_BAD_REQUEST, "对方账号已注销: " + accountsId, session);
            return;
        }
        if (recipient.getBan()) {
            sendErrorMsg(HttpStatus.SC_BAD_REQUEST, "对方账号已被封禁: " + accountsId, session);
            return;
        }
        ChatUserRecord senderRecord = chatUserRecordRepository.findBySenderAndRecipientAndDelFalse(recipient, sender);
        logger.debug("查询出的senderRecord: " + senderRecord);
        if (senderRecord == null) {
            senderRecord = new ChatUserRecord(sender, recipient, null);
        }
        ChatUserRecord recipientRecord = chatUserRecordRepository.findBySenderAndRecipientAndDelFalse(sender, recipient);
        logger.debug("查询出的recipientRecord: " + recipientRecord);
        if (recipientRecord == null) {
            recipientRecord = new ChatUserRecord(recipient, sender, null);
        }

        //存储消息内容
        SendMessage sendMessage;
        String lastText;
        if (request.getMediaType() != null) {
            sendMessage = new SendMessage(sender.getId(), recipient.getId(), request.getMediaType(), request.getOssKey(), recipientRecord.getId());
            lastText = request.getMediaType().getText();
        } else {
            sendMessage = new SendMessage(sender.getId(), recipient.getId(), request.getMessage(), recipientRecord.getId());
            lastText = request.getMessage();
        }

        userRepository.save(recipient.addMsg());
        sendMessageRepository.save(sendMessage);
        chatUserRecordRepository.save(senderRecord.lastText(lastText));
        chatUserRecordRepository.save(recipientRecord.lastText(lastText).addMsg());
        sendMessage(sendMessage, recipient.getAccounts().getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        logger.debug("断开连接:" + session.getId() + "，连接地址: " + session.getUri());
        removeSession(session);
        session.close();
    }

    private void sendMessage(SendMessage sendMessage, String accountsId) throws IOException {
        WebSocketSession session = getWebSocketSession(accountsId);
        if (session == null) {
            logger.warn("该用户不在线: " + accountsId);
            return;
        }
        if (!session.isOpen()) {
            logger.warn("该用户连接已断开: " + accountsId);
            removeSession(session);
            session.close();
        }
        try {
            String sendJson = Constant.MAPPER.writeValueAsString(new SendMessageResponse(sendMessage));
            session.sendMessage(new TextMessage(sendJson));
            logger.debug("发送消息消息体:" + sendJson);
        } catch (IOException ex) {
            logger.error("消息发送失败", ex);
        }
    }

    private WebSocketSession getWebSocketSession(String accountsId) {
        String sessionId = USER_KEY.get(accountsId);
        return Constant.USER_CONNECTIONS.get(sessionId);
    }

    private void removeSession(WebSocketSession session) {
        String accountsId = USER_KEY.get(session.getId());
        USER_KEY.remove(session.getId());
        USER_KEY.remove(accountsId);
        USER_CONNECTIONS.remove(session.getId());
    }

    private User getUserByAccountsId(String accountsId) {
        User user = userRepository.getUserByAccounts_Id(accountsId);
        if (user == null) {
            throw new RuntimeException("该帐号没有user: " + accountsId);
        }
        return user;
    }

    private User getUser(String id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("没有该帐号: " + id);
        }
        return userOptional.get();
    }

    private void sendErrorMsg(int code, String msg, WebSocketSession session) throws IOException {
        String sendJson = Constant.MAPPER.writeValueAsString(new SendMessageResponse(code, msg));
        session.sendMessage(new TextMessage(sendJson));
    }

}
