package com.loften.android.api.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.loften.android.api.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTabsFragment extends Fragment {
    TabManager mTabManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTabManager = new TabManager(getActivity(), getChildFragmentManager(),
                R.id.realtabcontent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fragment_tabs, container, false);

        TabHost host = mTabManager.handleCreateView(v);

        mTabManager.addTab(host.newTabSpec("result").setIndicator("Result"),
                FragmentMenuFragment.class, null);
        mTabManager.addTab(host.newTabSpec("contacts").setIndicator("Contacts"),
                FragmentHideShowFragment.class, null);
        mTabManager.addTab(host.newTabSpec("apps").setIndicator("Apps"),
                FragmentArgumentsFragment.class, null);

        return v;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mTabManager.handleViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabManager.handleDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mTabManager.handleSaveInstanceState(outState);
    }

    public static class TabManager implements TabHost.OnTabChangeListener{
        private final Context mContext;
        private final FragmentManager mManager;
        private final int mContainerId;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
        private TabHost mTabHost;
        private TabInfo mLastTab;
        private boolean mInitialized;
        private String mCurrentTabTag;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(Context context, FragmentManager manager, int containerId) {
            mContext = context;
            mManager = manager;
            mContainerId = containerId;
        }

        public TabHost handleCreateView(View root) {
            if (mTabHost != null) {
                throw new IllegalStateException("TabHost already set");
            }
            mTabHost = (TabHost)root.findViewById(android.R.id.tabhost);
            mTabHost.setup();
            mTabHost.setOnTabChangedListener(this);
            return mTabHost;
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mContext));
            String tag = tabSpec.getTag();
            TabInfo info = new TabInfo(tag, clss, args);
            mTabs.add(info);
            mTabHost.addTab(tabSpec);
        }

        public void handleViewStateRestored(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                mCurrentTabTag = savedInstanceState.getString("tab");
            }
            mTabHost.setCurrentTabByTag(mCurrentTabTag);

            String currentTab = mTabHost.getCurrentTabTag();

            // Go through all tabs and make sure their fragments match
            // the correct state.
            FragmentTransaction ft = null;
            for (int i=0; i<mTabs.size(); i++) {
                TabInfo tab = mTabs.get(i);
                tab.fragment = mManager.findFragmentByTag(tab.tag);
                if (tab.fragment != null && !tab.fragment.isDetached()) {
                    if (tab.tag.equals(currentTab)) {
                        // The fragment for this tab is already there and
                        // active, and it is what we really want to have
                        // as the current tab.  Nothing to do.
                        mLastTab = tab;
                    } else {
                        // This fragment was restored in the active state,
                        // but is not the current tab.  Deactivate it.
                        if (ft == null) {
                            ft = mManager.beginTransaction();
                        }
                        ft.detach(tab.fragment);
                    }
                }
            }

            // We are now ready to go.  Make sure we are switched to the
            // correct tab.
            mInitialized = true;
            ft = doTabChanged(currentTab, ft);
            if (ft != null) {
                ft.commit();
                mManager.executePendingTransactions();
            }
        }

        public void handleDestroyView() {
            mCurrentTabTag = mTabHost.getCurrentTabTag();
            mTabHost = null;
            mTabs.clear();
            mInitialized = false;
        }

        public void handleSaveInstanceState(Bundle outState) {
            outState.putString("tab", mTabHost != null
                    ? mTabHost.getCurrentTabTag() : mCurrentTabTag);
        }

        @Override
        public void onTabChanged(String tabId) {
            if (!mInitialized) {
                return;
            }
            FragmentTransaction ft = doTabChanged(tabId, null);
            if (ft != null) {
                ft.commit();
            }
        }

        private FragmentTransaction doTabChanged(String tabId, FragmentTransaction ft) {
            TabInfo newTab = null;
            for (int i=0; i<mTabs.size(); i++) {
                TabInfo tab = mTabs.get(i);
                if (tab.tag.equals(tabId)) {
                    newTab = tab;
                }
            }
            if (newTab == null) {
                throw new IllegalStateException("No tab known for tag " + tabId);
            }
            if (mLastTab != newTab) {
                if (ft == null) {
                    ft = mManager.beginTransaction();
                }
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mContext,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }

                mLastTab = newTab;
            }
            return ft;
        }
    }

}
