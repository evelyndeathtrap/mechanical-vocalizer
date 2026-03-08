package com.x.evee.sys;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import java.io.FileInputStream;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start the service and close the UI
        startService(new Intent(this, VibrationService.class));
        finish();
    }

    public static class VibrationService extends Service {
        @Override
        public void onCreate() {
            super.onCreate();
            // Create notification to keep service in foreground
            String channelId = "VIB_CHANNEL";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Vib", NotificationManager.IMPORTANCE_LOW);
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
            }
            Notification notification = new Notification.Builder(this, channelId)
                    .setContentTitle("Vibration Engine Active")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .build();
            startForeground(1, notification);

            // Run the loop
            new Thread(new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            }).start();
        }

        private void loop() {
    byte[] b = new byte[1];
    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    while (!Thread.currentThread().isInterrupted()) {
        // Open the file, read, and close in every single iteration
        try (FileInputStream fis = new FileInputStream("/dev/random")) {
            if (fis.read(b) != -1) {
                int amp = (b[0] & 0xFF);
                if (amp > 10 && v != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(amp, Math.min(amp, 255)));
                    } else {
                        v.vibrate(amp);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("VibService", "Error reading file: " + e.getMessage());
        }

        try {
            Thread.sleep(100); // Throttle frequency
        } catch (InterruptedException e) {
            break;
        }
    }
}


        @Override
        public IBinder onBind(Intent i) { return null; }
    }
}
