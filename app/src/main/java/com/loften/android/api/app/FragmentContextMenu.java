package com.loften.android.api.app;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loften.android.api.R;

public class FragmentContextMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContextMenuFragment content = new ContextMenuFragment();
        getFragmentManager().beginTransaction().add(android.R.id.content, content).commit();
    }

    public static class ContextMenuFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_context_menu, container, false);
            registerForContextMenu(root.findViewById(R.id.long_press));
            return root;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.add(Menu.NONE, R.id.a_item, Menu.NONE, "Menu A");
            menu.add(Menu.NONE, R.id.b_item, Menu.NONE, "Menu B");
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.a_item:
                    Toast.makeText(getActivity(), "Item 1a was chosen", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.b_item:
                    Toast.makeText(getActivity(), "Item 1b was chosen", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return super.onContextItemSelected(item);
        }
    }
}
