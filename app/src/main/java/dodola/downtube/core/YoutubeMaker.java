///*
// * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
// */
//package dodola.downtube.core;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Scanner;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONObject;
//
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.DialogInterface;
//import android.os.AsyncTask;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import dodola.downtube.core.entity.FmtStreamMap;
//import dodola.downtube.core.entity.VideoPlayBean;
//import dodola.downtube.utils.LogUtil;
//
//public class YoutubeMaker implements IYoutubeMaker {
//    public static final String WATCHV = "http://www.youtube.com/watch?v=%s";
//    private static final String USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
//    private static final String JSPLAYER = "ytplayer\\.config\\s*=\\s*([^\\n]+);";
//    private static final String FUNCCALL = "([$\\w]+)=([$\\w]+)\\(((?:\\w+,?)+)\\)$";
//    private static final String OBJCALL = "([$\\w]+).([$\\w]+)\\(((?:\\w+,?)+)\\)$";
//    private static final String[] REGEX_PRE =
//        {"*", ".", "?", "+", "$", "^", "[", "]", "(", ")", "{", "}", "|", "\\", "/"};
//    private static final String YOUTUBE_IMAGE = "http://i1.ytimg.com/vi/%1$s/default.jpg";// youtube图片地址
//    private Activity mContext;
//    private IDownloadProxy mDownloadProxy;
//    private ProgressDialog mProgressDialog;
//    private int mSelectItem = 0;
//
//    public YoutubeMaker(Activity context, IDownloadProxy downloadProxy) {
//        mContext = context;
//        mDownloadProxy = downloadProxy;
//    }
//
//    protected void showWaitDialog(boolean cancelable) {
//        if (mProgressDialog == null) {
//            mProgressDialog = ProgressDialog.show(mContext, "Loading...", "Please wait...", true, cancelable);
//            mProgressDialog.setCanceledOnTouchOutside(false);
//            mProgressDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                }
//            });
//        } else {
//            mProgressDialog.show();
//        }
//    }
//
//    protected void dismissDialog() {
//        try {
//            if (mProgressDialog != null) {
//                mProgressDialog.dismiss();
//            }
//        } catch (Exception ex) {// 可能会报Token的错误。。。
//        }
//    }
//
//    private class MyTask extends AsyncTask<Void, Void, Void> {
//        StringBuilder functionSb = new StringBuilder();
//        private boolean mIsDownload = true;
//        private List<FmtStreamMap> mResult;
//
//        public MyTask(boolean isDownload, List<FmtStreamMap> result) {
//            mIsDownload = isDownload;
//            mResult = result;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            showWaitDialog(false);
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//            dismissDialog();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            if (mResult == null) {
//                return null;
//            }
//            try {
//                if (mSelectItem < mResult.size()) {
//                    FmtStreamMap fmtStreamMap = mResult.get(mSelectItem);
//                    parseStream(fmtStreamMap);
//                }
//            } catch (Exception ex) {
//
//            }
//            return null;
//        }
//
//        private void parseStream(FmtStreamMap fmtStreamMap) {
//
//            String downloadUrl = "";
//            if (!TextUtils.isEmpty(fmtStreamMap.sig)) {
//                String sig = fmtStreamMap.sig;
//                downloadUrl = String.format("%s&signature=%s", fmtStreamMap.url, sig);
//                startDownload(downloadUrl, fmtStreamMap, mIsDownload);
//            } else if (!TextUtils.isEmpty(fmtStreamMap.s)) {
//
//                //根据版本清理保存的数据，每次升级都监测是否是第一次升级状态
//                int saveVersion =
//                    SharePreferenceDataManager.getInt(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_JS_VERSION.key,
//                        SharePreferenceDataManager.SettingsXml.KEY_JS_VERSION.defaultValue);
//
//                if (saveVersion != ManifestUtil.getVersionCode(mContext)) {//版本不同清理数据
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.PrefsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_HTMLJS.key, "");
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.PrefsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_JSFUNCTION.key, "");
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.PrefsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_FINALF1.key, "");
//                    SharePreferenceDataManager.setInt(mContext, SharePreferenceDataManager.PrefsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_JS_VERSION.key,
//                        ManifestUtil.getVersionCode(mContext));
//                }
//
//                String htmljs =
//                    SharePreferenceDataManager.getString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_HTMLJS.key,
//                        SharePreferenceDataManager.SettingsXml.KEY_HTMLJS.defaultValue);
//                String jsContent = "";
//                if (!htmljs.equals(fmtStreamMap.html5playerJS)) {
//                    // 如果内容发生变化
//                    jsContent = YoutubeUtils.getContent(fmtStreamMap.html5playerJS);
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_HTMLJS.key, fmtStreamMap.html5playerJS);
//                    decipher(jsContent, fmtStreamMap);
//                } else {
//                    // js内容没有变化
//
//                    String jsfunction =
//                        SharePreferenceDataManager.getString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                            SharePreferenceDataManager.SettingsXml.KEY_JSFUNCTION.key,
//                            SharePreferenceDataManager.SettingsXml.KEY_JSFUNCTION.defaultValue);
//                    String finalF1 =
//                        SharePreferenceDataManager.getString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                            SharePreferenceDataManager.SettingsXml.KEY_FINALF1.key,
//                            SharePreferenceDataManager.SettingsXml.KEY_FINALF1.defaultValue);
//                    if (!TextUtils.isEmpty(jsfunction) && !TextUtils.isEmpty(finalF1)) {
//                        decipher(jsfunction, finalF1, fmtStreamMap);
//                    } else {
//                        jsContent = YoutubeUtils.getContent(fmtStreamMap.html5playerJS);
//                        SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                            SharePreferenceDataManager.SettingsXml.KEY_HTMLJS.key, fmtStreamMap.html5playerJS);
//                        decipher(jsContent, fmtStreamMap);
//                    }
//                }
//            } else {
//                startDownload(fmtStreamMap.url, fmtStreamMap, mIsDownload);
//            }
//        }
//
//        /**
//         * 解密
//         *
//         * @return
//         */
//        private void decipher(String jsContent, FmtStreamMap fmtStreamMap) {
//            final String f1 =
//                YoutubeUtils.getRegexString(jsContent, "\\w+\\.sig\\|\\|([$a-zA-Z]+)\\([$a-zA-Z]+\\.[$a-zA-Z]+\\)");
//            String finalF1 = f1;
//
//            int regLen = REGEX_PRE.length;
//            for (int i = 0; i < regLen; i++) {
//
//                if (f1.contains(REGEX_PRE[i])) {
//                    finalF1 = "\\" + f1;
//                    break;
//                }
//            }
//            String f1def =
//                YoutubeUtils.getRegexString(jsContent, String.format("(function %s\\(.*?\\)\\{[^\\{]+\\})", finalF1));
//            trJs(f1def, jsContent);
//
//            if (functionSb.length() > 0) {
//                SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                    SharePreferenceDataManager.SettingsXml.KEY_JSFUNCTION.key, functionSb.toString());
//                SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                    SharePreferenceDataManager.SettingsXml.KEY_FINALF1.key, f1);
//                String jsStr = functionSb.toString() + "\n" + String.format("%s('%s')", f1, fmtStreamMap.s);
//                LogUtil.d("jsStr:" + jsStr);
//                executeJS(jsStr, fmtStreamMap);
//            }
//        }
//
//        private void decipher(String jsfunction, String finalF1, FmtStreamMap fmtStreamMap) {
//            String jsStr = jsfunction + "\n" + String.format("%s('%s')", finalF1, fmtStreamMap.s);
//            executeJS(jsStr, fmtStreamMap);
//        }
//
//        private void executeJS(String js, FmtStreamMap fmtStreamMap) {
//            Context cx = Context.enter();
//            cx.setOptimizationLevel(-1);
//            Scriptable scope = cx.initStandardObjects(null);
//            Object result = null;
//            try {
//                result = cx.evaluateString(scope, js, null, 1, null);
//                String sig = Context.toString(result);
//                LogUtil.d("decryptSignature=" + sig);
//
//                String rawUrl = String.format("%s&signature=%s", fmtStreamMap.url, sig);
//                // 开始下载
//                startDownload(rawUrl, fmtStreamMap, mIsDownload);
//            } catch (JavaScriptException jse) {
//                Log.d("YoutubeParse", jse.getMessage());
//            }
//            Context.exit();
//        }
//
//        public void startDownload(String url, FmtStreamMap fmtStreamMap, boolean isDownload) {
//
//            if (!TextUtils.isEmpty(url) && fmtStreamMap != null && !TextUtils.isEmpty(fmtStreamMap.videoid)) {
//
//                String fileName = fmtStreamMap.title + "." + fmtStreamMap.extension;
//
//                String fileUid = "";
//                try {
//                    fileUid = (fmtStreamMap.videoid + "_" + fmtStreamMap.extension + "_"
//                                   + fmtStreamMap.resolution.resolution);
//                } catch (Exception e) {
//                    fileUid = (String.valueOf(fmtStreamMap.videoid.hashCode()));
//                }
//                if (mDownloadProxy != null) {
//                    final MulitDownloadBean youtubeBean = new MulitDownloadBean();
//                    youtubeBean.setDownloadUrl(url);
//                    youtubeBean.setFilename(fileName);
//                    youtubeBean.setFiletype(Constant.FILETYPE.VIDEO);
//                    youtubeBean.setDownloadState(DownloadState.STATE_INIT);
//                    youtubeBean.setName(fileName);
//                    youtubeBean.setStr1(String.format(YOUTUBE_IMAGE, fmtStreamMap.videoid));
//                    youtubeBean.setStr2(String.format(WATCHV, fmtStreamMap.videoid));
//                    youtubeBean.setStr3(String.valueOf(fmtStreamMap.videoid));
//                    youtubeBean.setFileUID(fileUid);
//                    youtubeBean.setPath(Constant.VIDEO_PATH);
//                    if (isDownload) {
//                        mDownloadProxy.downloadYoutube(youtubeBean);
//                    } else {
//                        mDownloadProxy.playYoutube(youtubeBean);
//                    }
//                }
//            }
//        }
//
//        private void trJs(String jsfunction, String jsContent) {
//            // 将js切成几部分
//            String[] split = jsfunction.split(";");
//            Pattern funcPattern = Pattern.compile(FUNCCALL);
//            Pattern objPattern = Pattern.compile(OBJCALL);
//            Matcher matcher = null;
//            for (String code : split) {
//                String innerFuncCall = null;
//                // 判断是否为obj调用
//                matcher = objPattern.matcher(code);
//                if (matcher.matches()) {// obj调用
//                    String strObj, strFuncName, strArgs;
//                    strObj = matcher.group(1);
//                    strFuncName = matcher.group(2);
//                    strArgs = matcher.group(3);
//                    if (!TextUtils.isEmpty(strObj)) {
//                        jsfunction = jsfunction.replace(strObj + ".", "");
//                    }
//                    String objFunction = "(" + strFuncName + "\\s*:\\s*function\\(.*?\\)\\{[^\\{]+\\})";
//                    String f1def = YoutubeUtils.getRegexString(jsContent, objFunction);
//
//                    if (!TextUtils.isEmpty(f1def)) {
//                        String objFuncMain = "function ";
//                        f1def = f1def.replace(":function", "");
//                        f1def = f1def.replace("}}", "}");
//                        objFuncMain += f1def;
//                        functionSb.append(objFuncMain);
//                        functionSb.append("\n");
//                    }
//                }
//
//                matcher = funcPattern.matcher(code);
//                if (matcher.matches()) {
//                    String strFunName, strArgs;
//                    strFunName = matcher.group(2);
//                    if (!TextUtils.isEmpty(strFunName)) {
//                        strFunName = Pattern.quote(strFunName);
//                    }
//                    strArgs = matcher.group(3);
//                    if (!TextUtils.isEmpty(strArgs)) {
//                        String[] args = strArgs.split(",");
//                        if (args != null) {
//                            if (args.length == 1) {
//                                innerFuncCall = String.format("(function %s\\(\\w+\\)\\{[^\\{]+\\})", strFunName);
//                            } else {
//                                innerFuncCall = String.format("(function %s\\(", strFunName);
//                                for (int i = 0; i < args.length - 1; ++i) {
//                                    innerFuncCall += "\\w+,";
//                                }
//                                innerFuncCall += "\\w+\\)\\{[^\\{]+\\})";
//                            }
//                        } else {
//                            continue;
//                        }
//                    }
//                    if (!TextUtils.isEmpty(innerFuncCall)) {
//
//                        String f1def = YoutubeUtils.getRegexString(jsContent, innerFuncCall);
//                        functionSb.append(f1def);
//                        functionSb.append("\n");
//                    }
//                }
//
//            }
//            functionSb.append(jsfunction);
//        }
//    }
//
//    public void showVideoTypeDialog(final VideoPlayBean bean, final boolean isDownload) {
//        if (bean == null) {
//            return;
//        }
//        final List<FmtStreamMap> result = bean.currentMaps;
//        if (result == null) {
//            return;
//        }
//        mSelectItem = 0;
//
//        if (!isDownload) {
//            int resultSize = result.size();
//            int size = YoutubeUtils.playResolutions.size();
//            FmtStreamMap fmtStreamMap;
//            Resolution resultResolution;
//            Resolution resolution;
//            int tempSelectItem = -1;
//            for (int i = 0; i < size; i++) {
//                resolution = YoutubeUtils.playResolutions.get(i);
//                for (int j = 0; j < resultSize; j++) {
//                    fmtStreamMap = result.get(j);
//                    resultResolution = fmtStreamMap.resolution;
//                    if (TextUtils.equals(resultResolution.id, resolution.id)) {
//                        tempSelectItem = j;
//                        break;
//                    }
//                }
//                if (tempSelectItem != -1) {
//                    break;
//                }
//            }
//            mSelectItem = tempSelectItem;
//            MyTask task = new MyTask(isDownload, result);
//            task.execute();
//
//        } else {
//
//            List<String> streamArrays = new ArrayList<String>();
//            for (int i = 0; i < result.size(); i++) {
//                final String streamType = result.get(i).getStreamString();
//                streamArrays.add(streamType);
//            }
//            String[] item1 = new String[streamArrays.size()];
//            streamArrays.toArray(item1);
//
//            String titleStr = mContext.getString(R.string.select_a_video_type);
//            TubeDialog ad = new TubeDialog.Builder(mContext).setTitle(titleStr)
//                .setSingleChoiceItems(item1, 0, new OnItemClickListener() {
//
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        mSelectItem = position;
//                    }
//                }).setOnPositiveButtonClick(new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                        if (isDownload) {
//                            LogUtil.d("========setOnPositiveButtonClick onClick=========");
//                            FlurryEngine.getInstance().logEvent(FlurryConstant.VIDEO_DOWNLOAD_DIALOG_BTN);
//                            GaHelper.sendEvent(mContext, GaHelper.CATEGORY_YOUTUBE,
//                                FlurryConstant.VIDEO_DOWNLOAD_DIALOG_BTN, null, null);
//                        }
//                        MyTask task = new MyTask(isDownload, result);
//                        task.execute();
//                    }
//                }).setOnNegativeButtonClick(new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        dialog.dismiss();
//                    }
//                }).create();
//
//            ad.show();
//        }
//    }
//
//    /**
//     * 开启一个线程
//     */
//    public void startParse(final String vid, final boolean isDownload) {
//
//        new AsyncTask<Void, Void, List<FmtStreamMap>>() {
//            VideoPlayBean analysisBean = new VideoPlayBean();
//
//            @Override
//            protected void onPreExecute() {
//                super.onPreExecute();
//                showWaitDialog(true);
//
//                boolean needClean =
//                    SharePreferenceDataManager.getBoolean(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_NEED_CLEAN.key, true);
//                if (needClean) {
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_JSFUNCTION.key, "");
//                    SharePreferenceDataManager.setString(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_FINALF1.key, "");
//                    SharePreferenceDataManager.setBoolean(mContext, SharePreferenceDataManager.SettingsXml.XML_NAME,
//                        SharePreferenceDataManager.SettingsXml.KEY_NEED_CLEAN.key, false);
//                }
//            }
//
//            @Override
//            protected void onPostExecute(List<FmtStreamMap> result) {
//                super.onPostExecute(result);
//                try {
//                    dismissDialog();
//                    if (result != null) {
//                        analysisBean.currentMaps = result;
//                        showVideoTypeDialog(analysisBean, isDownload);
//                    }
//                } catch (Exception ex) {
//                    // TOKEN错误
//                    FlurryEngine.getInstance().logEvent(FlurryConstant.VIEDO_PARSE_EXCEPTION);
//                    GaHelper.sendEvent(mContext, GaHelper.CATEGORY_YOUTUBE, FlurryConstant.VIEDO_PARSE_EXCEPTION, null,
//                        null);
//                }
//            }
//
//            @Override
//            protected List<FmtStreamMap> doInBackground(Void... params) {
//                String watchUrl = String.format(WATCHV, vid);
//                DefaultHttpClient client = new DefaultHttpClient();
//                HttpGet getData = new HttpGet(watchUrl);
//                getData.setHeader("User-Agent", USERAGENT);
//                HttpResponse execute;
//                try {
//                    execute = client.execute(getData);
//                    String pageContent = EntityUtils.toString(execute.getEntity(), "utf-8");
//                    Pattern jsPattern = Pattern.compile(JSPLAYER, Pattern.MULTILINE);
//                    Matcher matcher = jsPattern.matcher(pageContent);
//                    if (matcher.find()) {
//                        JSONObject ytplayerConfig = new JSONObject(matcher.group(1));
//                        JSONObject args = ytplayerConfig.getJSONObject("args");
//
//                        String html5playerJS = ytplayerConfig.getJSONObject("assets").getString("js");
//                        if (html5playerJS.startsWith("//")) {
//                            html5playerJS = "http:" + html5playerJS;
//                        }
//
//                        Log.d("Html5PlayerJS", "htmljs:" + html5playerJS);
//                        String fmtStream = args.getString("url_encoded_fmt_stream_map");
//
//                        String[] fmtArray = fmtStream.split(",");
//                        // 数据格式如下
//
//                        List<FmtStreamMap> streamMaps = new ArrayList<FmtStreamMap>();
//                        for (String fmt : fmtArray) {
//                            FmtStreamMap parseFmtStreamMap = YoutubeUtils.parseFmtStreamMap(new Scanner(fmt), "utf-8");
//                            parseFmtStreamMap.html5playerJS = html5playerJS;
//                            parseFmtStreamMap.videoid = args.optString("video_id");
//                            parseFmtStreamMap.title = args.optString("title");
//                            if (parseFmtStreamMap.resolution != null) {
//                                streamMaps.add(parseFmtStreamMap);
//                            }
//                        }
//
//                        String adaptiveStream = args.getString("adaptive_fmts");
//
//                        String[] adaptiveStreamArray = adaptiveStream.split(",");
//                        // 数据格式如下
//
//                        for (String fmt : adaptiveStreamArray) {
//                            FmtStreamMap parseFmtStreamMap = YoutubeUtils.parseFmtStreamMap(new Scanner(fmt), "utf-8");
//                            parseFmtStreamMap.html5playerJS = html5playerJS;
//                            parseFmtStreamMap.videoid = args.optString("video_id");
//                            parseFmtStreamMap.title = args.optString("title");
//                            if (parseFmtStreamMap.resolution != null) {
//                                streamMaps.add(parseFmtStreamMap);
//                            }
//                        }
//
//                        return streamMaps;
//                    }
//                } catch (Exception ex) {
//                    LogUtil.e(ex);
//
//                }
//                return null;
//            }
//        }.execute();
//    }
//}
