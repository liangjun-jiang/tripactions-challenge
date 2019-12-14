package com.ljapps.tripactionchallenge.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ljapps.tripactionchallenge.R;
import com.ljapps.tripactionchallenge.adapter.ArticleAdapter;
import com.ljapps.tripactionchallenge.api.ApiServiceSingleton;
import com.ljapps.tripactionchallenge.databinding.ActivityMainBinding;
import com.ljapps.tripactionchallenge.decoration.SpacesItemDecoration;
import com.ljapps.tripactionchallenge.listener.EndlessRecyclerViewScrollListener;
import com.ljapps.tripactionchallenge.listener.RecyclerViewItemClickSupport;
import com.ljapps.tripactionchallenge.model.ApiResponse;
import com.ljapps.tripactionchallenge.model.Doc;
import com.ljapps.tripactionchallenge.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_ARTICLE_URL = "ArticleUrl";

    // General
    Activity mActivity;
    SharedPreferences mPref;
    ActivityMainBinding b;

    // RecyclerView
    ArrayList<Doc> mArticles;
    ArticleAdapter mAdapter;
    ProgressDialog mProgressDialog;
    EndlessRecyclerViewScrollListener mScrollListener;

    // API requests
    String mQuery;

    // UI
    MenuItem mFilterMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mActivity = this;
        mPref = getPreferences(MODE_PRIVATE);
        mArticles = new ArrayList<>();
        mProgressDialog = setupProgressDialog();
        mAdapter = new ArticleAdapter(mArticles, this);

        // Set up RecyclerView
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadData(page);
            }
        };
        b.recyclerView.setAdapter(mAdapter);
        b.recyclerView.setLayoutManager(layoutManager);
        b.recyclerView.addItemDecoration(new SpacesItemDecoration(16));
        b.recyclerView.addOnScrollListener(mScrollListener);

        RecyclerViewItemClickSupport.addTo(b.recyclerView).setOnItemClickListener((recyclerView, position, v) -> {
            Intent intent = new Intent(mActivity, DetailActivity.class);
            intent.putExtra(EXTRA_ARTICLE_URL, mArticles.get(position).getWebUrl());
            startActivity(intent);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        setupSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }


    private ProgressDialog setupProgressDialog() {
        ProgressDialog p =  new ProgressDialog(mActivity);
        p.setIndeterminate(true);
        p.setMessage(getString(R.string.progress_loading));
        return p;
    }

    // Read filters from SharedPreferences, and construct and execute the API query
    private void loadData(int page) {
        // Read query parameter values from SharedPreferences
        String beginDate = nullify(mPref.getString(getString(R.string.pref_begin_date), ""));
        String endDate = nullify(mPref.getString(getString(R.string.pref_end_date), ""));
        String sortOrder = nullify(mPref.getString(getString(R.string.pref_sort_order), ""));
        String newsDesk = nullify(mPref.getString(getString(R.string.pref_news_desk), ""));
        String query = nullify(mQuery);

        if (newsDesk != null) {
            ArrayList<String> a = new ArrayList<>(Arrays.asList(newsDesk.split(":")));
            Iterator itr = a.iterator();
            newsDesk = "news_desk:(";
            while (itr.hasNext()) {
                newsDesk += "\"" + itr.next() + "\"";
                if (itr.hasNext()) newsDesk += " ";
                else newsDesk += ")";
            }
        }

        query(query, newsDesk, beginDate, endDate, sortOrder, page);
    }

    // Return null for an empty string, and the original string for an non-empty string
    private String nullify(String str) {
        return (str.isEmpty()) ? null : str;
    }

    private void setupSearchView(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // Called when query is submitted (by pressing "Search" button on keyboard)
            // Note: empty search queries detected by the SearchView itself and ignored
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.clearArticles();
                mScrollListener.resetState();
                mQuery = query;
                loadData(0);
                loadData(1);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void query(String q, String fq, String beginDate, String endDate, String sort, Integer page) {
        mProgressDialog.show();
        Call<ApiResponse> call = ApiServiceSingleton.getInstance().query(q, fq, beginDate, endDate, sort, page);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // API rate limits: 1000 requests per day, 1 request per second (check X-RateLimit
                // fields in HTTP response).
                if (response.code() == 429) {
                    Log.v(LOG_TAG, response.code() + ": rate limit exceeded");
                    return;
                }
                try {
                    ArrayList<Doc> articles = (ArrayList<Doc>) response.body().getResponse().getDocs();
                    if (articles.isEmpty()) {
                        Util.toastLong(mActivity, getString(R.string.toast_no_results));
                    }
                    else
                        mAdapter.appendArticles(articles);
                }
                catch (NullPointerException e) {
                    fail(e);
                }
                mProgressDialog.dismiss();
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                mProgressDialog.dismiss();
                fail(t);
            }

            private void fail(Throwable t) {
                Util.toastLong(mActivity, "Query failed: " + t.getClass().getSimpleName());
                t.printStackTrace();
            }
        });
    }
}
