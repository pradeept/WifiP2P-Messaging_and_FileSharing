package github.nullnet.wifip2p.ChatCode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import github.nullnet.wifip2p.MainActivity;
import github.nullnet.wifip2p.R;


public class Listandchat extends AppCompatActivity {

    //onpeerlistlistener requirements
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    public WifiP2pDevice device;

    WiFiDirectBroadcastReciever mReceiver;

    //local xml references
    ListView listView;
    TextView peersstatus;
    TextView recdmsg;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    ProgressDialog peerconnectprogress;
    EditText messageInput;
    Button sendmsg;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    //IntentFilter
    IntentFilter mIntentFilter;
    WifiP2pConfig config = new WifiP2pConfig();
    private final int MESSAGE_READ = 1;

    //Chat related
    final List<ChatAppMsgDTO> msgDtoList = new ArrayList<ChatAppMsgDTO>();
    public View view;
    final ChatAppMsgAdapter chatAppMsgAdapter = new ChatAppMsgAdapter(msgDtoList,Listandchat.this);
    RecyclerView msgRecyclerView = null;
    EditText msgInputText;
    ImageView msgSendButton;
    ImageButton attachButton;
    Intent fileSelector;
    Handler fileselectorhandler = new Handler();


    WifiP2pDevice pDevice;

    //for checking whether connection is present or not
    Boolean ConnectionExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listandchat);

        //for NetworkonMain thread exception
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView = findViewById(R.id.peerListView);
        peersstatus = findViewById(R.id.peersstatus);
        recdmsg = findViewById(R.id.recdmsg);
        peerconnectprogress = new ProgressDialog(Listandchat.this);
        sendmsg = findViewById(R.id.msgSend);
        messageInput = findViewById(R.id.msginput);


        //WIFIP2P related

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(Listandchat.this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReciever(mManager, mChannel, Listandchat.this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(mReceiver,mIntentFilter);

        if (!wifiManager.isWifiEnabled()) {
            peersstatus.setText("Wifi not enabled");
        } else {
            peersstatus.setText("Wifi enabled");
        }
        lisviewlistener();


        //First mesgsend and msginput will not be visible
        sendmsg.setVisibility(View.INVISIBLE);
        messageInput.setVisibility(View.INVISIBLE);

        LayoutInflater inflater = (LayoutInflater) Listandchat.this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.activity_chat_app, null);

        //Chat view (inflater) related
        msgRecyclerView = view.findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerView.setLayoutManager(linearLayoutManager);
        msgRecyclerView.setAdapter(chatAppMsgAdapter);
        msgInputText = view.findViewById(R.id.chat_input_msg);
        msgSendButton = view.findViewById(R.id.chat_send_msg);
        attachButton = view.findViewById(R.id.attach);

        discover();

    }


    public void discover() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(Listandchat.this, "Discovery Started!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(Listandchat.this, "Discovery Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //    Handler to handle messages
    Handler handler = new Handler(new Handler.Callback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:

//                    byte[] readBuff = (byte[]) msg.obj;
                    byte [] readBuff = (byte[]) msg.obj;
//                    Log.i("Type",msg.getClass().getName());
//                    String tempMsg = new String(readBuff,0,msg.arg1);

                    byte[] decodeMsg = Base64.getMimeDecoder().decode(readBuff);
                    String tempMsg = new String(decodeMsg);
                    String actualMessage;
//                    if(tempMsg.length() >40){
//
//                        final File f = new File(Environment.getExternalStorageDirectory() + "/"
//                                + Listandchat.this.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
//                                + ".jpg");
//
//                        File dirs = new File(f.getParent());
//                        if (!dirs.exists())
//                            dirs.mkdirs();
//                        try {
//                            f.createNewFile();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//                            FileOutputStream fos = new FileOutputStream(f);
//                            fos.write(readBuff);
//                            fos.close();
//                            Toast.makeText(Listandchat.this,"File saved!",Toast.LENGTH_LONG).show();
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }

                    actualMessage = tempMsg;
                    ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_RECEIVED, actualMessage,device);
                    msgDtoList.add(msgDto);
                    int newMsgPosition = msgDtoList.size() - 1;
                    // Notify recycler view insert one new data.
                    chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                    // Scroll RecyclerView to the last message.
                    msgRecyclerView.scrollToPosition(newMsgPosition);

                    break;

            }
            return true;
        }
    });



    //Searching for peers
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            peersstatus.setText("Available Peers!");
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];     //Size of deviceNameArray initialized
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];   //Size of deviceArray initialized
                int index = 0;

                //assigning device name and device to 'deviceNameArray' and 'deviceArray'
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
            }
            if (peers.size() == 0) {
                peersstatus.setText("No Device Found!");
                return;
            }

        }
    };

    //Connecting to a peer
    private void lisviewlistener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                final WifiP2pDevice device = deviceArray[position];
                device = deviceArray[position];

