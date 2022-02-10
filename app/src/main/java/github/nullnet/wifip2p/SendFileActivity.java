package github.nullnet.wifip2p;

import static android.util.Base64.DEFAULT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import github.nullnet.wifip2p.ChatCode.ChatAppMsgAdapter;
import github.nullnet.wifip2p.ChatCode.ChatAppMsgDTO;
import github.nullnet.wifip2p.adapter.DeviceAdapter;
import github.nullnet.wifip2p.broadcast.DirectBroadcastReceiver;
import github.nullnet.wifip2p.callback.DirectActionListener;
import github.nullnet.wifip2p.common.Constants;
import github.nullnet.wifip2p.task.WifiClientTask;
import github.nullnet.wifip2p.util.WifiP2pUtils;
import github.nullnet.wifip2p.widget.LoadingDialog;


public class SendFileActivity extends BaseActivity {

    private static final String TAG = "SendFileActivity";

    private static final int CODE_CHOOSE_FILE = 100;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private WifiP2pInfo wifiP2pInfo;

    private boolean wifiP2pEnabled = false;

    private List<WifiP2pDevice> wifiP2pDeviceList;

    private DeviceAdapter deviceAdapter;

    private TextView tv_myDeviceName;

    private TextView tv_myDeviceAddress;

    private TextView tv_myDeviceStatus;

    private TextView tv_status;

    private Button btn_disconnect;
    ImageView iv_image;
    private Button btn_chooseFile;



    private LoadingDialog loadingDialog;

    private BroadcastReceiver broadcastReceiver;

    private WifiP2pDevice mWifiP2pDevice;

    //New
    SendReceive sendReceive;
    public WifiP2pDevice device = mWifiP2pDevice; //set Device
    final List<ChatAppMsgDTO> msgDtoList = new ArrayList<ChatAppMsgDTO>();
    public View view;
    final ChatAppMsgAdapter chatAppMsgAdapter = new ChatAppMsgAdapter(msgDtoList,SendFileActivity.this);
    RecyclerView msgRecyclerView = null;
    ImageView msgSendButton ;
    EditText msgInputText ;
    private final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    private ProgressDialog progressDialog;
    ImageButton attachButton;

    private final DirectActionListener directActionListener = new DirectActionListener() {

        @SuppressLint("MissingPermission")
        @Override
        public void wifiP2pEnabled(boolean enabled) {
            wifiP2pEnabled = enabled;

            //Start Searching;

            if (ActivityCompat.checkSelfPermission(SendFileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Please grant location permission! and try again");
                return;
            }
            if (!wifiP2pEnabled) {
                showToast("Need to turn on Wifi first");
                return;
            }
            loadingDialog.show("Searching for nearby devices", true, false);
            wifiP2pDeviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    showToast("Success");
                }
                @Override
                public void onFailure(int reasonCode) {
                    showToast("Failure");
                    loadingDialog.cancel();
                }
            });


        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            dismissLoadingDialog();
            wifiP2pDeviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            btn_disconnect.setEnabled(true);
            btn_chooseFile.setEnabled(true);
            Log.e(TAG, "onConnectionInfoAvailable");
            Log.e(TAG, "onConnectionInfoAvailable groupFormed: " + wifiP2pInfo.groupFormed);
            Log.e(TAG, "onConnectionInfoAvailable isGroupOwner: " + wifiP2pInfo.isGroupOwner);
            Log.e(TAG, "onConnectionInfoAvailable getHostAddress: " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
            StringBuilder stringBuilder = new StringBuilder();
            if (mWifiP2pDevice != null) {
                stringBuilder.append("Connected To：");
                stringBuilder.append(mWifiP2pDevice.deviceName);
                stringBuilder.append("\n");
                stringBuilder.append("Address：");
                stringBuilder.append(mWifiP2pDevice.deviceAddress);
            }
            stringBuilder.append("\n");
            stringBuilder.append("Owner：");
            stringBuilder.append(wifiP2pInfo.isGroupOwner ? "True" : "False");
            stringBuilder.append("\n");
            stringBuilder.append("Owner IP ：");
            stringBuilder.append(wifiP2pInfo.groupOwnerAddress.getHostAddress());
            tv_status.setText(stringBuilder);
//            if (wifiP2pInfo.groupFormed && !wifiP2pInfo.isGroupOwner) {
//                SendFileActivity.this.wifiP2pInfo = wifiP2pInfo;
//            }
            SendFileActivity.this.wifiP2pInfo = wifiP2pInfo;

            //Code For Chat
            boolean ImageSender = getIntent().getStringExtra("image").equals("true");

            if(!ImageSender){
                SetChat();
            }


        }

