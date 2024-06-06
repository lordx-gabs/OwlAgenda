package com.example.owlagenda.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

public class VerificationWifi {

    public static boolean hasInternet(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
            return capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            Network activeNetwork = connMgr.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }
            android.net.NetworkInfo networkInfo = connMgr.getNetworkInfo(activeNetwork);
            return networkInfo != null && networkInfo.isConnected();
        }
    }

}
