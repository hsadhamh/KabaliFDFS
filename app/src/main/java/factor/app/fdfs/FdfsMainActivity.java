package factor.app.fdfs;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

public class FdfsMainActivity extends AppCompatActivity {

    String[] mStrCities = {"Not Set", "bengaluru", "chennai", "mumbai"};
    ArrayList<MovieInfo> mListMovies = new ArrayList<>();
    ArrayList<CinemaInfo> mListCinemas = new ArrayList<>();
    String mCityName = "";
    List<String> MovieNames = new ArrayList<>();

    @BindView(R.id.spinner_cities)
    Spinner mSpinnerCities;
    @BindView(R.id.spinner_movies)
    Spinner mSpinnerMovies;
    @BindView(R.id.spinner_cinemas)
    Spinner mSpinnerCinemas;

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
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(FdfsMainActivity.this)
                    .content("Loading Movies & Cinemas...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false).show();
            dialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                mListMovies.addAll(FdfsDataProvider.getFullMoviesList(mCityName));
                mListCinemas.addAll(FdfsDataProvider.getCinemasList(mCityName));
                Collections.sort(mListMovies, new Comparator<MovieInfo>() {
                    @Override
                    public int compare(MovieInfo o1, MovieInfo o2) {
                        return o1.Name.compareTo(o2.Name);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            MoviesSpinnerAdapter adapter = new MoviesSpinnerAdapter(getApplicationContext(), R.layout.layout_spinner_item, mListMovies);
            mSpinnerMovies.setAdapter(adapter);

            CinemasAdapter CineAdapter = new CinemasAdapter(getApplicationContext(), R.layout.layout_spinner_item, mListCinemas);
            mSpinnerCinemas.setAdapter(CineAdapter);

            dialog.dismiss();
        }

    }
}