        @Override
        public void onDisconnection() {
            Log.e(TAG, "onDisconnection");
            btn_disconnect.setEnabled(false);
            btn_chooseFile.setEnabled(false);
            showToast("Not connected");
            wifiP2pDeviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            tv_status.setText(null);
            SendFileActivity.this.wifiP2pInfo = null;
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            Log.e(TAG, "onSelfDeviceAvailable");
            Log.e(TAG, "DeviceName: " + wifiP2pDevice.deviceName);
            Log.e(TAG, "DeviceAddress: " + wifiP2pDevice.deviceAddress);
            Log.e(TAG, "Status: " + wifiP2pDevice.status);
            tv_myDeviceName.setText(wifiP2pDevice.deviceName);
            tv_myDeviceAddress.setText(wifiP2pDevice.deviceAddress);
            tv_myDeviceStatus.setText(WifiP2pUtils.getDeviceStatus(wifiP2pDevice.status));
        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            Log.e(TAG, "onPeersAvailable :" + wifiP2pDeviceList.size());
            SendFileActivity.this.wifiP2pDeviceList.clear();
            SendFileActivity.this.wifiP2pDeviceList.addAll(wifiP2pDeviceList);
            deviceAdapter.notifyDataSetChanged();
            loadingDialog.cancel();
        }

        @Override
        public void onChannelDisconnected() {
            Log.e(TAG, "onChannelDisconnected");
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        initView();
        initEvent();
    }

    private void initEvent() {
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if (wifiP2pManager == null) {
            finish();
            return;
        }
        channel = wifiP2pManager.initialize(this, getMainLooper(), directActionListener);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, directActionListener);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
    }

    private void initView() {
        View.OnClickListener clickListener = v -> {
            long id = v.getId();
            if (id == R.id.btn_disconnect) {
                disconnect();
            } else if (id == R.id.btn_chooseFile) {
                navToChosePicture();
            }
        };
        setTitle("Send File");
        tv_myDeviceName = findViewById(R.id.tv_myDeviceName);
        tv_myDeviceAddress = findViewById(R.id.tv_myDeviceAddress);
        tv_myDeviceStatus = findViewById(R.id.tv_myDeviceStatus);
        tv_status = findViewById(R.id.tv_status);
        btn_disconnect = findViewById(R.id.btn_disconnect);
        btn_chooseFile = findViewById(R.id.btn_chooseFile);
        btn_disconnect.setOnClickListener(clickListener);
        btn_chooseFile.setOnClickListener(clickListener);
        loadingDialog = new LoadingDialog(this);
        RecyclerView rv_deviceList = findViewById(R.id.rv_deviceList);
        wifiP2pDeviceList = new ArrayList<>();
        deviceAdapter = new DeviceAdapter(wifiP2pDeviceList);
        deviceAdapter.setClickListener(position -> {
            mWifiP2pDevice = wifiP2pDeviceList.get(position);

            showToast(mWifiP2pDevice.deviceName);
            connect();
        });
        rv_deviceList.setAdapter(deviceAdapter);
        rv_deviceList.setLayoutManager(new LinearLayoutManager(this));
        iv_image = findViewById(R.id.iv_image);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Receiving file");
        progressDialog.setMax(100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_CHOOSE_FILE) {
            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(imageUri,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Log.e(TAG, "file path：" + picturePath);
                
                if (wifiP2pInfo != null) {
                    new WifiClientTask(this).execute(wifiP2pInfo.groupOwnerAddress.getHostAddress(), imageUri);
                    ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, picturePath,device,true);
                    msgDtoList.add(msgDto);
                    int newMsgPosition = msgDtoList.size() - 1;
                    chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                    msgRecyclerView.scrollToPosition(newMsgPosition);
                    msgInputText.setText("");
                }
            }
        }
    }

    private void connect() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showToast("Please grant location permission first");
            return;
        }
        WifiP2pConfig config = new WifiP2pConfig();
        if (config.deviceAddress != null && mWifiP2pDevice != null) {
            config.deviceAddress = mWifiP2pDevice.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            showLoadingDialog("connecting " + mWifiP2pDevice.deviceName);
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.e(TAG, "connect onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    showToast("Connection failed " + reason);
                    dismissLoadingDialog();
                }
            });
        }
    }

    private void disconnect() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.e(TAG, "disconnect onFailure:" + reasonCode);
            }

            @Override
            public void onSuccess() {
                Log.e(TAG, "disconnect onSuccess");
                tv_status.setText(null);
                btn_disconnect.setEnabled(false);
                btn_chooseFile.setEnabled(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        long id = item.getItemId();
        if (id == R.id.menuDirectEnable) {
            if (wifiP2pManager != null && channel != null) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            } else {
                showToast("The current device does not support Wifi Direct");
            }
            return true;
        } else if (id == R.id.menuDirectDiscover) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showToast("Please grant location permission first");
                return true;
            }

            if (!wifiP2pEnabled) {
                showToast("Need to turn on Wifi first");
                return true;
            }
            loadingDialog.show("Searching for nearby devices", true, false);
            wifiP2pDeviceList.clear();
            deviceAdapter.notifyDataSetChanged();
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    showToast("Success");
                }

                @Override
                public void onFailure(int reasonCode) {
                    showToast("Failure");
                    loadingDialog.cancel();
                }
            });
            return true;
        }
        return true;
    }

    private void navToChosePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, CODE_CHOOSE_FILE);
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
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                             EncodedMsg = Base64.getEncoder().encodeToString(bytes);
                            outputStream.write(EncodedMsg.getBytes());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
        }


        public void filewrite(byte[] bytes) throws IOException {
            String EncodedMsg;
            outputStream.write(bytes);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void filetransfer(Uri uri) throws IOException {
            Toast.makeText(SendFileActivity.this, "Inside Filetransfer function", Toast.LENGTH_LONG);
            int len;
            byte[] buf = new byte[1024];
            OutputStream out = outputStream;
            ContentResolver cr = getApplicationContext().getContentResolver();
            InputStream in = null;
            in = cr.openInputStream(uri);
            byte[] inbyte = getBytes(in);
            String encodedBytes = Base64.getEncoder().encodeToString(inbyte);
            out.write(encodedBytes.getBytes());
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void getBase64FromPath(String path) throws IOException {
            String base64 = "";
            File file = new File(path);
            byte[] buffer = new byte[(int) file.length() + 100];
            buffer = Files.readAllBytes(file.toPath());
            base64 = android.util.Base64.encodeToString(buffer, DEFAULT);
            filewrite(base64.getBytes());
        }

        private byte[] getBytes(InputStream is) throws IOException {

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while (((len = is.read(buffer)) != -1)) {
                byteBuffer.write(buffer, 0, len);
            }
            return byteBuffer.toByteArray();
        }
    }

    //    Handler to handle messages
    Handler handler = new Handler(new Handler.Callback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte [] readBuff = (byte[]) msg.obj;
                    byte[] decodeMsg = Base64.getMimeDecoder().decode(readBuff);
                    String tempMsg = new String(decodeMsg);
                    String actualMessage;
                    if(tempMsg.length() >40){
                        final File f = new File(Environment.getExternalStorageDirectory() + "/"
                                + SendFileActivity.this.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                                + ".jpg");

                        File dirs = new File(f.getParent());
                        if (!dirs.exists())
                            dirs.mkdirs();
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            FileOutputStream fos = new FileOutputStream(f);
                            fos.write(readBuff);
                            fos.close();
                            Toast.makeText(SendFileActivity.this,"File saved!",Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    actualMessage = tempMsg;
                    ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_RECEIVED, actualMessage,device);
                    msgDtoList.add(msgDto);
                    int newMsgPosition = msgDtoList.size() - 1;
                    chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                    msgRecyclerView.scrollToPosition(newMsgPosition);
                    break;
            }
            return true;
        }
    });

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
                socket.connect(new InetSocketAddress(hostAdress, Constants.PORT), 5000);
                sendReceive = new SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
              //  serverSocket = wifiServerService.serverSocket;//new ServerSocket(8888);
