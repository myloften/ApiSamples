package com.loften.android.api.app;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.loften.android.api.R;

/**
 * 本例采用屏幕顶部Search Dialog的方式。在这种方式下，Android操作系统接管所有Search Dialog的事件，当用户提交查询后，
 * Android系统将给支持的用来处理查询的Activity发送消息。Search Dialog可以提供查询提示列表来匹配用户输入。
 * 用户提交查询后，Android系统构造一个Intent并把用户的查询内容放在这个Intent中。然后Android启动你定义的用来处理用户
 * 查询的Activity（“Searchable Activity”)，并把这个Intent发给该Activity。为了能够使用Android系统提供的Search Framework.
 * 需要以下几步：
 *
 * 1. Creating a Searchable Configuration
 * 首先定义一个Searchable configuration,用于描述Search Dialog 的一些属性，该描述文件按惯例通常命名为searchable.xml 并定义在/res/xml 目录下。
 *
 * 2. Creating a Searchable Activity (本例中 SearchQueryResults 定义为“Searchable Activity”)
 * 一个”Searchable Activity”就是一个可以用来处理Search Query 的Activity。和一般的Activity没有太大分别。
 * 当用户提交查询后，Android会给这个“Searchable Activity”发送一个Intent包含有用户查询内容，同时这个Intent 含有ACTION_SEARCH action。
 * 由于可以在任何一个Activity中使用Search Dialog或是SearchView，Android需要知道哪个Activity是“Searchable Activity”，
 * 这就需要在AndroidManifest.xml中来定义“Searchable Activity”。
 *
 * 3. Using the Search Dialog
 * Search Dialog 先为屏幕上方的浮动窗口，缺省为不可见的。只有当调用onSearchRequested()或是用户按“Search”键时
 * （不是所有设备都有Search钮，在模拟器上可以用F5）Search Dialog才会显示。
 * 为了使用Search Dialog，我们在AndroidManifest.xml定义了Searchable Activity： SearchQueryResults。
 * 如果此时直接运行SearchQueryResults，在模拟器上按F5，将会在屏幕上方显示Search Dialog。
 * 如果现在Invoke Search （SearchInvoke）Activity也可以使用Search Dialog， 也需要在AndroidManifest.xml做些说明;
 *
 * 这时按下 onSearchRequest() 或是 “Search”键就显示Search Dialog，按查询键后，将会在SearchQueryResults显示用户输入的查询内容
 */
public class SearchInvoke extends ToolbarActivity implements View.OnClickListener {

    private Button btnStartSearch;
    private Spinner spinnerMenuMode;
    private EditText txtQueryPrefill;
    private EditText txtQueryAppdata;

    // 相对应的列表项在 samples/ApiDemos/res/values/arrays.xml
    final static int MENUMODE_SEARCH_KEY = 0;
    final static int MENUMODE_MENU_ITEM = 1;
    final static int MENUMODE_TYPE_TO_SEARCH = 2;
    final static int MENUMODE_DISABLED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_invoke);
        initView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.search_menuModes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMenuMode.setAdapter(adapter);
        spinnerMenuMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == MENUMODE_TYPE_TO_SEARCH){
                    //在activity中按键会打开本地搜索
                    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
                }else{
                    //在activity中按键的不做响应
                    setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setDefaultKeyMode(DEFAULT_KEYS_DISABLE);
            }
        });
    }

    private void initView() {
        btnStartSearch = (Button) findViewById(R.id.btn_start_search);
        spinnerMenuMode = (Spinner) findViewById(R.id.spinner_menu_mode);
        txtQueryPrefill = (EditText) findViewById(R.id.txt_query_prefill);
        txtQueryAppdata = (EditText) findViewById(R.id.txt_query_appdata);

        btnStartSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_search:
                onSearchRequested();
                break;
        }
    }

    /**
     * Called when your activity's options menu needs to be updated.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item;

        // first, get rid of our menus (if any)
        menu.removeItem(0);
        menu.removeItem(1);

        // next, add back item(s) based on current menu mode
        switch (spinnerMenuMode.getSelectedItemPosition())
        {
            case MENUMODE_SEARCH_KEY:
                item = menu.add( 0, 0, 0, "(Search Key)");
                break;

            case MENUMODE_MENU_ITEM:
                item = menu.add( 0, 0, 0, "Search");
                item.setAlphabeticShortcut(SearchManager.MENU_KEY);
                break;

            case MENUMODE_TYPE_TO_SEARCH:
                item = menu.add( 0, 0, 0, "(Type-To-Search)");
                break;

            case MENUMODE_DISABLED:
                item = menu.add( 0, 0, 0, "(Disabled)");
                break;
        }

        item = menu.add(0, 1, 0, "Clear History");
        return true;
    }

    /** Handle the menu item selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                switch (spinnerMenuMode.getSelectedItemPosition()) {
                    case MENUMODE_SEARCH_KEY:
                        new AlertDialog.Builder(this)
                                .setMessage("To invoke search, dismiss this dialog and press the search key" +
                                        " (F5 on the simulator).")
                                .setPositiveButton("OK", null)
                                .show();
                        break;

                    case MENUMODE_MENU_ITEM:
                        onSearchRequested();
                        break;

                    case MENUMODE_TYPE_TO_SEARCH:
                        new AlertDialog.Builder(this)
                                .setMessage("To invoke search, dismiss this dialog and start typing.")
                                .setPositiveButton("OK", null)
                                .show();
                        break;

                    case MENUMODE_DISABLED:
                        new AlertDialog.Builder(this)
                                .setMessage("You have disabled search.")
                                .setPositiveButton("OK", null)
                                .show();
                        break;
                }
                break;
            case 1:
                clearSearchHistory();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        if(spinnerMenuMode.getSelectedItemPosition() == MENUMODE_DISABLED){
            return false;
        }

        final String queryPrefill = txtQueryPrefill.getText().toString();

        Bundle appDataBundle = null;
        final String queryAppDataString = txtQueryAppdata.getText().toString();
        if (queryAppDataString != null) {
            appDataBundle = new Bundle();
            appDataBundle.putString("demo_key", queryAppDataString);
        }

        startSearch(queryPrefill, false, appDataBundle, false);

        return true;
    }

    private void clearSearchHistory(){
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE);
        suggestions.clearHistory();
    }
}
