package github.nullnet.wifip2p.ChatCode;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import github.nullnet.wifip2p.R;
import pl.droidsonroids.gif.GifImageButton;

public class ScanFragment extends Fragment {

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReciever mReceiver;
    IntentFilter mIntentFilter;

    //GIF Related referemce
    GifImageButton gifImageButton;
    ImageView wifisearchpng;
    TextView searchinfo;
    TextView peersstatus;


//    Listandchat listandchat = new Listandchat();


    //New View
    LinearLayout send,receive;

    public ScanFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_scan, container, false);




        //SCANGIF
        wifisearchpng = view.findViewById(R.id.wifiserchingpng);
        gifImageButton = view.findViewById(R.id.wifiScanGif);
        gifImageButton.setVisibility(View.INVISIBLE);
        searchinfo = view.findViewById(R.id.searchinfo);

        //peers availability checker
        peersstatus = view.findViewById(R.id.peersstatus);

        wifisearchpng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifisearchpng.setVisibility(View.INVISIBLE);
                searchinfo.setEnabled(false);
                searchinfo.setVisibility(View.INVISIBLE);
                gifImageButton.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), Listandchat.class);
                startActivity(intent);

            }
        });


        //New View
        send = view.findViewById(R.id.send);
        receive = view.findViewById(R.id.receive);

        return view;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

}