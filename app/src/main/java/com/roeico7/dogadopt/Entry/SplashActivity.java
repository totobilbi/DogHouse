package com.roeico7.dogadopt.Entry;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.jorgecastillo.FillableLoader;
import com.github.jorgecastillo.State;
import com.github.jorgecastillo.listener.OnStateChangeListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roeico7.dogadopt.R;
import com.roeico7.dogadopt.Translate.Translator;


public class SplashActivity extends AppCompatActivity implements OnStateChangeListener {
    private FillableLoader fillableLoader;
    private Boolean isDownloaded = false;
    private Boolean downloadStarted = false;
    private TextView tv_title, tv_text;
    private Translator translator;

    private String svgPath = "M 76.00,25.00\n" +
            "           C 69.02,30.28 57.70,42.30 51.00,49.00\n" +
            "             46.37,53.63 40.83,58.02 38.00,64.00\n" +
            "             38.00,64.00 47.00,64.00 47.00,64.00\n" +
            "             47.00,64.00 47.00,99.00 47.00,99.00\n" +
            "             47.00,99.00 107.00,99.00 107.00,99.00\n" +
            "             107.00,99.00 106.00,76.00 106.00,76.00\n" +
            "             106.00,76.00 107.60,67.60 107.60,67.60\n" +
            "             107.60,67.60 116.00,66.00 116.00,66.00\n" +
            "             116.00,66.00 100.00,49.00 100.00,49.00\n" +
            "             100.00,49.00 76.00,25.00 76.00,25.00 Z\n" +
            "           M 75.00,123.00\n" +
            "           C 71.00,125.34 70.07,125.76 68.00,130.00\n" +
            "             68.00,130.00 66.37,124.02 66.37,124.02\n" +
            "             66.02,123.07 65.63,122.11 64.99,121.31\n" +
            "             58.17,112.81 52.93,131.06 63.02,131.39\n" +
            "             64.74,131.45 65.61,130.69 67.00,130.00\n" +
            "             66.36,132.65 65.08,138.98 67.00,141.36\n" +
            "             69.15,143.82 73.73,141.56 77.00,141.36\n" +
            "             77.00,141.36 84.89,142.50 84.89,142.50\n" +
            "             90.32,141.71 88.64,134.61 88.00,131.09\n" +
            "             89.73,131.32 91.11,131.78 92.89,131.09\n" +
            "             102.49,127.90 92.86,111.61 87.89,124.02\n" +
            "             87.24,125.63 87.16,127.30 87.00,129.00\n" +
            "             87.00,129.00 80.00,124.00 80.00,124.00\n" +
            "             82.31,122.82 84.43,121.98 85.99,119.78\n" +
            "             89.76,114.46 86.24,108.70 81.21,110.76\n" +
            "             78.83,111.72 78.03,113.88 77.00,116.00\n" +
            "             75.69,113.98 74.37,111.45 71.91,110.66\n" +
            "             68.39,109.52 65.17,112.79 67.16,117.96\n" +
            "             68.71,121.99 71.12,122.55 75.00,123.00 Z\n" +
            "           M 125.00,127.00\n" +
            "           C 118.43,127.09 117.24,127.11 117.00,134.00\n" +
            "             107.67,132.44 109.51,122.29 112.51,119.88\n" +
            "             115.37,117.33 120.58,119.47 124.00,119.88\n" +
            "             124.00,119.88 125.00,111.02 125.00,111.02\n" +
            "             119.87,110.76 117.25,109.74 112.00,111.02\n" +
            "             97.32,114.68 98.54,134.12 106.04,139.19\n" +
            "             110.02,141.75 121.20,142.76 124.01,139.19\n" +
            "             125.17,137.81 124.99,134.79 125.00,133.00\n" +
            "             125.00,133.00 125.00,127.00 125.00,127.00 Z\n" +
            "           M 27.00,111.00\n" +
            "           C 27.00,111.00 27.00,141.00 27.00,141.00\n" +
            "             33.51,141.00 41.22,142.66 46.96,138.99\n" +
            "             54.53,134.16 55.57,118.45 47.81,113.36\n" +
            "             42.70,110.01 33.10,111.00 27.00,111.00 Z\n" +
            "           M 51.00,150.53\n" +
            "           C 47.25,151.65 44.12,152.80 41.70,156.10\n" +
            "             39.53,159.06 39.10,162.45 39.01,166.00\n" +
            "             38.79,175.37 41.43,182.16 52.00,182.90\n" +
            "             74.96,184.51 75.01,146.83 51.00,150.53 Z\n" +
            "           M 114.00,175.00\n" +
            "           C 114.00,175.00 101.80,172.00 101.80,172.00\n" +
            "             101.33,174.36 100.48,177.32 101.80,179.70\n" +
            "             102.83,182.32 106.44,182.75 109.00,182.91\n" +
            "             121.86,183.72 126.66,173.93 121.85,167.21\n" +
            "             120.04,164.67 114.05,161.48 111.00,159.00\n" +
            "             111.00,159.00 121.00,161.00 121.00,161.00\n" +
            "             121.76,158.92 123.16,155.51 122.31,153.31\n" +
            "             121.30,150.69 116.43,150.15 114.00,150.18\n" +
            "             106.53,150.27 98.27,157.03 102.31,164.96\n" +
            "             104.86,169.95 109.16,169.51 114.00,175.00 Z\n" +
            "           M 7.00,151.00\n" +
            "           C 7.00,151.00 7.00,182.00 7.00,182.00\n" +
            "             7.00,182.00 17.00,182.00 17.00,182.00\n" +
            "             17.00,182.00 17.00,171.00 17.00,171.00\n" +
            "             17.00,171.00 26.00,171.00 26.00,171.00\n" +
            "             26.00,171.00 26.00,182.00 26.00,182.00\n" +
            "             26.00,182.00 35.00,182.00 35.00,182.00\n" +
            "             35.00,182.00 35.00,151.00 35.00,151.00\n" +
            "             24.33,151.01 25.00,151.18 25.00,163.00\n" +
            "             25.00,163.00 17.00,163.00 17.00,163.00\n" +
            "             17.00,163.00 17.00,151.00 17.00,151.00\n" +
            "             17.00,151.00 7.00,151.00 7.00,151.00 Z\n" +
            "           M 71.00,151.00\n" +
            "           C 71.00,156.40 70.20,174.01 72.31,177.89\n" +
            "             76.17,184.98 91.77,184.62 95.86,177.89\n" +
            "             98.94,172.83 98.00,157.43 98.00,151.00\n" +
            "             98.00,151.00 87.97,151.00 87.97,151.00\n" +
            "             87.97,151.00 87.97,168.98 87.97,168.98\n" +
            "             87.75,171.17 86.98,174.33 84.07,173.97\n" +
            "             81.76,173.69 80.71,170.99 80.43,169.00\n" +
            "             80.43,169.00 81.00,151.00 81.00,151.00\n" +
            "             81.00,151.00 71.00,151.00 71.00,151.00 Z\n" +
            "           M 128.00,151.00\n" +
            "           C 128.00,151.00 128.00,182.00 128.00,182.00\n" +
            "             128.00,182.00 150.00,182.00 150.00,182.00\n" +
            "             150.00,182.00 150.00,174.00 150.00,174.00\n" +
            "             150.00,174.00 138.00,174.00 138.00,174.00\n" +
            "             138.00,174.00 138.00,170.00 138.00,170.00\n" +
            "             138.00,170.00 147.00,171.00 147.00,171.00\n" +
            "             147.00,171.00 147.00,162.00 147.00,162.00\n" +
            "             147.00,162.00 138.00,163.00 138.00,163.00\n" +
            "             138.00,163.00 138.00,159.00 138.00,159.00\n" +
            "             138.00,159.00 150.00,159.00 150.00,159.00\n" +
            "             150.00,159.00 151.00,152.00 151.00,152.00\n" +
            "             151.00,152.00 128.00,151.00 128.00,151.00 Z\n" +
            "           M 62.00,194.00\n" +
            "           C 62.00,196.23 61.53,201.54 63.60,202.88\n" +
            "             65.42,204.05 69.05,202.42 71.00,201.92\n" +
            "             74.88,200.93 80.04,200.68 84.00,201.32\n" +
            "             86.23,201.68 90.52,203.32 92.53,202.39\n" +
            "             95.22,201.14 94.75,193.84 90.85,193.78\n" +
            "             88.47,193.75 86.98,195.90 79.00,196.00\n" +
            "             67.37,196.13 70.56,194.47 62.00,194.00 Z";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        tv_title = findViewById(R.id.tv_title);
        tv_text = findViewById(R.id.tv_text);
        fillableLoader = findViewById(R.id.fillableLoader);
        startAnimation();

