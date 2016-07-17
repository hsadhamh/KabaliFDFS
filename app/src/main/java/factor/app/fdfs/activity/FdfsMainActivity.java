package factor.app.fdfs.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.rey.material.widget.ImageButton;
import com.rey.material.widget.ListView;
import com.rey.material.widget.Spinner;
import com.rey.material.widget.TextView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import factor.app.fdfs.providers.FdfsDataProvider;
import factor.app.fdfs.R;
import factor.app.fdfs.adapters.CinemasAdapter;
import factor.app.fdfs.adapters.MoviesSpinnerAdapter;
import factor.app.fdfs.models.CinemaInfo;
import factor.app.fdfs.models.MovieInTheatresInfo;
import factor.app.fdfs.models.MovieInfo;

public class FdfsMainActivity extends AppCompatActivity {

    String[] mStrCities = {"Not Set", "bengaluru", "chennai", "madurai"};
    ArrayList<MovieInfo> mListMovies = new ArrayList<>();
    ArrayList<CinemaInfo> mListCinemas = new ArrayList<>();
    String mCityName = "";

    @BindView(R.id.spinner_cities)
    Spinner mSpinnerCities;
    @BindView(R.id.spinner_movies)
    Spinner mSpinnerMovies;
    @BindView(R.id.spinner_cinemas)
    ListView mSpinnerCinemas;
    @BindView(R.id.id_fab_next_screen)
    FloatingActionButton fabNext;
    @BindView(R.id.btn_calendar_click)
    ImageButton calendar;
    @BindView(R.id.txt_booking_date)
    TextView mTxtDate;
    @BindView(R.id.adView)
    AdView adView;

    InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fdfs_main);
        ButterKnife.bind(this);

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        mTxtDate.setText(String.format("%02d", day) + "-" + String.format("%02d", month) + "-" + year);

        ArrayAdapter<String> spFoodAdapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.layout_spinner_item, mStrCities);
        spFoodAdapter.setDropDownViewResource(R.layout.layout_spinner_item);
        mSpinnerCities.setAdapter(spFoodAdapter);
        mSpinnerCinemas.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mSpinnerCities.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                SelectedCity(position);
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDatePicker();
            }
        });

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });
        // Request for Ads
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("CC5F2C72DF2B356BBF0DA198") //Random Text
                .build();
        // Load ads into Banner Ads
        adView.loadAd(adRequest);

        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(FdfsMainActivity.this);
        interstitial.setAdUnitId("ca-app-pub-123456789/123456789");
        interstitial.loadAd(adRequest);
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });
    }

    public void SelectedCity(int position){
        if(position == 0)
            return;
        String sCity = mStrCities[position];
        Log.d("City Selected ", sCity);
        mCityName = sCity;
        if(!sCity.equals("Not Set")) {
            new BackgroundOps().execute();
        }
    }

    private class BackgroundOps extends AsyncTask<Void, Void, Void> {
        MaterialDialog dialog = null;
        boolean internetCOnnection = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(FdfsMainActivity.this)
                    .content("Loading Movies & Cinemas...")
                    .progress(true, 0)
                    .cancelable(false)
                    .progressIndeterminateStyle(false).show();
            dialog.show();
        }

        private boolean isNetworkConnected() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo() != null;
        }

        public boolean isInternetAvailable() {
            try {
                InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
                return !ipAddr.equals("");
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                internetCOnnection = (isNetworkConnected() && isInternetAvailable());
                if(internetCOnnection) {
                    mListMovies.addAll(FdfsDataProvider.getFullMoviesList(mCityName));
                    mListCinemas.addAll(FdfsDataProvider.getCinemasList(mCityName));
                    Collections.sort(mListMovies, new Comparator<MovieInfo>() {
                        @Override
                        public int compare(MovieInfo o1, MovieInfo o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!internetCOnnection)
            {
                new MaterialDialog
                        .Builder(FdfsMainActivity.this)
                        .title("No Internet Connection")
                        .content("Please check your internet connection.")
                        .positiveText("OK")
                        .show();
                dialog.dismiss();
                mSpinnerCities.setSelection(0);
                return;
            }
            if(mListMovies.isEmpty() || mListCinemas.isEmpty())
            {
                String sStr = "";
                if(mListCinemas.isEmpty())
                    sStr = "Cinemas";
                else if(mListMovies.isEmpty())
                    sStr = "Movies";
                new MaterialDialog
                        .Builder(FdfsMainActivity.this)
                        .title("Failed to get ["+sStr+"].")
                        .content("Please contact app administrator.")
                        .positiveText("OK")
                        .show();
                dialog.dismiss();
                mSpinnerCities.setSelection(0);
                return;
            }
            MoviesSpinnerAdapter adapter = new MoviesSpinnerAdapter(getApplicationContext(), R.layout.layout_spinner_item, mListMovies);
            mSpinnerMovies.setAdapter(adapter);

            final CinemasAdapter CineAdapter = new CinemasAdapter(getApplicationContext(), R.layout.layout_list_item, mListCinemas);
            mSpinnerCinemas.setAdapter(CineAdapter);
            mSpinnerCinemas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CineAdapter.toggleSelection(position);
                }
            });
            dialog.dismiss();
        }

    }

    @OnClick(R.id.id_fab_next_screen)
    public void onClickFab(){
        MovieInTheatresInfo movieTheatre = new MovieInTheatresInfo();
        CinemasAdapter cineAdpater = (CinemasAdapter) mSpinnerCinemas.getAdapter();
        if(cineAdpater ==null || cineAdpater.getSelectedIds() == null) return;

        for(int i=0; i < cineAdpater.getSelectedIds().size(); i++) {
            if(cineAdpater.getSelectedIds().get(i)){
                movieTheatre.getCinemas().add(cineAdpater.getListMovies().get(i));
            }
        }

        int nPos = mSpinnerMovies.getSelectedItemPosition();
        movieTheatre.setMovie(mListMovies.get(nPos));
        movieTheatre.setCity(mCityName);
        String dateString = (String) mTxtDate.getText();
        String [] dates = dateString.split("-");
        movieTheatre.setDate(dates[2] + dates[1] +  dates[0]);

        if(!movieTheatre.getCinemas().isEmpty()) {
            try {
                String s = FdfsDataProvider.getSavedInfoString(movieTheatre);
                FdfsDataProvider.writeToFile(getApplicationContext(),s);

                Intent i = new Intent();
                i.setClass(this, BackgroundCheckActivity.class);
                startActivity(i);

                FdfsDataProvider.scheduleAlarm(getApplicationContext());
                finish();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void ShowDatePicker(){
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(FdfsMainActivity.this,
                new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        mTxtDate.setText(String.format("%02d", dayOfMonth) + "-" + String.format("%02d", monthOfYear + 1) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }
}
