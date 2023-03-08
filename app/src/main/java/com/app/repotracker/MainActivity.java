package com.app.repotracker;

import androidx.annotation.NonNull;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

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
    private static final String SEARCH_QUERY_KEY = "urlQuery";
    private TextView infoMessage;
    private ProgressBar loadingSymbol;
    private RecyclerView recyclerView;
    private String searchQuery;
    final static String PARAM_QUERY = "q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        infoMessage = (TextView) findViewById(R.id.info);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Get previous search made from any saved instance state
        if (savedInstanceState != null) {
            String query = savedInstanceState.getString(SEARCH_QUERY_KEY);
            if (!TextUtils.isEmpty(query))
                searchGithubRepos(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        if (!TextUtils.isEmpty(searchQuery))
            search.setQuery(searchQuery, false);
        // Add listener to search bar
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
     * Make a search on github from the text in the searchBox through an AsyncTaskLoader.
     */
    public void searchGithubRepos(String query) {
        // If search is empty, display empty search message
        if (TextUtils.isEmpty(query)) {
            infoMessage.setText(R.string.empty_search);
            return;
        }

        searchQuery = query;
        URL githubSearchUrl = Network.buildUrl("/search/repositories", PARAM_QUERY, query);
        performSearch(githubSearchUrl);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // Hide loading symbol and info message
        loadingSymbol.setVisibility(View.INVISIBLE);

        // Show error message if no data, or show results
        if (data == null) {
            recyclerView.setVisibility(View.INVISIBLE);
            infoMessage.setText(R.string.error_message);
            infoMessage.setVisibility(View.VISIBLE);
        } else {
            loadData(data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save any search query made
        outState.putString(SEARCH_QUERY_KEY, searchQuery);
    }
}