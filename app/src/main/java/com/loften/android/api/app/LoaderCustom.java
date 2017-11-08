package com.loften.android.api.app;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.loften.android.api.R;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LoaderCustom extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getFragmentManager();

        if(fm.findFragmentById(android.R.id.content) == null){
            AppListFragment list = new AppListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }

    }

    /**
     * 安装的app信息实体类
     */
    public static class AppEntry{

        private final AppListLoader mLoader;
        private final ApplicationInfo mInfo;//<application>节点信息，只有一个
        private final File mApkFile;
        private String mLabel;//应用文字标签
        private Drawable mIcon;//应用图标
        private boolean mMounted;

        public AppEntry(AppListLoader loader, ApplicationInfo info){
            mLoader = loader;
            mInfo = info;
            mApkFile = new File(info.sourceDir);
        }

        public ApplicationInfo getApplicationInfo(){
            return mInfo;
        }

        public String getLabel(){
            return mLabel;
        }

        public Drawable getIcon(){
            if(mIcon == null){
                if(mApkFile.exists()){
                    //获取应用图标
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                }else{
                    mMounted = false;
                }
            } else if(!mMounted){
                if(mApkFile.exists()){
                    mMounted = true;
                    mIcon = mInfo.loadIcon(mLoader.mPm);
                    return mIcon;
                }
            } else {
                return mIcon;
            }

            return mLoader.getContext().getResources().getDrawable(android.R.drawable.sym_def_app_icon);
        }

        @Override
        public String toString() {
            return mLabel;
        }

        /**
         * 获取程序名称
         */
        void loadLabel(Context context){
            if(mLabel == null || !mMounted){
                if(!mApkFile.exists()){
                    mMounted = false;
                    mLabel = mInfo.packageName;
                }else{
                    mMounted = true;
                    CharSequence label = mInfo.loadLabel(context.getPackageManager());
                    mLabel = label != null ? label.toString():mInfo.packageName;
                }
            }
        }
    }

    /**
     * * 在Android3.0中引入了AsyncTaskLoader装载器功能类，这使它很容易在Activity或Fragment中使用异步的方式加载数据。
     * 装载器的特点如下：
     *
     * 1. 装载器对于每个Activity和Fagment都是有效的；
     *
     * 2. 装载器提供异步数据加载的能力；
     *
     * 3. 装载器监视数据资源并且当内容改变时发送新的结果；
     *
     * 4. 在配置改变后重建的时候，装载器自动的重连最后的装载器游标，因此，不需要重新查询数据。
     *
     */
    public static class AppListLoader extends AsyncTaskLoader<List<AppEntry>> {

        final InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
        final PackageManager mPm;//包管理器

        List<AppEntry> mApps;
        PackageIntentReceiver mPackageObserver;

        public AppListLoader(Context context){
            super(context);

            mPm = getContext().getPackageManager();
        }

        /**
         * 这是Loader的核心方法，必须重载，后台运行。在这里头实现加载数据的功能
         */
        @Override
        public List<AppEntry> loadInBackground() {
            // GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
            // GET_DISABLED_COMPONENTS获取所有组件
            List<ApplicationInfo> apps = mPm.getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                            PackageManager.GET_DISABLED_COMPONENTS);
            if(apps == null){
                apps = new ArrayList<>();
            }

            final Context context = getContext();

            List<AppEntry> entries = new ArrayList<>(apps.size());
            for(int i=0; i<apps.size(); i++){
                AppEntry entry = new AppEntry(this, apps.get(i));
                entry.loadLabel(context);
                entries.add(entry);
            }

            Collections.sort(entries, ALPHA_COMPARATOR);
            return entries;
        }

        /**
         * 将结果传递给已注册的监听器们。
         */
        @Override
        public void deliverResult(List<AppEntry> apps) {
            if (isReset()) {
                if (apps != null) {
                    onReleaseResources(apps);
                }
            }
            List<AppEntry> oldApps = mApps;
            mApps = apps;

            if (isStarted()) {
                super.deliverResult(apps);
            }

            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /**
         * 开始Loader数据
         */
        @Override
        protected void onStartLoading() {
            if (mApps != null) {
                // 一个结果，立即传递结果
                deliverResult(mApps);
            }

            //通过广播监听数据改变
            if (mPackageObserver == null) {
                mPackageObserver = new PackageIntentReceiver(this);
            }

            boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

            if (takeContentChanged() || mApps == null || configChange) {
                forceLoad();
            }
        }

        /**
         * 停止Loader数据
         */
        @Override
        protected void onStopLoading() {
            // 取消当前的加载任务
            cancelLoad();
        }

        @Override
        public void onCanceled(List<AppEntry> data) {
            super.onCanceled(data);
            onReleaseResources(data);
        }

        @Override
        protected void onReset() {
            super.onReset();
            // 停止加载
            onStopLoading();
            // 释放资源
            if (mApps != null) {
                onReleaseResources(mApps);
                mApps = null;
            }

            if (mPackageObserver != null) {
                getContext().unregisterReceiver(mPackageObserver);
                mPackageObserver = null;
            }
        }

        protected void onReleaseResources(List<AppEntry> apps){

        }
    }

    /**
     * 排序规则
     */
    public static final Comparator<AppEntry> ALPHA_COMPARATOR = new Comparator<AppEntry>() {
        private final Collator sCollator = Collator.getInstance();
        @Override
        public int compare(AppEntry object1, AppEntry object2) {
            return sCollator.compare(object1.getLabel(), object2.getLabel());
        }
    };


    public static class InterestingConfigChanges{
        final Configuration mLastConfiguration = new Configuration();
        int mLastDensity;

        boolean applyNewConfig(Resources res){
            int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
            boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
            if(densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0){
                mLastDensity = res.getDisplayMetrics().densityDpi;
                return true;
            }
            return false;
        }
    }

    public static class PackageIntentReceiver extends BroadcastReceiver{

        final AppListLoader mLoader;

        public PackageIntentReceiver(AppListLoader loader){
            mLoader = loader;
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mLoader.getContext().registerReceiver(this, filter);

            IntentFilter sdFilter = new IntentFilter();
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
            sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
            mLoader.getContext().registerReceiver(this, sdFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mLoader.onContentChanged();
        }
    }

    public static class AppListFragment extends ListFragment
            implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
            LoaderManager.LoaderCallbacks<List<AppEntry>> {

        AppListAdapter mAdapter;
        SearchView mSearchView;
        String mCurFilter;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            setEmptyText("No applications");

            setHasOptionsMenu(true);

            mAdapter = new AppListAdapter(getActivity());
            setListAdapter(mAdapter);

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

        @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Place an action bar item for searching.
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

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            Toast.makeText(getActivity(), "Item clicked: " + id, Toast.LENGTH_SHORT).show();
        }

        @Override
        public Loader<List<AppEntry>> onCreateLoader(int i, Bundle bundle) {
            return new AppListLoader(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<AppEntry>> loader, List<AppEntry> data) {
            mAdapter.setData(data);

            // The list should now be shown.
            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<AppEntry>> loader) {
            mAdapter.setData(null);
        }

        @Override
        public boolean onClose() {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
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
            mCurFilter = !TextUtils.isEmpty(s) ? s : null;
            mAdapter.getFilter().filter(mCurFilter);
            return true;
        }
    }

    public static class AppListAdapter extends ArrayAdapter<AppEntry>{
        private final LayoutInflater mInflater;

        public AppListAdapter(Context context){
            super(context, android.R.layout.simple_list_item_2);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<AppEntry> data){
            clear();
            if(data != null){
                addAll(data);
            }
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view;
            if(convertView == null){
                view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
            }else{
                view = convertView;
            }

            AppEntry item = getItem(position);
            ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            ((TextView)view.findViewById(R.id.text)).setText(item.getLabel());

            return view;
        }
    }
}
