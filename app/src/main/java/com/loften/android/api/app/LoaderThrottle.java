package com.loften.android.api.app;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.HashMap;

public class LoaderThrottle extends AppCompatActivity {

    // 定义主机名，用以拼接Uri，Uri表明了内容提供的地址，外部应用通过Uri访问内容提供者，来实现对数据的增删改查
    public static final String AUTHORITY = "com.loften.android.api.app.LoaderThrottle";

    /**
     * 本例中我们将自定义的一个数据库通过内容提供者共享给其它的外部应用，使外部应用可以对数据库中的内容进行 增删改查的操作。
     * 定义一个类用于定义关于内容提供者和工作表的一些常量，该类实现了BaseColumns接口，表示将继承该接口
     * _id和_icount两列，我们无需再定义就会在表中创建出这两个列
     */
    public static final class MainTable implements BaseColumns {
        // 构造函数私有化，不允许创建此类的实例
        private MainTable() {
        }
        // 定义表的名字
        public static final String TABLE_NAME = "main";
        // 定义这张表的uri
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/main");
        // 定义表中某个条目（某行数据）的Uri的前面公共的部分，使用时我们还需要在后面添加条目的Id
        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse("content://" + AUTHORITY + "/main/");
        // 定义Uri的命名机制，/前面的部分是android系统定义的，不能改变，/后面的部分可以自定义任意字符串
        public static final String CONTENT_TYPE
                = "vnd.android.cursor.dir/vnd.example.api-demos-throttle";
        // 定义某个条目的Uri的命名机制
        public static final String CONTENT_ITEM_TYPE
                = "vnd.android.cursor.item/vnd.example.api-demos-throttle";
        // 定义默认的排序方式
        public static final String DEFAULT_SORT_ORDER = "data COLLATE LOCALIZED ASC";
        // 定义列的名字，本例中只有一列
        public static final String COLUMN_NAME_DATA = "data";
    }
    /*
     * 定义数据库的帮助库，用于创建数据库和工作表
     */
    static class DatabaseHelper extends SQLiteOpenHelper{
        // 定义数据库的名字的版本
        private static final String DATABASE_NAME = "loader_throttle.db";
        private static final int DATABASE_VERSION = 2;

        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + MainTable.TABLE_NAME + " ("
                    + MainTable._ID + " INTEGER PRIMARY KEY,"
                    + MainTable.COLUMN_NAME_DATA + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 版本升级时调用，删除旧表，创建新表
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * 自定义内容提供者，通过它实现把数据库共享给外部应用
     */
    public static class SimpleProvider extends ContentProvider {
        // 定义一个集合，把从数据库中选出的列映射到该集合中
        private final HashMap<String, String> mNotesProjectionMap;
        // 定义Uri的匹配器，用于解析传入的Uri
        private final UriMatcher mUriMatcher;
        // 定义当Uri匹配时的返回码
        // 匹配整个表时的返回码
        private static final int MAIN = 1;
        // 匹配某一行时的返回码
        private static final int MAIN_ID = 2;

        private DatabaseHelper mOpenHelper;

        public SimpleProvider(){
            // 为内容提供者注册Uri
            mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME, MAIN);
            // #表示通配符
            mUriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", MAIN_ID);


            mNotesProjectionMap = new HashMap<String, String>();
            mNotesProjectionMap.put(MainTable._ID, MainTable._ID);
            mNotesProjectionMap.put(MainTable.COLUMN_NAME_DATA, MainTable.COLUMN_NAME_DATA);
        }

        @Override
        public boolean onCreate() {
            // 创建数据库和数据表
            mOpenHelper = new DatabaseHelper(getContext());
            return true;
        }

        /**
         * 用于供外部应用从内容提供者中获取数据
         */
        @Nullable
        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                            String[] selectionArgs, String sortOrder) {
            // 使用Sql查询语句构建的辅助类
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(MainTable.TABLE_NAME);
            // 根据匹配Uri的返回码来判定是查询整个数据表，还是查询某条数据
            switch (mUriMatcher.match(uri)){
                case MAIN:
                    // 查询整个表
                    qb.setProjectionMap(mNotesProjectionMap);
                    break;
                case MAIN_ID:
                    qb.setProjectionMap(mNotesProjectionMap);
                    // 追加筛选条件
                    qb.appendWhere(MainTable._ID + "=?");
                    // 获取查询参数即所要查询条目的Id
                    selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs,
                            new String[] { uri.getLastPathSegment() });
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            // 如果没定义排序规则，则按照默认的规则进行排序
            if(TextUtils.isEmpty(sortOrder)){
                sortOrder = MainTable.DEFAULT_SORT_ORDER;
            }

            // 获取到可读的数据库
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            Cursor c = qb.query(db, projection, selection, selectionArgs,
                    null /* no group */, null /* no filter */, sortOrder);
            // 监听uri的变化
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }

