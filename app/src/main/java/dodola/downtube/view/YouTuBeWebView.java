/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

public class YouTuBeWebView extends WebView {

    DisplayFinish df;

    public void setDf(DisplayFinish df) {
        this.df = df;
    }

    public YouTuBeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YouTuBeWebView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //		try {
        //			df.After();
        //		} catch (Exception ex) {
        //
        //		}
    }

    public interface DisplayFinish {
        public void After();
    }

    private boolean is_gone = false;

    public void visibleChange(int visibility) {
        if (visibility == View.GONE) {
            try {
                WebView.class.getMethod("onPause").invoke(this);//stop flash
            } catch (Exception e) {
            }
            this.pauseTimers();
            this.is_gone = true;
        } else if (visibility == View.VISIBLE) {
            try {
                WebView.class.getMethod("onResume").invoke(this);//resume flash
            } catch (Exception e) {
            }
            this.resumeTimers();
            this.is_gone = false;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollListener != null) {
            onScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    public void onDetachedFromWindow() {//this will be trigger when back key pressed, not when home key pressed
        super.onDetachedFromWindow();
        if (this.is_gone) {
            try {
                this.destroy();
            } catch (Exception e) {
            }
        }
    }

    WebViewScrollListener onScrollListener;

    public void setOnScrollListener(WebViewScrollListener s) {
        onScrollListener = s;
    }

    public interface WebViewScrollListener {
        public void onScrollChanged(int l, int t, int oldl, int oldt);
    }
}