//                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                if (ActivityCompat.checkSelfPermission(Listandchat.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                        Toast.makeText(Listandchat.this, "Connected to " + device.deviceName + "!", Toast.LENGTH_SHORT).show();
//                        LayoutInflater inflater = LayoutInflater.from(getActivity());
//                        View viewMyLayout = inflater.inflate(R.layout.onpeerconnect, null,false);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(Listandchat.this, "Could not able to Connect " + device + "!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    //Broadcastreceiver ConnectionInfo Listener
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {

                Toast.makeText(Listandchat.this, "You are the HOST!", Toast.LENGTH_SHORT).show();

                Listandchat.this.setContentView(view);


                serverClass = new ServerClass();
                serverClass.start();

                attachButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        fileSelector = new Intent(Intent.ACTION_GET_CONTENT);
//                        fileSelector.setType("*/*");
//                        startActivityForResult(fileSelector, 10);
                      //  fileselectorhandler.post(fileselectorRunnable);

                        Toast.makeText(Listandchat.this, "To Send Images , Go Back To Images Chat Section", Toast.LENGTH_SHORT).show();

                    }

                });

                msgSendButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        String msgContent = msgInputText.getText().toString();

                        if(!TextUtils.isEmpty(msgContent))
                        {
                            sendReceive.write(msgContent.getBytes());
                            // Add a new sent message to the list.
                            ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, msgContent,device);
                            msgDtoList.add(msgDto);
                            int newMsgPosition = msgDtoList.size() - 1;
                            // Notify recycler view insert one new data.
                            chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                            // Scroll RecyclerView to the last message.
                            msgRecyclerView.scrollToPosition(newMsgPosition);
                            // Empty the input edit text box.
                            msgInputText.setText("");

                        }
                    }
                });


            }

            else if (wifiP2pInfo.groupFormed) {
                Toast.makeText(Listandchat.this, "You are the CLIENT!", Toast.LENGTH_SHORT).show();
                Listandchat.this.setContentView(view);

                clientClass = new ClientClass(groupOwnerAdress);
                clientClass.start();

                attachButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                   //  fileselectorhandler.post(fileselectorRunnable);
                        Toast.makeText(Listandchat.this, "To Send Images , Go Back To Images Chat Section", Toast.LENGTH_SHORT).show();

                    }
                });

                msgSendButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View view) {
                        String msgContent = msgInputText.getText().toString();

                        if(!TextUtils.isEmpty(msgContent) )
                        {
                            sendReceive.write(msgContent.getBytes());
                            // Add a new sent message to the list.
                            ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, msgContent,device);
                            msgDtoList.add(msgDto);
                            int newMsgPosition = msgDtoList.size() - 1;
                            // Notify recycler view insert one new data.
                            chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                            // Scroll RecyclerView to the last message.
                            msgRecyclerView.scrollToPosition(newMsgPosition);
                            // Empty the input edit text box.
                            msgInputText.setText("");
                        }
                    }
                });
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ClientClass extends Thread {
        Socket socket;
        String hostAdress;
        public ClientClass(InetAddress groupOwnerAdress) {
            hostAdress = groupOwnerAdress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdress, 8888), 5000);
                sendReceive = new SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        public boolean exists(){
//            if (socket.isConnected()){
//                return true;
//            }
//            else {
//                return false;
//            }
//        }

    }

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class SendReceive extends Thread {
        private final Socket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        String EncodedMsg;


        public SendReceive(Socket skt, InputStream is, OutputStream os) {
            socket = skt;
            this.inputStream = is;
            this.outputStream = os;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[10000];
            int bytes;


            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
//                    MESSAGE_READ, bytes, -1, buffer
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void write(byte[] bytes) {
            try {

                EncodedMsg = Base64.getEncoder().encodeToString(bytes);
//                byte[] encodedbytes = Base64.getEncoder().encode(bytes);
//                outputStream.write(EncodedMsg.getBytes());
                if(socket.isOutputShutdown() || socket.isInputShutdown()) {
                    Intent intentmain = new Intent(Listandchat.this,MainActivity.class);
                    startActivity(intentmain);
                    Toast.makeText(Listandchat.this,"Connection Error",Toast.LENGTH_LONG).show();
                }
                else {

                    outputStream.write(EncodedMsg.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

