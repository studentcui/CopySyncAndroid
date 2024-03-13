package com.example.copysync;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.net.URI;

public class WebSocketService extends Service {
    private WebSocketClient webSocketClient;
    private static final String CHANNEL_ID = "your_channel_id";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 创建通知
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("WebSocket Service")
                .setContentText("正在运行...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        // 将服务设置为前台服务
        startForeground(1, notification);

        if (intent != null) {
            String webSocketUrl = intent.getStringExtra("WebSocketUrl");
            connectWebSocket(webSocketUrl);
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void connectWebSocket(String webSocketUrl) {
        URI uri = URI.create(webSocketUrl);
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("WebSocket", "连接成功");
                System.out.println("connected!");
                // 连接成功后的操作
            }

            @Override
            public void onMessage(String s) {
                Log.i("WebSocket", "收到消息: " + s);
                System.out.println("receive:" + s);
                // 处理收到的消息
                // 获取ClipboardManager的实例
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建一个ClipData对象，其中包含一个MIME类型和要复制的数据
                ClipData clip = ClipData.newPlainText("label", s);
                // 将ClipData对象设置到剪贴板
                clipboard.setPrimaryClip(clip);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("WebSocket", "连接关闭: " + s);
                // 连接关闭后的操作
            }

            @Override
            public void onError(Exception e) {
                Log.i("WebSocket", "连接出错: " + e.getMessage());
                // 连接出错后的操作
            }
        };
        webSocketClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}

