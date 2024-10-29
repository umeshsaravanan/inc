import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/output")
public class OutputWebSocket {

    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    String sessionId = null;

    @OnOpen
    public void onOpen(Session session) {
        String queryString = session.getRequestURI().getQuery();

        if (queryString != null) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "sessionId".equals(pair[0])) {
                    sessionId = pair[1];
                }
            }
        }

        if (sessionId != null) {
            sessions.put(sessionId, session);
            System.out.println("Session opened: " + sessionId);
        } else {
            System.err.println("Session ID not provided!");
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.entrySet().removeIf(entry -> entry.getKey().equals(sessionId));
        System.out.println("Session closed: " + sessionId);
    }

    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket Error: " + throwable.getMessage());
        throwable.printStackTrace();
    }

    public synchronized static void sendOutput(String sessionId, String data) {
        Session clientSession = null;
        if(sessions.containsKey(sessionId))
            clientSession = sessions.get(sessionId);
        if (clientSession != null && clientSession.isOpen()) {
            try {
                clientSession.getBasicRemote().sendText(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
