package com.loften.android.api.app;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loften.android.api.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentArgumentsFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            FragmentArguments.MyFragment newFragment = FragmentArguments.MyFragment.newInstance(getString(R.string.fragment_arguments_add));
            ft.add(R.id.created, newFragment);
            ft.commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_fragment_arguments, container, false);
        return v;
    }

}
