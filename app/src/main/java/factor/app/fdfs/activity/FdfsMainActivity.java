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
import com.rey.material.widget.ListView;
import com.rey.material.widget.Spinner;

import org.json.JSONException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fdfs_main);
        ButterKnife.setDebug(true);
        ButterKnife.bind(this);

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
        movieTheatre.setDate("20160722");

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
}
