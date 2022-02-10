package github.nullnet.wifip2p;

import static android.util.Base64.DEFAULT;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import com.bumptech.glide.Glide;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

import github.nullnet.wifip2p.ChatCode.ChatAppMsgAdapter;
import github.nullnet.wifip2p.ChatCode.ChatAppMsgDTO;
import github.nullnet.wifip2p.broadcast.DirectBroadcastReceiver;
import github.nullnet.wifip2p.callback.DirectActionListener;
import github.nullnet.wifip2p.common.Constants;
import github.nullnet.wifip2p.model.FileTransfer;
import github.nullnet.wifip2p.service.WifiServerService;


public class ReceiveFileActivity extends BaseActivity {

    private ImageView iv_image;

    private TextView tv_log;

    private ProgressDialog progressDialog;

    private WifiP2pManager wifiP2pManager;

    private WifiP2pManager.Channel channel;

    private boolean connectionInfoAvailable;

    private BroadcastReceiver broadcastReceiver;

    private WifiServerService wifiServerService;

    SendReceive sendReceive;
    public WifiP2pDevice device ; //set Device
    final List<ChatAppMsgDTO> msgDtoList = new ArrayList<ChatAppMsgDTO>();
    public View view;
    final ChatAppMsgAdapter chatAppMsgAdapter = new ChatAppMsgAdapter(msgDtoList,ReceiveFileActivity.this);
    RecyclerView msgRecyclerView = null;
    ImageView msgSendButton ;
    EditText msgInputText ;
    private final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    private WifiP2pInfo wifiP2pInfo;
    ImageButton attachButton;

    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WifiServerService.WifiServerBinder binder = (WifiServerService.WifiServerBinder) service;
            wifiServerService = binder.getService();
            wifiServerService.setProgressChangListener(progressChangListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (wifiServerService != null) {
                wifiServerService.setProgressChangListener(null);
                wifiServerService = null;
            }
            bindService();
        }
    };

    private final DirectActionListener directActionListener = new DirectActionListener() {
        @Override
        public void wifiP2pEnabled(boolean enabled) {
            log("wifiP2pEnabled: " + enabled);
        }

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            log("onConnectionInfoAvailable");
            log("isGroupOwner：" + wifiP2pInfo.isGroupOwner);
            log("groupFormed：" + wifiP2pInfo.groupFormed);
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionInfoAvailable = true;
                if (wifiServerService != null) {
                    startService(WifiServerService.class);
                }
            }
            ReceiveFileActivity.this.wifiP2pInfo = wifiP2pInfo;
            //Code For Chat
            SetChat();
        }

        @Override
        public void onDisconnection() {
            connectionInfoAvailable = false;
            log("onDisconnection");
        }

        @Override
        public void onSelfDeviceAvailable(WifiP2pDevice wifiP2pDevice) {
            log("Device - "+wifiP2pDevice.deviceName);
            device = wifiP2pDevice;
        }

        @Override
        public void onPeersAvailable(Collection<WifiP2pDevice> wifiP2pDeviceList) {
            log("onPeersAvailable, size:" + wifiP2pDeviceList.size());
            for (WifiP2pDevice wifiP2pDevice : wifiP2pDeviceList) {
                log(wifiP2pDevice.toString());
            }
        }

        @Override
        public void onChannelDisconnected() {
            log("onChannelDisconnected");
        }
    };

    private final WifiServerService.OnProgressChangListener progressChangListener = new WifiServerService.OnProgressChangListener() {
        @Override
        public void onProgressChanged(final FileTransfer fileTransfer, final int progress) {
            runOnUiThread(() -> {
                progressDialog.setMessage("file name： " + fileTransfer.getFileName());
                progressDialog.setProgress(progress);
                progressDialog.show();
            });
        }

        @Override
        public void onTransferFinished(final File file) {
            runOnUiThread(() -> {
                progressDialog.cancel();
                if (file != null && file.exists()) {
                    Glide.with(ReceiveFileActivity.this).load(file.getPath()).into(iv_image);
                    Log.e("Rohit","Path "+file.getPath());

                    // sendReceive.write(msgContent.getBytes());
                    // Add a new sent message to the list.
                    ChatAppMsgDTO msgDto = new ChatAppMsgDTO(ChatAppMsgDTO.MSG_TYPE_SENT, file.getPath(),device,true);
                    msgDtoList.add(msgDto);
                    int newMsgPosition = msgDtoList.size() - 1;
                    // Notify recycler view insert one new data.
                    chatAppMsgAdapter.notifyItemInserted(newMsgPosition);
                    // Scroll RecyclerView to the last message.
                    msgRecyclerView.scrollToPosition(newMsgPosition);
                    // Empty the input edit text box.
                    msgInputText.setText("");


                    try {
                        saveImage(file.getPath());
                        Toast.makeText(ReceiveFileActivity.this, "File Saved", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(ReceiveFileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_file);
        initView();
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if (wifiP2pManager == null) {
            finish();
            return;
        }
        channel = wifiP2pManager.initialize(this, getMainLooper(), directActionListener);
        broadcastReceiver = new DirectBroadcastReceiver(wifiP2pManager, channel, directActionListener);
        registerReceiver(broadcastReceiver, DirectBroadcastReceiver.getIntentFilter());
        bindService();
    }

    private void initView() {
        setTitle("Receive files");
        iv_image = findViewById(R.id.iv_image);
        tv_log = findViewById(R.id.tv_log);
        findViewById(R.id.btnCreateGroup).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(ReceiveFileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    log("createGroup onSuccess");
                    dismissLoadingDialog();
                    showToast("onSuccess");
                }

                @Override
                public void onFailure(int reason) {
                    log("createGroup onFailure: " + reason);
                    dismissLoadingDialog();
                    showToast("onFailure");
                }
            });
        });
        findViewById(R.id.btnRemoveGroup).setOnClickListener(v -> removeGroup());
        findViewById(R.id.btnSendImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReceiveFileActivity.this, SendFileActivity.class);
                intent.putExtra("sender","true");
                intent.putExtra("image","false"); //just Want To Chat
                startActivity(intent);
            }
        });
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
        if (wifiServerService != null) {
            wifiServerService.setProgressChangListener(null);
            unbindService(serviceConnection);
        }
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(this, WifiServerService.class));
        if (connectionInfoAvailable) {
            removeGroup();
        }
    }

    private void removeGroup() {
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                log("removeGroup onSuccess");
                showToast("Success");
            }
            @Override
            public void onFailure(int reason) {
                log("No Data To Clear");
                showToast("Success");
            }
        });
    }

    private void log(String log) {
        tv_log.append(log + "\n");
        tv_log.append("----------" + "\n");
    }

    private void bindService() {
        Intent intent = new Intent(ReceiveFileActivity.this, WifiServerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }


    public  class SendReceive extends Thread {
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
            Toast.makeText(ReceiveFileActivity.this, "Inside Filetransfer function", Toast.LENGTH_LONG);
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
                                + ReceiveFileActivity.this.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
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
                            Toast.makeText(ReceiveFileActivity.this,"File saved!",Toast.LENGTH_LONG).show();
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
                sendReceive = new ReceiveFileActivity.SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
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

//                serverSocket = new ServerSocket(8888);
//                socket = serverSocket.accept();
                //socket = wifiServerService.client;
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(Constants.PORT));
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket,socket.getInputStream(),socket.getOutputStream());
                sendReceive.start();
            } catch (IOException e) {
                Log.e("Rohit","Server"+e.toString());
                e.printStackTrace();
            }
        }
    }

    void SetChat(){
        LayoutInflater inflater = (LayoutInflater) ReceiveFileActivity.this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.activity_chat_app, null);
        ReceiveFileActivity.this.setContentView(view);
        msgRecyclerView = view.findViewById(R.id.chat_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        msgRecyclerView.setLayoutManager(linearLayoutManager);
        msgRecyclerView.setAdapter(chatAppMsgAdapter);
        msgInputText = view.findViewById(R.id.chat_input_msg);
        msgSendButton = view.findViewById(R.id.chat_send_msg);
        final InetAddress groupOwnerAdress = wifiP2pInfo.groupOwnerAddress;


        attachButton = view.findViewById(R.id.attach);
        attachButton.setVisibility(View.GONE);
//        attachButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                navToChosePicture();
//            }
//        });




        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Toast.makeText(ReceiveFileActivity.this, "You are the HOST!", Toast.LENGTH_SHORT).show();
            serverClass = new ServerClass();
            serverClass.start();

            if(sendReceive == null){
                msgSendButton.setVisibility(View.GONE);
                msgInputText.setVisibility(View.GONE);
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
            Toast.makeText(ReceiveFileActivity.this, "You are the CLIENT!", Toast.LENGTH_SHORT).show();
            ReceiveFileActivity.this.setContentView(view);

            clientClass = new ReceiveFileActivity.ClientClass(groupOwnerAdress);
            clientClass.start();
            msgSendButton.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    String msgContent = msgInputText.getText().toString();

                    if(!TextUtils.isEmpty(msgContent) )
                    {
                       // sendReceive.write(msgContent.getBytes());
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


    private boolean saveImage(String filePath) throws IOException {
        boolean saved;
        OutputStream fos;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = ReceiveFileActivity.this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + "NetNull");
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + "NetNull";

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
        return saved;
    }
}