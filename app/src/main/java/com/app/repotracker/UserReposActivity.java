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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.repotracker.utilities.Adapter;
import com.app.repotracker.utilities.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class UserReposActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<String>, Adapter.ListItemClickListener {

    private SearchView search;
    private TextView infoMessage;
    private ProgressBar loadingSymbol;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private Toast toast;
    private SharedPreferences sharedPreferences;
    private final String PREFERENCES = "Preferences";
    private static final String COMPLETE_URL_KEY = "completeUrl";
    private static final int GITHUB_SEARCH_LOADER = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_repos);
        sharedPreferences = getSharedPreferences(PREFERENCES, 0);

        infoMessage = (TextView) findViewById(R.id.user_info_message);
        loadingSymbol = (ProgressBar) findViewById(R.id.loading_symbol2);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view2);

        // Set layout of recycler view to be vertical list
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (sharedPreferences != null
                && sharedPreferences.contains("username")) {
            // Search GitHub for user's public repositories
            String username = sharedPreferences.getString("username", "");
            getUserRepos(username);
        }
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
                // TODO
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
        Context context = UserReposActivity.this;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.home:
                intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.user_repos:
                // Change to User Repos Activity
                intent = new Intent(context, UserReposActivity.class);
                startActivity(intent);
                return true;

            case R.id.set_user:
                // Change to Set User Activity
                intent = new Intent(context, SetUserActivity.class);
                startActivity(intent);
                return true;

            case R.id.reset:
                // Clear stored shared preferences and launch Set User Activity
                SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                sharedPreferencesEditor.clear();
                sharedPreferencesEditor.apply();
                intent = new Intent(context, SetUserActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getUserRepos(String username) {
        // If search is empty, display empty search message
        if (TextUtils.isEmpty(username)) {
            infoMessage.setText(R.string.empty_username);
            return;
        }

        URL githubSearchUrl = Network.buildUrl("/users/" + username + "/repos");

        // Save search URL to bundle
        Bundle bundle = new Bundle();
        bundle.putString(COMPLETE_URL_KEY, githubSearchUrl.toString());

        // Get existing loader or create new loader from loader manager
        LoaderManager loaderManager = LoaderManager.getInstance(this);
        Loader<String> githubSearchLoader = loaderManager.getLoader(GITHUB_SEARCH_LOADER);
        if (githubSearchLoader == null) {
            loaderManager.initLoader(GITHUB_SEARCH_LOADER, bundle, this);
        } else {
            loaderManager.restartLoader(GITHUB_SEARCH_LOADER, bundle, this);
        }
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
                String searchQueryUrlString = args.getString(COMPLETE_URL_KEY);

                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }

                // Make URL request and return response
                try {
                    URL githubUrl = new URL(searchQueryUrlString);
                    return Network.getResponseFromHttpUrl(githubUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
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
    public void onLoaderReset(Loader<String> loader) {
        // Must be overridden
    }

    /**
     * Parse received data into json and load data into RecyclerView
     *
     * @param data The data to parse into RecyclerView
     */
    public void loadData(String data) {
        try {
            String formattedData = "{\"items\": " + data + "}";
            JSONObject dataJson = new JSONObject(formattedData);
            System.out.println(dataJson.toString());
            int numItems = dataJson.getJSONArray("items").length();

            if (numItems < 1) {
                // Display message that result was empty
                recyclerView.setVisibility(View.INVISIBLE);
                infoMessage.setText(R.string.error_empty);
                infoMessage.setVisibility(View.VISIBLE);
            }
            else {
                // Adapter for displaying items in recycler view
                adapter = new Adapter(numItems, this, dataJson);
                recyclerView.setAdapter(adapter);
                infoMessage.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            recyclerView.setVisibility(View.INVISIBLE);
            infoMessage.setText(R.string.error_message);
            infoMessage.setVisibility(View.VISIBLE);
            Log.d("JSONError", e.toString());
        }
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        // Cancel any existing toast
        if (toast != null) {
            toast.cancel();
        }

        // Display toast when a list item has been pressed
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);

        toast.show();
    }
}