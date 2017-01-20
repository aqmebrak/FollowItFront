package polytech.followit.service;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import polytech.followit.MainActivity;
import polytech.followit.R;

public class BroadcastResponseReceiver extends BroadcastReceiver {

    private static final String TAG = BroadcastResponseReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent viewIntent = new Intent(context, MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(context, 0, viewIntent, 0);
        createNotification(context,1, R.drawable.ic_play_light,"Titre","Corps de la notification",viewPendingIntent);
    }

    private void createNotification(Context context, int nId, int icon, String title, String body, PendingIntent intent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(intent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // mId allows you to update the notification later on.
        notificationManager.notify(nId, mBuilder.build());
    }
}
