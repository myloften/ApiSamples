package com.loften.android.api.app;

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

public class FragmentStack extends AppCompatActivity implements View.OnClickListener {
    int mStackLevel = 1;

    private Button newFragment;
    private Button deleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_stack);
        initView();
        if(savedInstanceState == null){
            CountingFragment newFragment = CountingFragment.newInstance(mStackLevel);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.simple_fragment, newFragment);
        }else{
            mStackLevel = savedInstanceState.getInt("level");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }

    private void initView() {
        newFragment = (Button) findViewById(R.id.new_fragment);
        deleteFragment = (Button) findViewById(R.id.delete_fragment);

        newFragment.setOnClickListener(this);
        deleteFragment.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_fragment:
                addFragmentToStack();
                break;
            case R.id.delete_fragment:
                getFragmentManager().popBackStack();
                break;
        }
    }

    void addFragmentToStack(){
        mStackLevel ++;

        CountingFragment newFragment = CountingFragment.newInstance(mStackLevel);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.simple_fragment, newFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public static class CountingFragment extends Fragment{
        int mNum;

        static CountingFragment newInstance(int num){
            CountingFragment f = new CountingFragment();

            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num"):1;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.hello_world, container, false);
            TextView tv = (TextView) v.findViewById(R.id.text);
            tv.setText("Fragment #"+mNum);
            tv.setBackgroundResource(android.R.drawable.gallery_thumb);
            return v;
        }
    }
}
