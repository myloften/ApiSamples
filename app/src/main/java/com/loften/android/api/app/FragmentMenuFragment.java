package com.loften.android.api.app;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.loften.android.api.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMenuFragment extends Fragment {
    Fragment mFragment1;
    Fragment mFragment2;
    private CheckBox menu1;
    private CheckBox menu2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_fragment_menu, container, false);

        initView(v);

        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = fm.findFragmentByTag("f1");
        if(mFragment1 == null){
            mFragment1 = new FragmentMenu.MenuFragment();
            ft.add(mFragment1, "f1");
        }
        mFragment2 = fm.findFragmentByTag("f2");
        if(mFragment2 == null){
            mFragment2 = new FragmentMenu.Menu2Fragment();
            ft.add(mFragment2, "f2");
        }
        ft.commit();

        updateFragmentVisibility();

        return v;
    }

    private void initView(View v) {
        menu1 = (CheckBox) v.findViewById(R.id.menu1);
        menu1.setOnClickListener(mClickListener);
        menu2 = (CheckBox) v.findViewById(R.id.menu2);
        menu2.setOnClickListener(mClickListener);
    }

    final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateFragmentVisibility();
        }
    };

    private void updateFragmentVisibility() {
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if(menu1.isChecked()){
            ft.show(mFragment1);
        }else{
            ft.hide(mFragment1);
        }

        if(menu2.isChecked()){
            ft.show(mFragment2);
        }else{
            ft.hide(mFragment2);
        }
        ft.commit();
    }

}
