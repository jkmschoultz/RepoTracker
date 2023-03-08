package com.app.repotracker;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.app.repotracker.utilities.Network;

import java.net.URL;

public class MainActivity extends BaseActivity {
    private TextView infoMessage;
    private ProgressBar loadingSymbol;
    private String searchQuery;
    private static final String PARAM_QUERY = "q";
    private static final String SEARCH_QUERY_KEY = "urlQuery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Use BaseActivity's onCreate() method
        super.onCreate(savedInstanceState);

        infoMessage = (TextView) findViewById(R.id.info);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);

        // Get any previous search made from a saved instance state
        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(SEARCH_QUERY_KEY);
            if (!TextUtils.isEmpty(query))
                searchGithubRepos(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Use BaseActivity's onCreateOptionsMenu() method
        super.onCreateOptionsMenu(menu);

        // Get any previous search that has been saved
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        if (!TextUtils.isEmpty(searchQuery))
            search.setQuery(searchQuery, false);

        // Add listener to SearchView
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Search github public repos for user's submitted query
                search.clearFocus();
                searchGithubRepos(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    /**
     * Make a search on github from a provided query through an AsyncTaskLoader.
     *
     * @param query The query to search GitHub repositories for
     */
    public void searchGithubRepos(String query) {
        // If search is empty, display empty search message
        if (TextUtils.isEmpty(query)) {
            infoMessage.setText(R.string.empty_search);
            return;
        }

        // Build URL from provided query and perform search
        searchQuery = query;
        URL githubSearchUrl = Network.buildUrl("/search/repositories", PARAM_QUERY, query);
        performSearch(githubSearchUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // Override onLoadFinished() inherited from BaseActivity
        // Hide loading symbol and info message
        loadingSymbol.setVisibility(View.INVISIBLE);
        infoMessage.setVisibility(View.INVISIBLE);

        // Show error message if no data, or show results
        if (data == null) {
            showError();
        } else {
            loadData(data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save latest search query made to instance state
        outState.putString(SEARCH_QUERY_KEY, searchQuery);
    }
}