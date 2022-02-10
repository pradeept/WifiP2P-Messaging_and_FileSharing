package github.nullnet.wifip2p.ChatCode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import github.nullnet.wifip2p.R;

public class MainActivity2 extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;

    ScanFragment scanFragment;
    HelpFragment helpFragment;



    public String[] PermissionsList = {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.MANAGE_EXTERNAL_STORAGE};

    public int permsRequestCode = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tablayout);



        //      Permissions

        for (int i = 0; i < PermissionsList.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity2.this, PermissionsList[i]) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    requestPermissions(PermissionsList, permsRequestCode);

                }
            } else {
//                Toast.makeText(MainActivity2.this, "Permissions Granted Already!", Toast.LENGTH_SHORT).show();
            }
        }

        //Viewpager

        scanFragment = new ScanFragment();
        helpFragment = new HelpFragment();

        tabLayout.setupWithViewPager(viewPager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),0);
        viewPagerAdapter.addFragment(scanFragment,"Scan");
        viewPagerAdapter.addFragment(helpFragment,"Help");
        viewPager.setAdapter(viewPagerAdapter);
    }
    //Permission (Override stuff)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (permsRequestCode) {
            case 200:
                if(grantResults.length > 0 &&  grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity2.this,"Permission[1] granted!",Toast.LENGTH_LONG);
                }
                else{Toast.makeText(MainActivity2.this,"Permission[1] Denied!",Toast.LENGTH_LONG);}
                if(grantResults.length > 0 &&  grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Log.i("myTag","Permission 1 granted");}
                if(grantResults.length > 0 &&  grantResults[2] == PackageManager.PERMISSION_GRANTED){Log.i("myTag","Permission 2 granted");}
                if(grantResults.length > 0 &&  grantResults[3] == PackageManager.PERMISSION_GRANTED){Log.i("myTag","Permission 3 granted");}
                if(grantResults.length > 0 &&  grantResults[4] == PackageManager.PERMISSION_GRANTED){Log.i("myTag","Permission 4 granted");}
                if(grantResults.length > 0 &&  grantResults[5] == PackageManager.PERMISSION_GRANTED){Log.i("myTag","Permission 5 granted");}
                if(grantResults.length > 0 &&  grantResults[5] == PackageManager.PERMISSION_GRANTED){Log.i("myTag","Permission 6 granted");}

                break;
        }
    }


    private class ViewPagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragments = new ArrayList<>();
        List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public  void addFragment(Fragment fragment,String title){
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_settings:

                break;
            case R.id.menu_darkmode:
                Toast.makeText(this, "Homepage", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_about:
                Toast.makeText(this, "Personal Center", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

}
