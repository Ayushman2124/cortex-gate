package com.cortexgate.api.websocket;

import com.cortexgate.api.dto.TokenMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Maps requestId to WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // We will expect the client to send a message first with the requestId to bind the session
        // Or we could pass it in the query param: ws://localhost:8080/ws/chat?requestId=123
        String query = session.getUri().getQuery();
        if (query != null && query.contains("requestId=")) {
            String requestId = query.split("requestId=")[1].split("&")[0];
            sessions.put(requestId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Can be used if client sends the request over WS instead of REST
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session);
    }

    public void sendMessageToClient(TokenMessage tokenMessage) {
        WebSocketSession session = sessions.get(tokenMessage.getRequestId());
        if (session != null && session.isOpen()) {
            try {
                String payload = objectMapper.writeValueAsString(tokenMessage);
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
