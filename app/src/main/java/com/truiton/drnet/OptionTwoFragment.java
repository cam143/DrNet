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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionTwoFragment extends Fragment {

    public static String downloadSpeed = null; // Download Speed

    private View rootView; // RootView
    private String wifiInfo[] = null;
    private WifiManager wifiManager; // Wifi Manager

    public static OptionTwoFragment newInstance() {
        OptionTwoFragment fragment = new OptionTwoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_option_two, container, false);

        getWifiInfo(); // Get Wifi Info
        displayWifiInfo(); // Display Wifi Info

        return rootView;
    }

    // Get Wifi Information
    public void getWifiInfo() {

        ConnectivityManager cm = (ConnectivityManager) rootView.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null) {
            wifiInfo = new String[1];
            wifiInfo[0] = OptionOneFragment.WIFI_ERROR_MESSAGE;
            return;
        }

        if (networkInfo.isConnected()) {
            wifiManager = (WifiManager) rootView.getContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

            if (connectionInfo != null || !connectionInfo.getSSID().toString().isEmpty()) {
                wifiInfo = new String[5];
                wifiInfo[0] = "SSID: " + connectionInfo.getSSID();
                wifiInfo[1] = "IP Address: " + String.valueOf(connectionInfo.getIpAddress());
                wifiInfo[2] = "MAC Address: " +  String.valueOf(connectionInfo.getMacAddress());
                wifiInfo[3] = "Link Speed: " + String.valueOf(connectionInfo.getLinkSpeed()) + " Mbps";
                wifiInfo[4] = "Download Speed: " + OptionTwoFragment.downloadSpeed + " Mbps";
            }

            // If No Troubleshooting
            if(OptionTwoFragment.downloadSpeed == null){
                wifiInfo = new String[1];
                wifiInfo[0] = "Troubleshoot first...";
            }

            // If WIFI_ERROR
            if(OptionTwoFragment.downloadSpeed != null && OptionTwoFragment.downloadSpeed.equals(OptionOneFragment.WIFI_ERROR_MESSAGE)){
                wifiInfo = new String[1];
                wifiInfo[0] = OptionOneFragment.WIFI_ERROR_MESSAGE;
            }
        }
    }

    // Display WiFi Info
    private void displayWifiInfo(){
        // Get String Array
        List<String> fruits_list = new ArrayList<>(Arrays.asList(wifiInfo));

        // Set Array Adapter
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(rootView.getContext(), android.R.layout.simple_list_item_1, fruits_list);

        // Set ListView
        final ListView listView = (ListView) rootView.findViewById(R.id.wifiInfo_list);

        // Add Array Adapter
        listView.setAdapter(arrayAdapter);
    }
}
