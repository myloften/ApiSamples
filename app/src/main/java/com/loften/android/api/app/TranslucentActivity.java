package com.loften.android.api.app;


import android.app.Activity;
import android.os.Bundle;

import com.loften.android.api.R;

public class TranslucentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translucent);
    }
}
