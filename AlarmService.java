package com.biblealarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    
    private static final String CHANNEL_ID = "AlarmServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        
        // 服务启动后可以在这里处理额外的逻辑
        // 比如振动、闪光灯等
        
        return START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "圣经闹钟服务",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("圣经闹钟后台服务");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, 
            notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("圣经闹钟")
            .setContentText("闹钟正在响铃...")
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}