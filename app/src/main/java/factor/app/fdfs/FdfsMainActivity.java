package factor.app.fdfs;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.ListView;
import com.rey.material.widget.Spinner;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

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
                            return o1.Name.compareTo(o2.Name);
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
}
