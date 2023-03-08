package com.app.repotracker.utilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Network {

    final static String GITHUB_BASE_URL = "https://api.github.com";
    final static String PARAM_NUM_RESULTS = "per_page";
    final static String NUM_RESULTS = "100"; // Max number of results to retrieve

    /**
     * Builds URL from base URL and query with parameter.
     *
     * @param path The path to query API in
     * @param param The type of the query to append to the URL
     * @param query The search query to add to the URL
     *
     * @return Created URL
     */
    public static URL buildUrl(String path, String param, String query) {
        // Build URI with appended parameters
        Uri builtUri = Uri.parse(GITHUB_BASE_URL + path).buildUpon()
                .appendQueryParameter(param, query)
                .appendQueryParameter(PARAM_NUM_RESULTS, NUM_RESULTS)
                .build();

        // Form URL from created URI
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Builds URL from base URL without any query parameters (overloaded method).
     *
     * @param path The path to query API in
     * @return Created URL
     */
    public static URL buildUrl(String path) {
        // Build URI with appended parameters
        Uri builtUri = Uri.parse(GITHUB_BASE_URL + path).buildUpon()
                .appendQueryParameter(PARAM_NUM_RESULTS, NUM_RESULTS)
                .build();

        // Form URL from created URI
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Returns result from an HTTP request to a URL.
     *
     * @param url The URL to request
     *
     * @return HTTP response
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        // Create HTTP connection from URL
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            // Scan received HTTP response
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            // Return response string if exists
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}