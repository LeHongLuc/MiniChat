package com.example.minichat.Service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.minichat.Activity.MainActivity;
import com.example.minichat.utilities.ChatHeadService;

public class ServiceChat extends Service {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    public ServiceChat() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//
//        //chat head
//        if (!Settings.canDrawOverlays(this)) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//        } else {
//            initializeView();
//        }
//
//    }
//
//    private void initializeView() {
//        binding.tvName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startService(new Intent(MainActivity.this, ChatHeadService.class));
//                finish();
//            }
//        });
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
//
//            //Check if the permission is granted or not.
//            // Settings activity never returns proper value so instead check with following method
//            if (Settings.canDrawOverlays(this)) {
//                initializeView();
//            } else { //Permission is not available
//                Toast.makeText(this,
//                        "Draw over other app permission not available. Closing the application",
//                        Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }


}