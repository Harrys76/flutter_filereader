package com.webview.filereader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by hanbin on 2017/9/12.
 */

public class NetUtil {
    /**
     * No internet
     */
    public static final int NETWORK_NONE = -1;
    /**
     * mobile network
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * wireless network
     */
    public static final int NETWORK_WIFI = 1;

    public static int getNetWorkState(Context context) {
        //Get the connection manager object
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
            //If network connection，Determine the network type
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                    return NETWORK_WIFI;//wifi
                } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                    return NETWORK_MOBILE;//mobile
                }
            } else {
                //network anomaly
                return NETWORK_NONE;
            }
        }
        return NETWORK_NONE;
    }
}