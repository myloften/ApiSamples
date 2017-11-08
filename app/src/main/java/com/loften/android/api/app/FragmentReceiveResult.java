package com.loften.android.api.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.loften.android.api.R;

public class FragmentReceiveResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout frame = new FrameLayout(this);
        frame.setId(R.id.simple_fragment);
        setContentView(frame, lp);

        if(savedInstanceState == null){
            Fragment newFragment = new ReceiveResultFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.simple_fragment, newFragment).commit();
        }
    }

    public static class ReceiveResultFragment extends Fragment {
        static final private int GET_CODE = 0;

        private TextView mResults;

        private View.OnClickListener mGetListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SendResult.class);
                startActivityForResult(intent, GET_CODE);
            }
        };

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.activity_receive_result, container, false);

            mResults = (TextView)v.findViewById(R.id.result);

            mResults.setText(mResults.getText(), TextView.BufferType.EDITABLE);

            Button getButton = (Button)v.findViewById(R.id.get);
            getButton.setOnClickListener(mGetListener);
            return v;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == GET_CODE){
                Editable text = (Editable)mResults.getText();

                if(resultCode == RESULT_CANCELED){
                    text.append("(cancelled)");
                }else{
                    text.append("(okay ");
                    text.append(Integer.toString(resultCode));
                    text.append(") ");
                    if(data != null){
                        text.append(data.getAction());
                    }
                }

                text.append("\n");
            }
        }
    }
}
