package com.loften.android.api.app;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class FragmentDialogOrActivity extends AppCompatActivity implements View.OnClickListener {

    private Button showDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_or_fragment_dialog);
        initView();

        if (savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            MyDialogFragment fragment = MyDialogFragment.newInstance();
            ft.add(R.id.embedded, fragment);
            ft.commit();
        }
    }

    private void initView() {
        showDialog = (Button) findViewById(R.id.show_dialog);

        showDialog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_dialog:
                showDialog();
                break;
        }
    }

    void showDialog() {
        MyDialogFragment newFragment = MyDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }

    public static class MyDialogFragment extends DialogFragment {
        static MyDialogFragment newInstance() {
            return new MyDialogFragment();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            TextView tv = (TextView) v.findViewById(R.id.text);
            tv.setText(R.string.fragment_dialog_text);
            return v;
        }
    }
}
