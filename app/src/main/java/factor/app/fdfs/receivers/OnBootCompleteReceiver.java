package factor.app.fdfs.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

import factor.app.fdfs.providers.FdfsDataProvider;
import factor.app.fdfs.service.CheckTicketsService;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class OnBootCompleteReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getName(), "On Receive for Boot complete.");
        Intent i = new Intent();
        i.putExtra("REQUEST", 100);
        i.setClass(context, CheckTicketsService.class);
        startWakefulService(context, i);
    }
}
