package com.loften.android.api.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.loften.android.api.R;

/**
 * 这里主要列举两种启动服务
 */
public class LocalServiceActivity{

    /**
     * 直接通过startService()绑定服务
     */
    public static class Controller extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_local_service);

            Button button = (Button)findViewById(R.id.start);
            button.setOnClickListener(mStartListener);
            button = (Button)findViewById(R.id.stop);
            button.setOnClickListener(mStopListener);
        }

        private View.OnClickListener mStartListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(Controller.this, LocalService.class));
            }
        };

        private View.OnClickListener mStopListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(Controller.this, LocalService.class));
            }
        };
    }

    /**
     * 通过bindService绑定服务
     */
    public static class Binding extends AppCompatActivity{
        private boolean mIsBound;

        private LocalService mBoundService;

        private ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBoundService = ((LocalService.LocalBindel)service).getService();

                Toast.makeText(Binding.this, R.string.local_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mBoundService = null;
                Toast.makeText(Binding.this, R.string.local_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };

        void doBindService(){
            bindService(new Intent(Binding.this,
                    LocalService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }

        void doUnbindService(){
            if(mIsBound){
                unbindService(mConnection);
                mIsBound = false;
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            doUnbindService();
        }

        private View.OnClickListener mBindListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBindService();
            }
        };

        private View.OnClickListener mUnbindListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doUnbindService();
            }
        };

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.local_service_binding);

            Button button = (Button)findViewById(R.id.bind);
            button.setOnClickListener(mBindListener);
            button = (Button)findViewById(R.id.unbind);
            button.setOnClickListener(mUnbindListener);
        }
    }
}
