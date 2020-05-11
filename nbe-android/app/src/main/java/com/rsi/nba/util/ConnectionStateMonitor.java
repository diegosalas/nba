package com.rsi.nba.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest mNetworkRequest;
    private ConnectivityManager mConnectivityManager;
    private static final String TAG = "LOG_TAG";
    public static final int NO_NETWORK_TYPE = -1;
    public static final int WIFI = 1;
    public static final int CELLULAR = 2;
    public static final int ETHERNET = 3;

    interface ConnectionStateListener {
        void onAvailable(boolean isAvailable);
    }

    private ConnectionStateListener mConnectionStateListener;

    public ConnectionStateMonitor(Context context) {
        mNetworkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    void setOnConnectionStateListener(ConnectionStateListener connectionStateListener) {
        mConnectionStateListener = connectionStateListener;
    }

    public void enable() {
        assert mConnectivityManager != null;
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this);
    }

    void disable() {
        mConnectivityManager.unregisterNetworkCallback(this);
    }


    @Override
    public void onAvailable(@NonNull Network network) {
        if (mConnectionStateListener != null)
            mConnectionStateListener.onAvailable(true);
    }

    @Override
    public void onUnavailable() {
    }

    @Override
    public void onLost(@NonNull Network network) {
        if (mConnectionStateListener != null)
            mConnectionStateListener.onAvailable(false);
    }


    public int getNetworkType() {

        if (!isOnline())
            return NO_NETWORK_TYPE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network activeNetwork = mConnectivityManager.getActiveNetwork();
            if (activeNetwork == null) return NO_NETWORK_TYPE;

            NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities == null) return NO_NETWORK_TYPE;

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return WIFI;
            else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return CELLULAR;
            else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                return ETHERNET;

        } else {
            Network[] allNetworks = mConnectivityManager.getAllNetworks();
            for (Network network : allNetworks) {

                NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities == null) return NO_NETWORK_TYPE;

                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    return WIFI;
                else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    return CELLULAR;
                else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    return ETHERNET;
            }

        }
        return NO_NETWORK_TYPE;
    }


    boolean isOnline() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Network activeNetwork = mConnectivityManager.getActiveNetwork();

            if (activeNetwork == null) {
                Log.d(TAG, "isOnline: " + "false");

            } else {
                NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities == null) return false;
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
            }

        } else {
            Network[] allNetworks = mConnectivityManager.getAllNetworks();
            for (Network network : allNetworks) {

                NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(network);

                if (networkCapabilities != null
                        && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)))
                    return true;
            }

        }
        return false;
    }

}