        /**
         * 返回对应Uri MIME类型用以验证数据的合法性
         */
        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            switch (mUriMatcher.match(uri)){
                case MAIN:
                    return MainTable.CONTENT_TYPE;
                case MAIN_ID:
                    return MainTable.CONTENT_ITEM_TYPE;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        /**
         * 用于外部应用向内容提供者中插入数据
         */
        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues initialValues) {
            // 只能插入到数据表中
            if (mUriMatcher.match(uri) != MAIN) {
                throw new IllegalArgumentException("Unknown URI " + uri);
            }

            ContentValues values;

            if (initialValues != null) {
                values = new ContentValues(initialValues);
            } else {
                values = new ContentValues();
            }

            if (values.containsKey(MainTable.COLUMN_NAME_DATA) == false) {
                values.put(MainTable.COLUMN_NAME_DATA, "");
            }

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();

            long rowId = db.insert(MainTable.TABLE_NAME, null, values);
            // 如果插入成功，则插入行的Id存在
            if (rowId > 0) {
                // ContentUris用于操作Uri路径后面的Id部分
                Uri noteUri = ContentUris.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId);
                //必须设置对Uri的监听，不然loader无法获取到数据库的更新，不能实现实时更新
                getContext().getContentResolver().notifyChange(noteUri, null);
                // 返回所插入条目的Uri
                return noteUri;
            }

            throw new SQLException("Failed to insert row into " + uri);
        }

        /**
         * 用于外部应用删除内容提供者中的数据
         */
        @Override
        public int delete(@NonNull Uri uri, @Nullable String where, @Nullable String[] whereArgs) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            String finalWhere;

            int count;

            switch (mUriMatcher.match(uri)){
                case MAIN:
                    count = db.delete(MainTable.TABLE_NAME, where, whereArgs);
                    break;

                case MAIN_ID:
                    // 组装查询条件
                    finalWhere = DatabaseUtils.concatenateWhere(
                            MainTable._ID + " = " + ContentUris.parseId(uri), where);
                    count = db.delete(MainTable.TABLE_NAME, finalWhere, whereArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String where, @Nullable String[] whereArgs) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            int count;
            String finalWhere;

            switch (mUriMatcher.match(uri)) {
                case MAIN:
                    count = db.update(MainTable.TABLE_NAME, values, where, whereArgs);
                    break;

                case MAIN_ID:
                    // 组装查询条件
                    finalWhere = DatabaseUtils.concatenateWhere(
                            MainTable._ID + " = " + ContentUris.parseId(uri), where);
                    count = db.update(MainTable.TABLE_NAME, values, finalWhere, whereArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }

            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            ThrottledLoaderListFragment list = new ThrottledLoaderListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    public static class ThrottledLoaderListFragment extends ListFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        static final int POPULATE_ID = Menu.FIRST;
        static final int CLEAR_ID = Menu.FIRST + 1;

        SimpleCursorAdapter mAdapter;
        String mCurFilter;
        AsyncTask<Void, Void, Void> mPopulationgTask;


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setEmptyText("No data.  Select 'Populate' to fill with data from Z to A at a rate of 4 per second.");
            setHasOptionsMenu(true);

            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_1, null,
                    new String[]{MainTable.COLUMN_NAME_DATA},
                    new int[]{android.R.id.text1}, 0);
            setListAdapter(mAdapter);

            setListShown(false);

            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.add(Menu.NONE, POPULATE_ID, 0, "Populate")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(Menu.NONE, CLEAR_ID, 0, "Clear")
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final ContentResolver cr = getActivity().getContentResolver();

            switch (item.getItemId()) {
                case POPULATE_ID:
                    // 如果异步任务不为空，则取消任务
                    if (mPopulationgTask != null) {
                        mPopulationgTask.cancel(false);
                    }
                    // 开启线程向数据库中添加内容
                    mPopulationgTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            // 向数据库中添加内容
                            for (char c = 'Z'; c >= 'A'; c--) {
                                if (isCancelled()) {
                                    break;
                                }
                                StringBuilder builder = new StringBuilder("Data ");
                                builder.append(c);
                                ContentValues values = new ContentValues();
                                values.put(MainTable.COLUMN_NAME_DATA, builder.toString());
                                cr.insert(MainTable.CONTENT_URI, values);
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                            }
                            return null;
                        }
                    };
                    // 使用系统默认的线程池来管理线程
                    mPopulationgTask.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    return true;

                case CLEAR_ID:
                    if (mPopulationgTask != null) {
                        mPopulationgTask.cancel(false);
                        mPopulationgTask = null;
                    }
                    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            cr.delete(MainTable.CONTENT_URI, null, null);
                            return null;
                        }
                    };
                    task.execute((Void[]) null);
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            Toast.makeText(getActivity(), "Item clicked: "+id, Toast.LENGTH_SHORT).show();
        }

        static final String[] PROJECTION = new String[] {
                MainTable._ID,
                MainTable.COLUMN_NAME_DATA,
        };

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            CursorLoader cl = new CursorLoader(getActivity(), MainTable.CONTENT_URI,
                    PROJECTION, null, null, null);
            //最多每2秒更新一次
            cl.setUpdateThrottle(2000);
            return cl;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
    }
}
