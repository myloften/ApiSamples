package com.loften.android.api.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.loften.android.api.R;

public class SoftInputModes extends AppCompatActivity {

    private Spinner resizeMode;

    final CharSequence[] mResizeModeLabels = new CharSequence[] {
            "Unspecified", "Resize", "Pan", "Nothing"
    };
    final int[] mResizeModeValues = new int[] {
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN,
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_input_modes);
        initView();

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, mResizeModeLabels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resizeMode.setAdapter(adapter);
        resizeMode.setSelection(0);
        resizeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getWindow().setSoftInputMode(mResizeModeValues[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                getWindow().setSoftInputMode(mResizeModeValues[0]);
            }
        });
    }

    private void initView() {
        resizeMode = (Spinner) findViewById(R.id.resize_mode);
    }

}
