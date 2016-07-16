package factor.app.fdfs.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import factor.app.fdfs.R;
import factor.app.fdfs.providers.FdfsDataProvider;

/**
 * Created by hassanhussain on 7/7/2016.
 */
public class FactorSplashScreen extends AppCompatActivity {
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new BackgroundOps().execute();
    }

    /**
     * Async Task
     */
    private class BackgroundOps extends AsyncTask<Void, Void, Void> {
        boolean fileExits = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                fileExits = FdfsDataProvider.doesFileExists(getApplicationContext());
                Thread.sleep(SPLASH_TIME_OUT);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return  null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(fileExits) {
                Intent i = new Intent(FactorSplashScreen.this, BackgroundCheckActivity.class);
                startActivity(i);
            }
            else{
                Intent i = new Intent(FactorSplashScreen.this, FdfsMainActivity.class);
                startActivity(i);
            }
            finish();
        }

    }
}
