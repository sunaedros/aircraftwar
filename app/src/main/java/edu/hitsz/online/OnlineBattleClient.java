package edu.hitsz.online;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlineBattleClient {
    public interface Listener {
        void onWaiting();

        void onStarted(int playerId, String selfName, String opponentName);

        void onState(OnlineBattleState state);

        void onGameOver(OnlineBattleState state);

        void onError(String message);
    }

    private static final int CONNECT_TIMEOUT_MS = 5000;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final String host;
    private final int port;
    private final String playerName;
    private final Listener listener;
    private final Object sendLock = new Object();
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();

    private Socket socket;
    private volatile PrintWriter writer;
    private Thread readThread;
    private volatile boolean connected;

    public OnlineBattleClient(String host, int port, String playerName, Listener listener) {
        this.host = host;
        this.port = port;
        this.playerName = playerName;
        this.listener = listener;
    }

    public void connect() {
        if (readThread != null) {
            return;
        }
        readThread = new Thread(this::runConnection, "online-battle-client");
        readThread.start();
    }

    public void sendScore(int score) {
        try {
            sendAsync(new JSONObject()
                    .put("type", "score")
                    .put("score", score));
        } catch (JSONException e) {
            postError("Cannot build score message");
        }
    }

    public void sendDead(int score) {
        try {
            sendAsync(new JSONObject()
                    .put("type", "dead")
                    .put("score", score));
        } catch (JSONException e) {
            postError("Cannot build death message");
        }
    }

    public void close() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        sendExecutor.shutdownNow();
    }

    private void runConnection() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            connected = true;
            send(new JSONObject()
                    .put("type", "join")
                    .put("name", playerName));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (connected && (line = reader.readLine()) != null) {
                    handleMessage(line);
                }
            }
        } catch (IOException | JSONException e) {
            if (connected) {
                postError("Socket error: " + e.getMessage());
            }
        } finally {
            connected = false;
        }
    }

    private void handleMessage(String line) throws JSONException {
        JSONObject message = new JSONObject(line);
        String type = message.optString("type");
        if ("waiting".equals(type)) {
            post(listener::onWaiting);
        } else if ("start".equals(type)) {
            int playerId = message.optInt("playerId", 0);
            String selfName = message.optString("name", playerName);
            String opponentName = message.optString("opponentName", "Opponent");
            post(() -> listener.onStarted(playerId, selfName, opponentName));
        } else if ("state".equals(type)) {
            OnlineBattleState state = parseState(message);
            post(() -> listener.onState(state));
        } else if ("game_over".equals(type)) {
            OnlineBattleState state = parseGameOver(message);
            post(() -> listener.onGameOver(state));
        } else if ("error".equals(type)) {
            postError(message.optString("message", "Unknown server error"));
        }
    }

    private OnlineBattleState parseState(JSONObject message) {
        return new OnlineBattleState(
                message.optString("p1Name", "Player 1"),
                message.optInt("p1Score", 0),
                message.optBoolean("p1Alive", false),
                message.optString("p2Name", "Player 2"),
                message.optInt("p2Score", 0),
                message.optBoolean("p2Alive", false)
        );
    }

    private OnlineBattleState parseGameOver(JSONObject message) {
        return new OnlineBattleState(
                message.optString("p1Name", "Player 1"),
                message.optInt("p1Score", 0),
                false,
                message.optString("p2Name", "Player 2"),
                message.optInt("p2Score", 0),
                false
        );
    }

    private void send(JSONObject message) {
        PrintWriter currentWriter = writer;
        if (currentWriter == null || !connected) {
            return;
        }
        synchronized (sendLock) {
            currentWriter.println(message.toString());
        }
    }

    private void sendAsync(JSONObject message) {
        if (!connected || sendExecutor.isShutdown()) {
            return;
        }
        sendExecutor.execute(() -> send(message));
    }

    private void post(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void postError(String message) {
        post(() -> listener.onError(message));
    }
}