//                socket = wifiServerService.client;// serverSocket.accept();

                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(Constants.PORT));
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
                sendReceive.start();
            } catch (IOException e) {
                Log.e("Rohit",e.toString());
                e.printStackTrace();
            }
        }
    }

    void SetChat(){
        LayoutInflater inflater = (LayoutInflater) SendFileActivity.this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.activity_chat_app, null);
        SendFileActivity.this.setContentView(view);
        msgRecyclerView = view.findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerView.setLayoutManager(linearLayoutManager);
        msgRecyclerView.setAdapter(chatAppMsgAdapter);
        msgInputText = view.findViewById(R.id.chat_input_msg);
        msgSendButton = view.findViewById(R.id.chat_send_msg);
        final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;
        attachButton = view.findViewById(R.id.attach);

        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navToChosePicture();
            }
        });



        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Toast.makeText(SendFileActivity.this, "You are the HOST!", Toast.LENGTH_SHORT).show();
            serverClass = new ServerClass();
            serverClass.start();

            if(sendReceive == null){
                msgSendButton.setVisibility(View.GONE);
                Toast.makeText(SendFileActivity.this, "Something Went Wrong , You can't Send Messages", Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(SendFileActivity.this, "You are the CLIENT!", Toast.LENGTH_SHORT).show();
            SendFileActivity.this.setContentView(view);

            clientClass = new ClientClass(groupOwnerAdress);
            clientClass.start();
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


}