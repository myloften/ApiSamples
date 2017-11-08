package com.loften.android.api.app;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loften.android.api.R;

public class FragmentArguments extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_arguments);

        if(savedInstanceState == null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            MyFragment newFragment = MyFragment.newInstance(getString(R.string.fragment_arguments_add));
            ft.add(R.id.created, newFragment);
            ft.commit();
        }
    }

    public static class MyFragment extends Fragment {
        CharSequence mLabel;

        static MyFragment newInstance(CharSequence label){
            MyFragment f = new MyFragment();
            Bundle b = new Bundle();
            b.putCharSequence("label", label);
            f.setArguments(b);
            return f;
        }

        @Override
        public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
            super.onInflate(context, attrs, savedInstanceState);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.FragmentArguments);
            mLabel = a.getText(R.styleable.FragmentArguments_android_label);
            a.recycle();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();
            if(args != null){
                mLabel = args.getCharSequence("label", mLabel);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.quick_contacts, container, false);
            TextView tv = (TextView)v.findViewById(R.id.name);
            tv.setText(mLabel != null ? mLabel : "(no label)");
            return v;
        }
    }
}
