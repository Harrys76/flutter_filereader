package com.webview.filereader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;


/**
 * Created by hanbin on 2017/9/12.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

    public NetChangeListener listener;

    NetBroadcastReceiver(NetChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // If they are equal, the network status has changed
        Log.i("NetBroadcastReceiver", "NetBroadcastReceiver changed");
        if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            // When the network changes，Determine the current network status,
            // And call back the current network status through NetEvent
            if (listener != null) {
                listener.onChangeListener(netWorkState);
            }
        }
    }

    // Custom interface
    public interface NetChangeListener {
        void onChangeListener(int status);
    }

}