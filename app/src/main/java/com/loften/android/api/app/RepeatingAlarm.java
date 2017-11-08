package com.loften.android.api.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.loften.android.api.R;

public class RepeatingAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, R.string.repeating_received, Toast.LENGTH_SHORT).show();
    }
}
