package polytech.followit.service;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import polytech.followit.NavigationActivity;
import polytech.followit.R;

public class NotificationBroadcast extends BroadcastReceiver {

    private static final String TAG = NotificationBroadcast.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent viewIntent = new Intent(context, NavigationActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(context, 0, viewIntent, 0);
        switch (intent.getAction()) {
            case "NEXT_INSTRUCTION":
                String instruction = intent.getExtras().getString("instruction");
                int icon = intent.getExtras().getInt("icon");
                notificationBuilder(context, 1, icon, "Instruction", instruction, viewPendingIntent);
                break;
            case "ARRIVED_TO_DESTINATION":
                notificationBuilder(context, 1, R.drawable.ic_arrived,
                        "Arrivé !", "Vous êtes arrivé à destination", viewPendingIntent);
        }
    }

    public void notificationBuilder(Context context, int nId, int icon, String title, String body, @Nullable PendingIntent intent) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(intent)
                .setLocalOnly(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(1000)
                .setVibrate(new long[]{1000, 1000});

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // mId allows you to update the notification later on.
        notificationManager.notify(nId, mBuilder.build());
    }
}
