package factor.app.fdfs.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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

                for (CinemaInfo cine : movies.getCinemas()) {
                    String URL = FdfsDataProvider.mStrBookMyShow + movies.getCity() + cine.getCinemaLink();

                    if (cine.isBookingOpen()) continue;

                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
                    String strDate = sdf.format(c.getTime());

                    if (FdfsDataProvider.isBookingOpenForTheDay(URL, movies.getDate())) {
                        cine.setCinemaStatus(strDate + ": Booking Open for the day. Not yet for movie.");
                        if (FdfsDataProvider.isBookingOpenForTheMovie(URL, movies.getDate(), movies.getMovie().getMovieID())) {
                            cine.setCinemaStatus(strDate + ": Booking Open for the day & movie.");
                            cine.setBookingOpen(true);
                        }
                    }
                    else
                        cine.setCinemaStatus(strDate +": Could not find booking for given date.");
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
}
