package com.app.repotracker.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.repotracker.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private static final String TAG = Adapter.class.getSimpleName();
    final private ListItemClickListener onClickListener;
    private static int viewHolderCount;
    private Context context;
    private int numberOfItems;
    private JSONArray data;

    /**
     * Interface for receiving message that a list item has been clicked.
     */
    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    /**
     * Create Adapter with a number of items to list and a listener for clicked items.
     *
     * @param numberOfItems Number of items in list
     * @param listener Listener for list item clicks
     * @param data JSON of data to use
     */
    public Adapter(Context context, int numberOfItems, ListItemClickListener listener, JSONArray data) {
        this.context = context;
        this.numberOfItems = numberOfItems;
        this.onClickListener = listener;
        this.data = data;
        viewHolderCount = 0;
    }

    /**
     * Called when every ViewHolder is created.
     *
     * @param viewGroup ViewGroup of the ViewHolders
     * @param viewType  Type of view, to specify which layout to use in ViewHolders.
     *
     * @return ViewHolder to hold View for each list item
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);

        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);
        return viewHolder;
    }

    /**
     * Binds data with ViewHolder in a specific position.
     *
     * @param holder   The ViewHolder to be updated with data
     * @param position The position of the item
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        try {
            JSONObject item = (JSONObject) data.get(position);
            String name = item.getString("name");
            String owner = item.getJSONObject("owner").getString("login");
            int issues = item.getInt("open_issues_count");
            int forks = item.getInt("forks");
            String url = item.getString("html_url");
            holder.bind(name, owner, issues, forks, url);

            // Set background colour to alternate
            int nightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch(position % 2) {
                case 0:
                    if (nightMode == Configuration.UI_MODE_NIGHT_NO)
                        holder.itemView.setBackgroundColor(Color.WHITE);
                    else
                        holder.itemView.setBackgroundColor(Color.BLACK);
                    break;
                case 1:
                    if (nightMode == Configuration.UI_MODE_NIGHT_NO)
                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                    else
                        holder.itemView.setBackgroundColor(Color.DKGRAY);
                    break;
            }

        } catch (JSONException e) {
            Log.d("JSONError", e.toString());
        }
    }

    /**
     * Getter method for the number of items.
     *
     * @return Number of items
     */
    @Override
    public int getItemCount() {
        return numberOfItems;
    }

    /**
     * ViewHolder to describe an item.
     */
    class ViewHolder extends RecyclerView.ViewHolder
            implements OnClickListener {

        TextView repoName;
        TextView repoOwner;
        TextView repoIssues;
        TextView forks;
        String url;

        /**
         * Create ViewHolder from an inflated View.
         *
         * @param itemView The inflated View item
         */
        public ViewHolder(View itemView) {
            super(itemView);

            repoName = (TextView) itemView.findViewById(R.id.repo_name);
            repoOwner = (TextView) itemView.findViewById(R.id.repo_owner);
            repoIssues = (TextView) itemView.findViewById(R.id.repo_issues);
            forks = (TextView) itemView.findViewById(R.id.forks);
            //  Call setOnClickListener on the View passed into the constructor (use 'this' as the OnClickListener)
            itemView.setOnClickListener(this);
        }

        /**
         * Binds data to ViewHolder.
         *
         * @param name Name of repository
         * @param owner Owner of repository
         * @param issues Number of open issues in repository
         * @param forks Number of forks in repository
         * @param url The url link to the repository
         */
        void bind(String name, String owner, int issues, int forks, String url) {
            String repo = "Repository: " + name;
            String by = "Owner: " + owner;
            String numIssues = "Issues: " + issues;
            String numForks = "Forks: " + forks;
            repoName.setText(repo);
            repoOwner.setText(by);
            this.repoIssues.setText(numIssues);
            this.forks.setText(numForks);
            this.url = url;
        }

        /**
         * Called when an item in the list is clicked.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            onClickListener.onListItemClick(clickedPosition);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            v.getContext().startActivity(intent);
        }
    }
}

