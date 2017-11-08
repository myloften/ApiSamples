package com.loften.android.api.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loften.android.api.R;

public class FragmentHideShow extends AppCompatActivity implements View.OnClickListener {

    private Button frag1hide;
    private Button frag2hide;
    private Fragment fragment1;
    private Fragment fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_hide_show);
        initView();
    }

    private void initView() {
        frag1hide = (Button) findViewById(R.id.frag1hide);
        frag2hide = (Button) findViewById(R.id.frag2hide);

        frag1hide.setOnClickListener(this);
        frag2hide.setOnClickListener(this);
        FragmentManager fm = getSupportFragmentManager();
        fragment1 = fm.findFragmentById(R.id.fragment1);
        fragment2 = fm.findFragmentById(R.id.fragment2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frag1hide:
                addShowHideListener(frag1hide, fragment1);
                break;
            case R.id.frag2hide:
                addShowHideListener(frag2hide, fragment2);
                break;
        }
    }


    void addShowHideListener(Button button, final Fragment fragment) {
        /**
         * v4包下的FragmentTransaction需要使用android.R.anim下的动画
         * android.app下的FragmentTransaction需要使用android.R.animator的动画
         */
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);

        if (fragment.isHidden()) {
            ft.show(fragment);
            button.setText(R.string.hide);
        } else {
            ft.hide(fragment);
            button.setText(R.string.show);
        }
        ft.commit();
    }

    public static class FirstFragment extends Fragment {
        TextView mTextView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView) tv).setText(R.string.activity_fragment_text);

            mTextView = (EditText) v.findViewById(R.id.saved);
            if (savedInstanceState != null) {
                mTextView.setText(savedInstanceState.getCharSequence("text"));
            }
            return v;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putCharSequence("text", mTextView.getText());
        }
    }

    public static class SecondFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.labeled_text_edit, container, false);
            View tv = v.findViewById(R.id.msg);
            ((TextView) tv).setText(R.string.activity_fragment_text);

            ((EditText) v.findViewById(R.id.saved)).setSaveEnabled(true);
            return v;
        }
    }
}
