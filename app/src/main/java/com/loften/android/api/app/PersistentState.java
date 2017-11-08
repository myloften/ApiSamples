package com.loften.android.api.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import com.loften.android.api.R;

public class PersistentState extends AppCompatActivity {

    private EditText savedEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persistent_state);
        initView();
    }

    private void initView() {
        savedEdit = (EditText) findViewById(R.id.saved_edit);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getPreferences(0);
        String restoredText = prefs.getString("text", null);
        if(restoredText != null){
            savedEdit.setText(restoredText, TextView.BufferType.EDITABLE);

            int selectionStart = prefs.getInt("selection-start", -1);
            int selectionEnd = prefs.getInt("selection-end", -1);
            if(selectionStart != -1 && selectionEnd != -1){
                savedEdit.setSelection(selectionStart, selectionEnd);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putString("text", savedEdit.getText().toString());
        editor.putInt("selection-start", savedEdit.getSelectionStart());
        editor.putInt("selection-end", savedEdit.getSelectionEnd());
        editor.commit();
    }
}
