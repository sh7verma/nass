package app.com.esenatenigeria.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Connection_Detector extends BroadcastReceiver {

    private Context _context;

    public Connection_Detector(Context context) {
        this._context = context;
    }

    /**
     * Checking for all possible internet providers
     **/
    public boolean isConnectingToInternet() {
        if (_context != null) {

            ConnectivityManager connectivity = (ConnectivityManager) _context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();

                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }

            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        _context = arg0;
        boolean check = isConnectingToInternet();
        Log.d("check", "is " + check);
    }
}
