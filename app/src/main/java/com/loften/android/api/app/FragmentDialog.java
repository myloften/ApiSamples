package com.loften.android.api.app;

import android.app.DialogFragment;
import android.app.Fragment;
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

public class FragmentDialog extends AppCompatActivity implements View.OnClickListener {

    private Button show;

    int mStackLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_dialog);
        initView();

        if(savedInstanceState != null){
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    private void initView() {
        show = (Button) findViewById(R.id.show);

        show.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show:
                showDialog();
                break;
        }
    }

    void showDialog(){
        mStackLevel ++;

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if(prev != null){
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        MyDialogFragment newFragment = MyDialogFragment.newInstance(mStackLevel);
        newFragment.show(ft, "dialog");
    }

    public static class MyDialogFragment extends DialogFragment {
        int mNum;

        static MyDialogFragment newInstance(int num){
            MyDialogFragment f = new MyDialogFragment();
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments().getInt("num");

            int style = DialogFragment.STYLE_NORMAL, theme = 0;
            switch ((mNum-1)%6){
                case 1: style = DialogFragment.STYLE_NO_TITLE; break;
                case 2: style = DialogFragment.STYLE_NO_FRAME; break;
                case 3: style = DialogFragment.STYLE_NO_INPUT; break;
                case 4: style = DialogFragment.STYLE_NORMAL; break;
                case 5: style = DialogFragment.STYLE_NORMAL; break;
            }
            switch ((mNum-1)%6){
                case 4: theme = android.R.style.Theme_Holo; break;
                case 5: theme = android.R.style.Theme_Holo_Light_Dialog; break;
            }
            setStyle(style, theme);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.activity_fragment_dialog, container, false);
            TextView tv = (TextView)v.findViewById(R.id.text);
            tv.setText("Dialog #"+mNum+": style "+ getNameForNum(mNum));
            Button button = (Button)v.findViewById(R.id.show);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((FragmentDialog)getActivity()).showDialog();
                }
            });
            return v;
        }
    }

    static String getNameForNum(int num) {
        switch ((num-1)%6) {
            case 1: return "STYLE_NO_TITLE";
            case 2: return "STYLE_NO_FRAME";
            case 3: return "STYLE_NO_INPUT (this window can't receive input, so "
                    + "you will need to press the bottom show button)";
            case 4: return "STYLE_NORMAL with dark fullscreen theme";
            case 5: return "STYLE_NORMAL with light theme";
            case 6: return "STYLE_NO_TITLE with light theme";
            case 7: return "STYLE_NO_FRAME with light theme";
            case 8: return "STYLE_NORMAL with light fullscreen theme";
        }
        return "STYLE_NORMAL";
    }
}
