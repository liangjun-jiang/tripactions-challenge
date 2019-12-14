package com.ljapps.tripactionchallenge.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ljapps.tripactionchallenge.R;
import com.ljapps.tripactionchallenge.databinding.ActivityDetailBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;

public class DetailActivity extends AppCompatActivity {

    ActivityDetailBinding b;
    String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.title_detail_activity);

        mUrl = getIntent().getStringExtra(MainActivity.EXTRA_ARTICLE_URL);

        b = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Display ProgressBar until page is loaded
        b.progressBar.setIndeterminate(true);
        b.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                b.progressBar.setVisibility(View.GONE);
                b.webView.setVisibility(View.VISIBLE);
                super.onPageFinished(view, url);
            }
        });
        b.webView.loadUrl(mUrl);
    }

    // Make navigation button in app bar behave like the device back button, i.e. return to previous
    // activity without calling onCreate, in this way, search results in MainActivity are maintained
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Change the device back button to a back button of the embedded web browser
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (b.webView.canGoBack()) {
                        b.webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mUrl);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider p = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        p.setShareIntent(intent);

        return super.onCreateOptionsMenu(menu);
    }
}
