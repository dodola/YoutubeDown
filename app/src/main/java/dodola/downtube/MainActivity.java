/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;
import dodola.downtube.core.RxYoutube;
import dodola.downtube.core.YoutubeUtils;
import dodola.downtube.core.entity.FmtStreamMap;
import dodola.downtube.utils.LogUtil;
import dodola.downtube.view.YouTuBeWebView;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    private ProgressDialog mProgressDialog;
    private DownloadManager downloadManager;
    private YouTuBeWebView myWebView;
    private WebChromeClient mWebChromeClient;
    private VideoView mVideoView = null;
    private WebChromeClient.CustomViewCallback mCustomViewCallback = null;
    private String mVideoId;
    private String mCurrentUrl;
    private LayoutInflater layoutInflater;
    private View videoView;
    public static final String YOUTUBE = "https://m.youtube.com/";
    private String loadUrl = YOUTUBE;
    private FloatingActionButton fab;
    private ProgressBar mLoadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myWebView = (YouTuBeWebView) findViewById(R.id.webview);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mLoadingProgressBar = (ProgressBar) findViewById(R.id.progress);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showWaitDialog();
                //调用解析
                RxYoutube.fetchYoutube(mVideoId, new Subscriber<List<FmtStreamMap>>() {
                    @Override
                    public void onCompleted() {
                        dismissWaitDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissWaitDialog();
                    }

                    @Override
                    public void onNext(List<FmtStreamMap> fmtStreamMaps) {
                        dismissWaitDialog();
                        showDialog(fmtStreamMaps);
                    }
                });
            }
        });
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initWebView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //            super.onBackPressed();
            if (myWebView.canGoBack()) {
                myWebView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void showDialog(final List<FmtStreamMap> result) {
        if (result != null && result.size() > 0) {
            List<String> streamArrays = new ArrayList<String>();
            for (int i = 0; i < result.size(); i++) {
                final String streamType = result.get(i).getStreamString();
                streamArrays.add(streamType);
            }
            String[] item1 = new String[streamArrays.size()];
            streamArrays.toArray(item1);

            Dialog alertDialog = new AlertDialog.Builder(this).
                setTitle("选择下载类型").
                setIcon(R.mipmap.ic_launcher)
                .setItems(item1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final FmtStreamMap fmtStreamMap = result.get(which);
                        RxYoutube.parseDownloadUrl(fmtStreamMap, new Subscriber<String>() {
                            @Override
                            public void onCompleted() {
                                dismissWaitDialog();
                            }

                            @Override
                            public void onError(Throwable e) {
                                dismissWaitDialog();
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Download Error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNext(String downloadUrl) {
                                dismissWaitDialog();

                                //调用系统下载
                                String fileName = fmtStreamMap.title + "." + fmtStreamMap.extension;
                                Uri uri = Uri.parse(downloadUrl);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setDestinationInExternalFilesDir(MainActivity.this,
                                    Environment.DIRECTORY_MOVIES, fileName);
                                downloadManager.enqueue(request);
                            }
                        });

                    }
                }).
                    setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).
                    create();
            alertDialog.show();
        }
    }

    protected void showWaitDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "Loading...", "Please wait...", true, true);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            });
        } else {
            mProgressDialog.show();
        }
    }

    private void dismissWaitDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 初始化WebView
     */
    private void initWebView() {
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAppCacheEnabled(false);
        final String USER_AGENT_STRING = myWebView.getSettings().getUserAgentString() + " Rong/2.0";
        webSettings.setUserAgentString(USER_AGENT_STRING);
        webSettings.setSupportZoom(false);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        mWebChromeClient = new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress >= 90) {
                    mLoadingProgressBar.setVisibility(View.GONE);
                } else {
                    if (mLoadingProgressBar.getVisibility() == View.GONE) {
                        mLoadingProgressBar.setVisibility(View.VISIBLE);
                    }
                    mLoadingProgressBar.setProgress(newProgress);
                }
            }

            @Override
            public View getVideoLoadingProgressView() {
                try {
                    myWebView.requestFocus();
                } catch (Exception ex) {
                    LogUtil.e(ex);
                }
                if (layoutInflater == null) {
                    layoutInflater = LayoutInflater.from(MainActivity.this);
                }
                View loadingView = layoutInflater.inflate(R.layout.tube_loading, null);
                return loadingView;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onHideCustomView() {
                hideFullscreen();
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // mRootView.addView(view);
                LogUtil.d("=====onShowCustomView=====");
                mCustomViewCallback = callback;

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    FrameLayout frame = (FrameLayout) view;

                    if (view instanceof FrameLayout) {

                        if (frame.getFocusedChild() instanceof VideoView) {

                            mVideoView = (VideoView) frame.getFocusedChild();

                            try {
                                Field field = VideoView.class.getDeclaredField("mUri");
                                field.setAccessible(true);
                                Uri videouri = (Uri) field.get(mVideoView);

                                Intent intentv = new Intent(Intent.ACTION_VIEW);
                                intentv.setDataAndType(videouri, "video/*");
                                startActivity(intentv);
                            } catch (Exception e) {
                                LogUtil.e(e);
                            }
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    changeFullscreen();
                    videoView = view;
                    ((ViewGroup) getWindow().getDecorView()).addView(videoView);

                } else {
                    try {
                        Field localField2 = Class.forName("android.webkit.HTML5VideoFullScreen$VideoSurfaceView")
                            .getDeclaredField("this$0");
                        localField2.setAccessible(true);
                        Object localObject = localField2.get(((FrameLayout) view).getFocusedChild());
                        Field localField3 = localField2.getType().getSuperclass().getDeclaredField("mUri");
                        localField3.setAccessible(true);
                        Uri localUri2 = (Uri) localField3.get(localObject);
                        Intent intentv = new Intent(Intent.ACTION_VIEW);
                        intentv.setDataAndType(localUri2, "video/*");
                        startActivity(intentv);
                    } catch (Exception localException1) {
                        LogUtil.e(localException1);
                    }

                }
            }
        };
        myWebView.setWebChromeClient(mWebChromeClient);
        myWebView.loadUrl(loadUrl);
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                if (myWebView == null || TextUtils.isEmpty(url)) {
                    return;
                }

                try {
                    LogUtil.d("=======doUpdateVisitedHistory=======");
                    updateButtonUI();
                } catch (Exception ex) {
                    LogUtil.e(ex);
                }
                super.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogUtil.d("========onReceivedError========");
                if ((errorCode == WebViewClient.ERROR_HOST_LOOKUP) || (errorCode == WebViewClient.ERROR_TIMEOUT)
                    || (errorCode == WebViewClient.ERROR_CONNECT)) {
                    myWebView.loadData("", "text/html", "utf-8");
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtil.d("======shouldOverrideUrlLoading=====" + url);
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        myWebView.setDf(new YouTuBeWebView.DisplayFinish() {

            @Override
            public void After() {

                if (myWebView != null) {
                    String urlx = myWebView.getUrl();
                    if (urlx != null) {
                        if (!TextUtils.isEmpty(urlx)) {
                            mVideoId = YoutubeUtils.extractVideoId(urlx);
                        }
                        if (!TextUtils.isEmpty(mVideoId)) {
                            mCurrentUrl = urlx;
                        }
                        updateButtonUI();
                    }
                }
            }

        });
    }

    public void hideFullscreen() {
        if (mCustomViewCallback != null) {
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            if (mVideoView != null) {
                mVideoView = null;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (videoView != null) {
                ((ViewGroup) getWindow().getDecorView()).removeView(videoView);
            }
            videoView = null;
            exitFullscreen();
        }
    }

    private boolean mIsFullscreen;

    public boolean isFullScreen() {
        return mIsFullscreen;
    }

    public void changeFullscreen() {
        mIsFullscreen = true;
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void exitFullscreen() {
        mIsFullscreen = false;
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void updateButtonUI() {
        if (myWebView == null) {
            return;
        }
        String urlx = myWebView.getUrl();
        if (!TextUtils.isEmpty(urlx)) {
            mVideoId = YoutubeUtils.extractVideoId(urlx);
            LogUtil.d("mVideoId:" + mVideoId);
        }
        if (!TextUtils.isEmpty(mVideoId)) {
            mCurrentUrl = urlx;
        }
        fab.setEnabled(!TextUtils.isEmpty(mVideoId));
    }
}
