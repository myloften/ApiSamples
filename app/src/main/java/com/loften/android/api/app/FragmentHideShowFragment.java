package com.loften.android.api.app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.loften.android.api.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHideShowFragment extends Fragment {
    private Button frag1hide;
    private Button frag2hide;
    private Fragment fragment1;
    private Fragment fragment2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_hide_show, container, false);
        initView(v);
        return v;
    }

    private void initView(View v) {
        frag1hide = (Button) v.findViewById(R.id.frag1hide);
        frag2hide = (Button) v.findViewById(R.id.frag2hide);

        frag1hide.setOnClickListener(mClickListener);
        frag2hide.setOnClickListener(mClickListener);
        FragmentManager fm = getChildFragmentManager();
        fragment1 = fm.findFragmentById(R.id.fragment1);
        fragment2 = fm.findFragmentById(R.id.fragment2);
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
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
    };

    void addShowHideListener(Button button, Fragment fragment){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);

        if(fragment.isHidden()){
            ft.show(fragment);
            button.setText(R.string.hide);
        }else {
            ft.hide(fragment);
            button.setText(R.string.show);
        }
        ft.commit();
    }
}
