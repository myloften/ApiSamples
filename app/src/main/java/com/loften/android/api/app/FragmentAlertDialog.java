package com.loften.android.api.app;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.loften.android.api.R;

public class FragmentAlertDialog extends AppCompatActivity implements View.OnClickListener {

    private TextView text;
    private Button show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_alert_dialog);
        initView();
    }

    private void initView() {
        text = (TextView) findViewById(R.id.text);
        text.setText(getString(R.string.fragment_alert_dialog_text));
        show = (Button) findViewById(R.id.show);

        show.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show:
                showDialog();
                break;
        }
    }

    private void showDialog(){
        MyAlertDialogFragment newFragment = MyAlertDialogFragment.newInstance(
                R.string.fragment_alert_dialog_title);
        newFragment.show(getFragmentManager(),"dialog");
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int title){
            MyAlertDialogFragment fragment = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", title);
            fragment.setArguments(args);
            fragment.setCancelable(false);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.head)
                    .setTitle(title)
                    .setPositiveButton(R.string.postive_button, null)
                    .setNegativeButton(R.string.negative_button, null)
                    .create();
        }
    }
}
