package com.loften.android.api.app;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.loften.android.api.R;

public class SearchQueryResults extends AppCompatActivity {

    private TextView txtQuery;
    private TextView txtAppdata;
    private TextView txtDeliveredby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_query_results);
        initView();

        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if(Intent.ACTION_SEARCH.equals(queryAction)){
            doSearchQuery(queryIntent, "onCreate()");
        }else {
            txtDeliveredby.setText("onCreate(), but no ACTION_SEARCH intent");
        }
    }

    /**
     * Called when new intent is delivered.
     *
     * This is where we check the incoming intent for a query string.
     *
     * @param newIntent The intent used to restart this activity
     */
    @Override
    public void onNewIntent(final Intent newIntent) {
        super.onNewIntent(newIntent);

        // get and process search query here
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            doSearchQuery(queryIntent, "onNewIntent()");
        }
        else {
            txtDeliveredby.setText("onNewIntent(), but no ACTION_SEARCH intent");
        }
    }

    /**
     * Generic search handler.
     *
     * In a "real" application, you would use the query string to select results from
     * your data source, and present a list of those results to the user.
     */
    private void doSearchQuery(final Intent queryIntent, final String entryPoint) {

        // The search query is provided as an "extra" string in the query intent
        final String queryString = queryIntent.getStringExtra(SearchManager.QUERY);
        txtQuery.setText(queryString);

        // Record the query string in the recent queries suggestions provider.
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SearchSuggestionSampleProvider.AUTHORITY, SearchSuggestionSampleProvider.MODE);
        suggestions.saveRecentQuery(queryString, null);

        // If your application provides context data for its searches,
        // you will receive it as an "extra" bundle in the query intent.
        // The bundle can contain any number of elements, using any number of keys;
        // For this Api Demo we're just using a single string, stored using "demo key".
        final Bundle appData = queryIntent.getBundleExtra(SearchManager.APP_DATA);
        if (appData == null) {
            txtAppdata.setText("<no app data bundle>");
        }
        if (appData != null) {
            String testStr = appData.getString("demo_key");
            txtAppdata.setText((testStr == null) ? "<no app data>" : testStr);
        }

        // Report the method by which we were called.
        txtDeliveredby.setText(entryPoint);
    }

    private void initView() {
        txtQuery = (TextView) findViewById(R.id.txt_query);
        txtAppdata = (TextView) findViewById(R.id.txt_appdata);
        txtDeliveredby = (TextView) findViewById(R.id.txt_deliveredby);
    }
}
