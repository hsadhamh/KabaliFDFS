package factor.app.fdfs.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.rey.material.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import factor.app.fdfs.events.OpenBookMyShowEvent;
import factor.app.fdfs.providers.FdfsDataProvider;
import factor.app.fdfs.R;
import factor.app.fdfs.events.UpdateUiEvent;
import factor.app.fdfs.adapters.ListTheatresAdapter;
import factor.app.fdfs.models.MovieInTheatresInfo;
import factor.app.fdfs.providers.Typefaces;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class BackgroundCheckActivity extends AppCompatActivity{

    @BindView(R.id.list_theatres)
    RecyclerView mListView;

    @BindView(R.id.id_view_movie_name)
    TextView mTxtMovieName;

    @BindView(R.id.id_fab_stop)
    FloatingActionButton mFab;
    @BindView(R.id.adViewCheckPage)
    AdView adView;

    MovieInTheatresInfo mMovies = null;

    private InterstitialAd interstitial;
    AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_background_check);
        ButterKnife.bind(this);
        updateUI();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

       adRequest = new AdRequest
                .Builder()
                //.addTestDevice("F3D0EE493657AD2952233060D190BFBF")
                .build();
        adView.loadAd(adRequest);

        // Prepare the Interstitial Ad
        interstitial = new InterstitialAd(BackgroundCheckActivity.this);
        interstitial.setAdUnitId("ca-app-pub-7462033170287511/2315398584");
        interstitial.setAdListener(new AdListener() {
            public void onAdLoaded() {
                super.onAdLoaded();
                if (interstitial.isLoaded()) {
                    interstitial.show();
                }
            }
        });
    }

    @OnClick(R.id.id_fab_stop)
    public void onStopClick(){
        FdfsDataProvider.cancelAlarm(getApplicationContext());
        if(FdfsDataProvider.deleteFile(getApplicationContext())) {
            Intent i = new Intent();
            i.setClass(BackgroundCheckActivity.this, FdfsMainActivity.class);
            finish();
            startActivity(i);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UpdateUiEvent e){
        if(!FdfsDataProvider.doesFileExists(getApplicationContext())){
            return;
        }
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OpenBookMyShowEvent e){
        new MaterialDialog
                .Builder(BackgroundCheckActivity.this)
                .cancelable(true)
                .title("Open BookMyShow App")
                .positiveText("Open App")
                .content("Continue booking in BookMyShow App. Be first to get tickets.")
                .negativeText("Open Site")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // check package. if not found, open play-store.
                        String packageName = "com.bt.bms";
                        if (FdfsDataProvider.appInstalledOrNot(getApplicationContext(), packageName)) {
                            Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(i);
                        } else {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                            } catch (android.content.ActivityNotFoundException anf) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
                            }
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //  open browser.
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FdfsDataProvider.mStrBookMyShow));
                        startActivity(browserIntent);
                    }
                })
                .show();
    }

    public void updateUI(){
        try {
            String sJson = FdfsDataProvider.readFromFile(getApplicationContext());
            if((sJson == null || sJson.isEmpty())) return;

            mMovies = FdfsDataProvider.getSavedInfoObject(sJson);

            if(mMovies != null) {
                mTxtMovieName.setText(mMovies.getMovie().getName() + " ["+ mMovies.getCity() +"]");
                mTxtMovieName.setTypeface(Typefaces.getRobotoMedium(getApplicationContext()));

                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                mListView.setLayoutManager(linearLayoutManager);

                ListTheatresAdapter linear = new ListTheatresAdapter(mMovies);
                mListView.setAdapter(linear);
            }
        } catch (JSONException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void finish(){
        // Load ads into Interstitial Ads
        interstitial.loadAd(adRequest);
        super.finish();
    }
}
