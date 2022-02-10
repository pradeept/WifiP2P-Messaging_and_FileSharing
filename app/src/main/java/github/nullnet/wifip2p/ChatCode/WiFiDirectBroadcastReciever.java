package github.nullnet.wifip2p.ChatCode;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import github.nullnet.wifip2p.MainActivity;

public class WiFiDirectBroadcastReciever extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private Listandchat mActivity = new Listandchat();


    public WiFiDirectBroadcastReciever(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, Listandchat mActivity) {

        this.mManager = mManager;
        this.mActivity = mActivity;
        this.mChannel = mChannel;
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);


            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi is Off", Toast.LENGTH_SHORT).show();

            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {

            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mManager.requestPeers(mChannel, mActivity.peerListListener);
            }

        }
        else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            if(mManager== null){
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(networkInfo.isConnected()){
                mManager.requestConnectionInfo(mChannel,mActivity.connectionInfoListener);
            }
            else {
                    Toast.makeText(mActivity,"Error While Connecting!",Toast.LENGTH_LONG).show();
//                Toast.makeText(mActivity,"Connection Interrupted!!",Toast.LENGTH_SHORT).show();
//                Toast.makeText(mActivity,"Please Check the connection!!",Toast.LENGTH_LONG).show();
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }


    }
}
