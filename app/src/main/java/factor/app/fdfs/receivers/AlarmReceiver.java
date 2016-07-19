package factor.app.fdfs.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import factor.app.fdfs.service.CheckTicketsService;

/**
 * Created by hassanhussain on 7/17/2016.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {
    public static final int REQUEST_CODE = 6045;
    public static final String ACTION = "factor.app.fdfs.alarm";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getName(), "on receive - for alarm receiver.");
        Intent i = new Intent(context, CheckTicketsService.class);
        startWakefulService(context, i);
    }
}
