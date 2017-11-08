package com.loften.android.api.app;

import android.Manifest;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loften.android.api.R;

public class QuickContactsDemo extends ListActivity {

    public static final int MY_PERMISSIONS_REQUEST_CONTACTS = 1001;

    static final String[] CONTACTS_SUMMARY_PROJECTION = new String[]{
            Contacts._ID, // 0
            Contacts.DISPLAY_NAME, // 1
            Contacts.STARRED, // 2
            Contacts.TIMES_CONTACTED, // 3
            Contacts.CONTACT_PRESENCE, // 4
            Contacts.PHOTO_ID, // 5
            Contacts.LOOKUP_KEY, // 6
            Contacts.HAS_PHONE_NUMBER, // 7
    };

    static final int SUMMARY_ID_COLUMN_INDEX = 0;
    static final int SUMMARY_NAME_COLUMN_INDEX = 1;
    static final int SUMMARY_STARRED_COLUMN_INDEX = 2;
    static final int SUMMARY_TIMES_CONTACTED_COLUMN_INDEX = 3;
    static final int SUMMARY_PRESENCE_STATUS_COLUMN_INDEX = 4;
    static final int SUMMARY_PHOTO_ID_COLUMN_INDEX = 5;
    static final int SUMMARY_LOOKUP_KEY = 6;
    static final int SUMMARY_HAS_PHONE_COLUMN_INDEX = 7;
    /**
     * CursorLoader可以当作一个cursor装载器,管理好你的cursor:
     * (a)对数据库进行监听，在数据变化时更新你的cursor,
     * (b)加载新cursor时，要关闭旧的cursor
     */
    private CursorLoader cursorLoader;
    private ContactListItemAdapter adapter;

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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(this, R.string.quick_contacts_text, Toast.LENGTH_SHORT).show();
    }

    private void initCursorLoader() {
        adapter = new ContactListItemAdapter(this, R.layout.quick_contacts, null);
        final String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND ("
                + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                cursorLoader = new CursorLoader(QuickContactsDemo.this, Contacts.CONTENT_URI,
                        CONTACTS_SUMMARY_PROJECTION, select,
                        null, Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
                return cursorLoader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                //自动完成所有数据填充功能
                adapter.swapCursor(cursor);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                adapter.swapCursor(null);
            }
        });
        setListAdapter(adapter);
    }

    private final class ContactListItemAdapter extends ResourceCursorAdapter {

        public ContactListItemAdapter(Context context, int layout, Cursor c) {
            super(context, layout, c);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ContactListItemCache cache = (ContactListItemCache) view.getTag();

            cursor.copyStringToBuffer(SUMMARY_NAME_COLUMN_INDEX, cache.nameBuffer);
            int size = cache.nameBuffer.sizeCopied;
            cache.nameView.setText(cache.nameBuffer.data, 0, size);
            final long contactId = cursor.getLong(SUMMARY_ID_COLUMN_INDEX);
            final String lookupKey = cursor.getString(SUMMARY_LOOKUP_KEY);
            cache.photoView.assignContactUri(Contacts.getLookupUri(contactId, lookupKey));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = super.newView(context, cursor, parent);
            ContactListItemCache cache = new ContactListItemCache();
            cache.nameView = (TextView) view.findViewById(R.id.name);
            cache.photoView = (QuickContactBadge) view.findViewById(R.id.badge);
            view.setTag(cache);
            return view;
        }
    }

    final static class ContactListItemCache {
        public TextView nameView;
        public QuickContactBadge photoView;
        public CharArrayBuffer nameBuffer = new CharArrayBuffer(128);
    }
}
