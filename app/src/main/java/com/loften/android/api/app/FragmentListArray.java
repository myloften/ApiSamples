package com.loften.android.api.app;

import android.app.ListFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.loften.android.api.Shakespeare;

public class FragmentListArray extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getFragmentManager().findFragmentById(android.R.id.content) == null){
            ArrayListFragment list = new ArrayListFragment();
            getFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class ArrayListFragment extends ListFragment{
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, Shakespeare.TITLES));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            Toast.makeText(getActivity(), "Item clicked:"+id, Toast.LENGTH_SHORT).show();
        }
    }
}
