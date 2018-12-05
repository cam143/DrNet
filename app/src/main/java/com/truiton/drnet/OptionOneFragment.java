/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.truiton.drnet;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.github.anastr.speedviewlib.AwesomeSpeedometer;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class OptionOneFragment extends Fragment {

    private View rootView; // RootView

    private AwesomeSpeedometer speedDownload; // Download Speed

    private static final int WIFI_ERROR = 0;
    private static final int WIFI_PROGRESS = 1;
    private static final int WIFI_COMPLETED = 2;

    public static final String WIFI_ERROR_MESSAGE = "No internet connection...";
    private static final String WIFI_LOADING_MESSAGE = "loading";
    private static final String WIFI_PROGRESS_MESSAGE = "progress";
    private static final String WIFI_COMPLETED_MESSAGE = "completed";

    private final int LOADING_END = 100;
    private final int LOADING_START = 0;

    private int progressbar_loading;
    private NumberProgressBar progressBar;

    private Bundle bundle;
    private Message message;

    public static OptionOneFragment newInstance() {
        OptionOneFragment fragment = new OptionOneFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_option_one, container, false);
        progressBar = (NumberProgressBar) rootView.findViewById(R.id.progressBarLoading);

        final Button btnTroubleShoot = (Button) rootView.findViewById(R.id.buttonTroubleshoot);
        speedDownload = (AwesomeSpeedometer) rootView.findViewById(R.id.speedDownload);

        btnTroubleShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnTroubleShoot.setEnabled(false);
                btnTroubleShoot.setTextColor(Color.BLACK);
                progressbar_loading = LOADING_START;
                progressBar.setProgress(progressbar_loading);
                speedDownload.speedTo(LOADING_START);

                new SpeedTestTask().execute(); // Download SpeedTest
            }
        });

        return rootView;
    }

    // Get Speed
    public class SpeedTestTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();

            // add a listener to wait for speedTest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is finished
                    Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());

                     /*
                        Formula:
                        Megabits = (bits) / (1024 * 1024)
                        Megabyte = Megabits * 8
                     */


                    BigDecimal bits = report.getTransferRateBit();
                    BigDecimal megabyte = new BigDecimal(1024 * 1024);
                    BigDecimal conversion = bits.divide(megabyte, 2, RoundingMode.CEILING);
                    conversion = conversion.multiply(new BigDecimal(8));

                    message = handler.obtainMessage(WIFI_COMPLETED); // Set message on handler
                    bundle = new Bundle(); // String to pass
                    bundle.putString(WIFI_COMPLETED_MESSAGE,conversion.toString()); // Add message
                    bundle.putString(WIFI_LOADING_MESSAGE,Double.valueOf(LOADING_END).toString()); // Add message

                    message.setData(bundle); // Set Message
                    handler.sendMessage(message); // Send Message
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                    Log.v("speedtest", "[ERROR] : " + errorMessage);

                    message = handler.obtainMessage(WIFI_ERROR); // Set message on handler
                    bundle = new Bundle(); // String to pass
                    bundle.putString(WIFI_ERROR_MESSAGE,WIFI_ERROR_MESSAGE); // Add message
                    message.setData(bundle); // Set Message
                    handler.sendMessage(message); // Send Message
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    Log.v("speedtest", "[PROGRESS] progress : " + percent + "%");
                    Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());

                    BigDecimal bits = report.getTransferRateBit();
                    BigDecimal megabyte = new BigDecimal(1024 * 1024);
                    BigDecimal conversion = bits.divide(megabyte, 2, RoundingMode.CEILING);
                    conversion = conversion.multiply(new BigDecimal(8));

                     /*
                        Formula:
                        Megabits = (bits) / (1024 * 1024)
                        Megabyte = Megabits * 8
                     */

                    Log.v("speedtest", "[RESULT] rate in megabyte   : " + conversion);

                    message = handler.obtainMessage(WIFI_PROGRESS); // Set message on handler
                    bundle = new Bundle(); // String to pass
                    bundle.putString(WIFI_PROGRESS_MESSAGE,conversion.toString()); // Add message
                    bundle.putString(WIFI_LOADING_MESSAGE,Double.valueOf(percent).toString()); // Add message

                    message.setData(bundle); // Set Message
                    handler.sendMessage(message); // Send Message

                    bundle = new Bundle();
                }
            });

            speedTestSocket.startDownload("ftp://speedtest:speedtest@ftp.otenet.gr/test1Mb.db");

            return null;
        }
    }

    // Handler
    private final Handler handler = new Handler() {

        float speed_download; // Download Speed

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WIFI_ERROR:
                        OptionTwoFragment.downloadSpeed = WIFI_ERROR_MESSAGE;
                        changeNavigationTwoView();
                    break;

                case WIFI_PROGRESS:
                        progressbar_loading = (int) Double.parseDouble(msg.getData().getString(WIFI_LOADING_MESSAGE));
                        speed_download = Float.parseFloat(msg.getData().getString(WIFI_PROGRESS_MESSAGE));
                        progressBar.setProgress(progressbar_loading);
                        speedDownload.speedTo(speed_download);
                    break;

                case WIFI_COMPLETED:
                        progressbar_loading = (int) Double.parseDouble(msg.getData().getString(WIFI_LOADING_MESSAGE));
                        speed_download = Float.parseFloat(msg.getData().getString(WIFI_COMPLETED_MESSAGE));
                        progressBar.setProgress(progressbar_loading);
                        speedDownload.speedTo(speed_download);
                        OptionTwoFragment.downloadSpeed = Float.toString(speed_download);

                        // Delay 5 seconds
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeNavigationTwoView();
                            }
                        }, 5000);
            }
        }
    };

    // Change Navigation Two View
    private void changeNavigationTwoView(){
        ItemOneFragment.bottomNavigationView.getMenu().getItem(1).setChecked(true);
        FragmentActivity activity = (FragmentActivity) rootView.getContext();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.option_layout, OptionTwoFragment.newInstance());
        transaction.commit();
    }
}