        translator = new Translator(new DownloadListener());
        translator.checkLanguageDownloaded();
    }


    private void startAnimation() {
        fillableLoader.setSvgPath(svgPath);
        fillableLoader.start();
        fillableLoader.setOnStateChangeListener(this);
    }


    private void resetAnimation() {
        fillableLoader.reset();
    }


    @Override
    public void onStateChange(int state) {
        switch (state) {
            case State.FILL_STARTED:
                if (downloadStarted) {
                    SplashActivity.this.tv_title.setText(getString(R.string.updating));
                    SplashActivity.this.tv_text.setText(getString(R.string.please_wait));
                    downloadStarted = false;
                } else if (isDownloaded) {
                    SplashActivity.this.tv_title.setText(getString(R.string.ui_update_complete));
                    SplashActivity.this.tv_text.setText(getString(R.string.logging_in));
                }
                break;

            case State.FINISHED:
                if (isDownloaded) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    resetAnimation();
                    startAnimation();
                }
        }
    }


    private class DownloadListener implements Translator.DownloadListener {
        @Override
        public void downloadFinished() {
            currentModel(true);
        }


        @Override
        public void downloadStart() {
            downloadStarted = true;
        }

        @Override
        public void currentModel(Boolean isDownloaded) {
            if (isDownloaded) {
                SplashActivity.this.isDownloaded = true;
            } else {
                downloadStart();
                translator.downloadLanguage();
            }
        }


    }
}
