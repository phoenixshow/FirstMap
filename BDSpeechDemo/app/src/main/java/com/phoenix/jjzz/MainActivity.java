package com.phoenix.jjzz;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean isGrant = true;
    private final int SDK_PERMISSION_REQUEST = 127;
    private CheckBox cbNLU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cbNLU = (CheckBox) findViewById(R.id.cbNLU);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        sp.edit().putString(Constant.EXTRA_LANGUAGE, "cmn-Hans-CN").commit();//普通话
//        sp.edit().putString(Constant.EXTRA_LANGUAGE, "sichuan-Hans-CN").commit();//四川话
//        sp.edit().putString(Constant.EXTRA_LANGUAGE, "yue-Hans-CN").commit();//粤语
//        sp.edit().putString(Constant.EXTRA_LANGUAGE, "en-GB").commit();//英语

        cbNLU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sp.edit().putString(Constant.EXTRA_NLU, "enable").commit();
                }else{
                    sp.edit().putString(Constant.EXTRA_NLU, "disable").commit();
                }
            }
        });

        getPersimmions();
    }

    public void touchMode(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, ActivityTouch.class);
            startActivity(intent);
        }
    }

    public void apiMode(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, ApiActivity.class);
            startActivity(intent);
        }
    }

    public void offlineMode(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, ActivityOffline.class);
            startActivity(intent);
        }
    }

    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.CALL_PHONE);
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case SDK_PERMISSION_REQUEST:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // 允许
                    Toast.makeText(this,getString(R.string.permisstion_grant),Toast.LENGTH_SHORT).show();
                }else{
                    // 不允许
                    isGrant = false;
                    Toast.makeText(this,getString(R.string.permisstion_deny),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void tts(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, TTSMainActivity.class);
            startActivity(intent);
        }
    }

    public void wakeUp(View view) {
        if (isGrant) {
            Intent intent = new Intent(MainActivity.this, ActivityWakeUp.class);
            startActivity(intent);
        }
    }
}
