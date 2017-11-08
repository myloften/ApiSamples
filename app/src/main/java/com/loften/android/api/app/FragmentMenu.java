package com.loften.android.api.app;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.loften.android.api.R;

public class FragmentMenu extends ToolbarActivity {
    Fragment mFragment1;
    Fragment mFragment2;
    private CheckBox menu1;
    private CheckBox menu2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_menu);
        initView();

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mFragment1 = fm.findFragmentByTag("f1");
        if(mFragment1 == null){
            mFragment1 = new MenuFragment();
            ft.add(mFragment1, "f1");
        }
        mFragment2 = fm.findFragmentByTag("f2");
        if(mFragment2 == null){
            mFragment2 = new Menu2Fragment();
            ft.add(mFragment2, "f2");
        }
        ft.commit();

        updateFragmentVisibility();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateFragmentVisibility();
    }

    private void initView() {
        menu1 = (CheckBox) findViewById(R.id.menu1);
        menu1.setOnClickListener(mClickListener);
        menu2 = (CheckBox) findViewById(R.id.menu2);
        menu2.setOnClickListener(mClickListener);
    }

    final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateFragmentVisibility();
        }
    };

    private void updateFragmentVisibility() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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

    public static class MenuFragment extends Fragment{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.add("Menu 1a").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add("Menu 1b").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    public static class Menu2Fragment extends Fragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.add("Menu 2").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }
}
