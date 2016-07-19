package factor.app.fdfs.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import factor.app.fdfs.R;
import factor.app.fdfs.activity.FactorSplashScreen;
import factor.app.fdfs.models.CinemaInfo;
import factor.app.fdfs.providers.FdfsDataProvider;
import factor.app.fdfs.models.MovieInTheatresInfo;
import factor.app.fdfs.events.UpdateUiEvent;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class CheckTicketsService extends IntentService {

    public CheckTicketsService() { super(CheckTicketsService.class.getName()); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getName(), "On Handle intent - for service.");
        int nRequest = 0;
        if(intent != null){
            nRequest = intent.getIntExtra("REQUEST", 0);
        }
        if(nRequest == 100) {
            Log.d(getClass().getName(), "On Handle intent - from On Boot.");
            WakefulBroadcastReceiver.completeWakefulIntent(intent);

            if (FdfsDataProvider.doesFileExists(getApplicationContext())) {
                FdfsDataProvider.scheduleAlarm(getApplicationContext());
            }
        }
        else {
            Log.d("debug-service", "Loading and updating movie info.");
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
            MovieInTheatresInfo movies = null;
            if (!FdfsDataProvider.doesFileExists(getApplicationContext())) {
                FdfsDataProvider.cancelAlarm(getApplicationContext());
                return;
            }

            try {
                String sJson = FdfsDataProvider.readFromFile(getApplicationContext());
                if ((sJson == null || sJson.isEmpty()))
                    return;

                movies = FdfsDataProvider.getSavedInfoObject(sJson);

                if (movies == null) return;

                int nTheatreID = 1250;

                for (CinemaInfo cine : movies.getCinemas()) {
                    String URL = FdfsDataProvider.mStrBookMyShow + movies.getCity() + cine.getCinemaLink();

                    if (cine.isBookingOpen()) continue;

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss");
                    String strDate = sdf.format(c.getTime());

                    if (FdfsDataProvider.isBookingOpenForTheDay(URL, movies.getDate())) {
                        cine.setCinemaStatus("[" + strDate + "]: Booking Open for the day. Not yet for movie.");
                        if (FdfsDataProvider.isBookingOpenForTheMovie(URL, movies.getDate(), movies.getMovie().getMovieID())) {
                            cine.setCinemaStatus("[" + strDate + "]: Booking Open for the day & movie.");
                            cine.setBookingOpen(true);
                            if(!cine.isAlreadyNotified()){
                                //  create notification now.
                                String str = "Booking open for the movie ["+
                                        movies.getMovie().getName()+"] on the selected date in the theatre ["+
                                        cine.getCinemaName() +"].";
                                CreateNotification(str, nTheatreID, cine.getCinemaName());
                                cine.setAlreadyNotified(true);
                            }
                        }
                    }
                    else
                        cine.setCinemaStatus(strDate +": Could not find booking for given date.");
                    nTheatreID++;
                }

                String s = FdfsDataProvider.getSavedInfoString(movies);
                FdfsDataProvider.writeToFile(getApplicationContext(), s);

                EventBus.getDefault().post(new UpdateUiEvent());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
        this.stopSelf();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void CreateNotification(String content, int notifyID, String cinemaName){
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, FactorSplashScreen.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, notifyID);

        Notification notification = new Notification();
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        // Build notification
        // Actions are just fake
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle("Bookings Open Now!!!")
                .setContentText("Booking open in theatre [ " + cinemaName + "].")
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.ic_movie_white_24dp)
                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setLights(Color.MAGENTA, 3000, 3000)
                .setDefaults(notification.defaults)
                .setStyle(new
                        NotificationCompat.BigTextStyle()
                        .bigText(content))
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(notifyID, noti);
    }
}
