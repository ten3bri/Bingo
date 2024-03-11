package com.example.bingo;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;
public class WebSocketManager {
    private static final String SERVER_URL = "wss://your_server_url"; // Zmień to na rzeczywisty adres serwera WebSocket

    private WebSocket webSocket;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    private MessageListener messageListener;

    public WebSocketManager(MessageListener listener) {
        this.messageListener = listener;
        initWebSocket();
    }

    private void initWebSocket() {
        try {
            webSocket = new WebSocketFactory().createSocket(SERVER_URL);
            webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onTextMessage(WebSocket websocket, String text) throws Exception {
                    messageListener.onMessageReceived(text);
                }
            });
            webSocket.connectAsynchronously();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.sendText(message);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null && webSocket.isOpen()) {
            webSocket.disconnect();
        }
    }
}
