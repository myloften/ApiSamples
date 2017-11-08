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
 *  在查询Content Provider发布的SQLite的数据时，常规的基于Content Resolver和Cursor的查询是与Activity/Fragment
 *  的View绘制同步的。因此，较大的数据量的数据库操作会影响用户体验。下面介绍一种异步的数据库操作方法——实现
 *  LoaderManager.LoaderCallbacks<Cursor>接口。
 *  LoaderManager是java.lang.Object的直接子类，LoaderCallbacks<D>是其内部接口。在Activity中，可以直接
 *  使用XxxActivity.this.getLoaderManager()来得到一个LoaderManager实例，以此调用
 *  initLoader(int id, Bundle args, LoaderCallbacks<D> callback)来初始化一个基于LoaderManager的异步查询操作。
 */
public class LoaderCursor extends AppCompatActivity {

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

    /**
     * 在Android 3.0中提供了一个新概念Loaders，通过LoaderManager类可以很轻松的异步加载数据从Fragment或Activity
     * 中，Loaders提供了回调机制通知最终的运行结果，有点类似AsyncTask类，但由于Loader对于并发可以用过Loader管理器统
     * 一管理，所以更适合批量处理多个异步任务的处理(当然内部仍然是多线程)。
     *
     * 异步的数据库操作
     */
    public static class CursorLoaderListFragment extends ListFragment
            implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
            LoaderManager.LoaderCallbacks<Cursor> {

        SimpleCursorAdapter mAdapter;
        SearchView mSearchView;
        String mCurFilter;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setEmptyText("没有联系人");

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
            mSearchView = new MySearchView(getActivity());
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

        /**
         * 创建一个可查询ContentProvider的loader
         * LoaderManager将会在它第一次创建Loader的时候调用该方法。
         * 只会调用一次
         */
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

        /**
         * loader完成查询时调用，通常用于在查询到的cursor中提取数据
         * 每次数据源都有更新的时候，就会回调这个方法，然后update 我们的ui了
         */
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

        /**
         * 移除不再有用的数据
         */
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
