package com.loften.android.api.app;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.loften.android.api.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentNestingTabs extends ToolbarActivity {

    private TabLayout tabs;
    private ViewPager viewpager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_nesting_tabs);
        initView();
        setupViewPager(viewpager);
        tabs.setupWithViewPager(viewpager);
    }

    private void initView() {
        tabs = (TabLayout) findViewById(R.id.tabs);
        viewpager = (ViewPager) findViewById(R.id.viewpager);
    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentMenuFragment(), "menus");
        adapter.addFragment(new FragmentArgumentsFragment(), "args");
        adapter.addFragment(new FragmentHideShowFragment(), "show");
        adapter.addFragment(new FragmentTabsFragment(), "tabs");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitileList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title){
            mFragmentList.add(fragment);
            mFragmentTitileList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitileList.get(position);
        }
    }
}
