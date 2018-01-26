package it.cnr.iit.sensapp.setup.instagramlogin;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by mattia on 25/01/18.
 */

public class InstagramWebViewController {

    private static String TAG = "InstagramWebViewController";

    private Context context;
    private WebView webView;
    private String url;
    private OAuthDialogListener listener;
    private ProgressDialog spinner;
    private LinearLayout content;
    private AVLoadingIndicatorView loader;

    public InstagramWebViewController(Context context, WebView webView, String url,
                                      OAuthDialogListener listener){

        this.context = context;
        this.webView = webView;
        this.url = url;
        this.listener = listener;
    }

    public void show(AVLoadingIndicatorView loader){
        this.loader = loader;
        setUpWebView();
    }

    private void setUpWebView() {
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        //webView.setLayoutParams(FILL);
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);
            if(loader != null) loader.show();
            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                String urls[] = url.split("=");
                listener.onComplete(urls[1]);
                return true;
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);

            super.onReceivedError(view, errorCode, description, failingUrl);
            listener.onError(description);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);

            super.onPageStarted(view, url, favicon);
            if(loader != null) loader.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished URL: " + url);
            if(loader != null) loader.hide();
        }
    }

    public interface OAuthDialogListener {
        void onComplete(String accessToken);
        void onError(String error);
    }
}
