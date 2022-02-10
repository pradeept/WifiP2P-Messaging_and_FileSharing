package github.nullnet.wifip2p;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import github.nullnet.wifip2p.ChatCode.Listandchat;


public class MainActivity extends BaseActivity {

    private static final int CODE_REQ_PERMISSIONS = 665;
    ImageView wifiserchingpng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION}, CODE_REQ_PERMISSIONS);
//        findViewById(R.id.btnCheckPermission).setOnClickListener(v ->
//                );
        findViewById(R.id.btnSender).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SendFileActivity.class)));

        wifiserchingpng = findViewById(R.id.wifiserchingpng);

        wifiserchingpng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Listandchat.class);
                intent.putExtra("sender","true");
                intent.putExtra("image","false"); //just Want To Chat
                startActivity(intent);
            }
        });

        findViewById(R.id.btnSender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendFileActivity.class);
                intent.putExtra("image","true");
                intent.putExtra("sender","true"); //Will Send Images
                startActivity(intent);
            }
        });

//        findViewById(R.id.btnReceiver).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, SendFileActivity.class);
//                intent.putExtra("sender","false");
//                startActivity(intent);
//            }
//        });



        findViewById(R.id.btnReceiver).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ReceiveFileActivity.class)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_REQ_PERMISSIONS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    showToast("Lack of permissions，Please grant permission first: " + permissions[i]);
                    return;
                }
            }
            showToast("Permission granted");
        }
    }

}