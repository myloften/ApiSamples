package com.loften.android.api.app;

import android.Manifest;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import static com.loften.android.api.app.QuickContactsDemo.MY_PERMISSIONS_REQUEST_CONTACTS;

/**
 * 转屏时防止fragment重建setRetainInstance(true)
 */
public class LoaderRetained extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_CONTACTS);
        }else {
            initCursorLoader();
        }
    }

    private void initCursorLoader(){
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            CursorLoaderListFragment list = new CursorLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == MY_PERMISSIONS_REQUEST_CONTACTS){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initCursorLoader();
            }else{
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static class CursorLoaderListFragment extends ListFragment
            implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
            LoaderManager.LoaderCallbacks<Cursor> {

        SimpleCursorAdapter mAdapter;
        SearchView mSearchView;
        String mCurFilter;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            //转屏fragment不会重新创建
            setRetainInstance(true);

            setEmptyText("没有内容");

            //指出fragment愿意添加item到选项菜单
            setHasOptionsMenu(true);

            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_2, null,
                    new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.CONTACT_STATUS},
                    new int[]{android.R.id.text1, android.R.id.text2}, 0);
            setListAdapter(mAdapter);

            //控制是否显示列表
            setListShown(false);

            getLoaderManager().initLoader(0, null, this);
        }

        public static class MySearchView extends SearchView {
            public MySearchView(Context context) {
                super(context);
            }

            @Override
            public void onActionViewCollapsed() {
                setQuery("", false);
                super.onActionViewCollapsed();
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            MenuItem item = menu.add("Search");
            item.setIcon(android.R.drawable.ic_menu_search);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            mSearchView = new LoaderCursor.CursorLoaderListFragment.MySearchView(getActivity());
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setIconifiedByDefault(true);
            item.setActionView(mSearchView);
        }

        static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.CONTACT_STATUS,
                ContactsContract.Contacts.CONTACT_PRESENCE,
                ContactsContract.Contacts.PHOTO_ID,
                ContactsContract.Contacts.LOOKUP_KEY
        };

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            Uri baseUri;
            if(mCurFilter != null){
                baseUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mCurFilter));
            }else{
                baseUri = ContactsContract.Contacts.CONTENT_URI;
            }

            String select = "((" + ContactsContract.Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                    + ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                    + ContactsContract.Contacts.DISPLAY_NAME + " != '' ))";
            return new CursorLoader(getActivity(), baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            //自动完成所有数据填充功能
            mAdapter.swapCursor(cursor);

            if(isResumed()){
                setListShown(true);
            }else{
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            Toast.makeText(getActivity(), "Item clicked:"+position, Toast.LENGTH_SHORT).show();
        }

        @Override
        public boolean onClose() {
            if(!TextUtils.isEmpty(mSearchView.getQuery())){
                mSearchView.setQuery(null, true);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String s) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            String newFilter = !TextUtils.isEmpty(s) ? s:null;

            if(mCurFilter == null && newFilter == null){
                return true;
            }
            if(mCurFilter != null && mCurFilter.equals(newFilter)){
                return true;
            }
            mCurFilter = newFilter;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }
    }
}
