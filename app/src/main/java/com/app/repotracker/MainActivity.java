package com.app.repotracker;

import androidx.appcompat.app.AppCompatActivity;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>, Adapter.ListItemClickListener {

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final int GITHUB_SEARCH_LOADER = 22;
    private TextView url;
    private TextView searchResults;
    private TextView errorMessage;
    private ProgressBar loadingSymbol;
    private SearchView search;

    private static final int NUM_LIST_ITEMS = 5;

    /*
     * References to RecyclerView and Adapter to reset the list to its
     * "pretty" state when the reset menu item is clicked.
     */
    private Adapter adapter;
    private RecyclerView recyclerView;
    // Create a Toast variable called mToast to store the current Toast
    /*
     * If we hold a reference to our Toast, we can cancel it (if it's showing)
     * to display a new Toast. If we didn't do this, Toasts would be delayed
     * in showing up if you clicked many list items in quick succession.
     */
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        url = (TextView) findViewById(R.id.url);
        searchResults = (TextView) findViewById(R.id.searchResults);
        errorMessage = (TextView) findViewById(R.id.error_message);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        /*
         * A LinearLayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView into a linear list. This means that it can produce either a horizontal or
         * vertical list depending on which parameter you pass in to the LinearLayoutManager
         * constructor. By default, if you don't specify an orientation, you get a vertical list.
         * In our case, we want a vertical list, so we don't need to pass in an orientation flag to
         * the LinearLayoutManager constructor.
         *
         * There are other LayoutManagers available to display your data in uniform grids,
         * staggered grids, and more! See the developer documentation for more details.
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        recyclerView.setHasFixedSize(true);
        //  Pass in this as the ListItemClickListener to the GreenAdapter constructor
        /*
         * The GreenAdapter is responsible for displaying each item in the list.
         */
        adapter = new Adapter(NUM_LIST_ITEMS, this);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            String queryUrl = savedInstanceState.getString(SEARCH_QUERY_URL_EXTRA);
            url.setText(queryUrl);
        }

        // Initialize loader
        LoaderManager.getInstance(this).initLoader(GITHUB_SEARCH_LOADER, null, this);
    }

    /**
     * Make a search on github from the text in the searchBox through an AsyncTaskLoader.
     */
    private void makeGithubSearchQuery() {
        String githubQuery = search.getQuery().toString();

        // If search is empty, display empty search message
        if (TextUtils.isEmpty(githubQuery)) {
            url.setText(R.string.empty_search);
            return;
        }

        URL githubSearchUrl = NetworkUtils.buildUrl(githubQuery);
        url.setText(githubSearchUrl.toString());

        // Save search URL to bundle
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_URL_EXTRA, githubSearchUrl.toString());

        // Get existing loader or create new loader from loader manager
        LoaderManager loaderManager = LoaderManager.getInstance(this);
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, bundle, this);
        } else {
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER, bundle, this);
        }
    }

    /**
     * Make search results visible (hides error message if visible)
     */
    private void showResult() {
        errorMessage.setVisibility(View.INVISIBLE);
        searchResults.setVisibility(View.VISIBLE);
    }

    /**
     * Make error message visible (and hide search results if visible)
     */
    private void showError() {
        searchResults.setVisibility(View.INVISIBLE);
        errorMessage.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {

                if (args == null) {
                    return;
                }
                // Display loading symbol
                loadingSymbol.setVisibility(View.VISIBLE);
                // Force a load
                forceLoad();
            }

            @Override
            public String loadInBackground() {

                // Get URL to search from bundle as string
                String searchQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);

                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }

                // Make URL request and return response
                try {
                    URL githubUrl = new URL(searchQueryUrlString);
                    return NetworkUtils.getResponseFromHttpUrl(githubUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        // Hide loading symbol
        loadingSymbol.setVisibility(View.INVISIBLE);

        // Show error message if no data, or show results
        if (data == null) {
            showError();
        } else {
            searchResults.setText(data);
            showResult();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Must be overridden
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        search = (SearchView) menu.findItem(R.id.search).getActionView();
        // Add listener to search button
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Perform action on click
                makeGithubSearchQuery();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search:

            case R.id.set_user:
                // Change to Set User Activity
                Context context = MainActivity.this;
                Class activity = SetUserActivity.class;
                Intent intent = new Intent(context, activity);
                startActivity(intent);
                return true;

            case R.id.reset:
                // Pass in this as the ListItemClickListener to the GreenAdapter constructor
                adapter = new Adapter(NUM_LIST_ITEMS, this);
                recyclerView.setAdapter(adapter);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        // In the beginning of the method, cancel the Toast if it isn't null
        /*
         * Even if a Toast isn't showing, it's okay to cancel it. Doing so
         * ensures that our new Toast will show immediately, rather than
         * being delayed while other pending Toasts are shown.
         *
         * Comment out these three lines, run the app, and click on a bunch of
         * different items if you're not sure what I'm talking about.
         */
        if (mToast != null) {
            mToast.cancel();
        }

        // Show a Toast when an item is clicked, displaying that item number that was clicked
        /*
         * Create a Toast and store it in our Toast field.
         * The Toast that shows up will have a message similar to the following:
         *
         *                     Item #42 clicked.
         */
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        mToast.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save search URL
        String queryUrl = url.getText().toString();
        outState.putString(SEARCH_QUERY_URL_EXTRA, queryUrl);

    }
}