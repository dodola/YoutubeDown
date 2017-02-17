/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.squareup.duktape.Duktape;

import android.text.TextUtils;
import android.util.Log;
import dodola.downtube.core.entity.FmtStreamMap;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by sunpengfei on 15/11/9.
 */
public class RxYoutube {
    public static final String BASEURL = "http://www.youtube.com/";
    public static final String WATCHV = "http://www.youtube.com/watch?v=%s";
    private static final String USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";
    private static final String JSPLAYER = "ytplayer\\.config\\s*=\\s*([^\\n]+);";
    private static final String FUNCCALL = "([$\\w]+)=([$\\w]+)\\(((?:\\w+,?)+)\\)$";
    private static final String OBJCALL = "([$\\w]+).([$\\w]+)\\(((?:\\w+,?)+)\\)$";
    private static final String[] REGEX_PRE =
        {"*", ".", "?", "+", "$", "^", "[", "]", "(", ")", "{", "}", "|", "\\", "/"};

    public static void fetchYoutube(final String vid, Subscriber<List<FmtStreamMap>> resultSubscriber) {

        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                //下载youtube播放页面

                String watchUrl = String.format(WATCHV, vid);
                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet getData = new HttpGet(watchUrl);
                getData.setHeader("User-Agent", USERAGENT);
                HttpResponse execute;
                try {
                    execute = client.execute(getData);
                    String pageContent = EntityUtils.toString(execute.getEntity(), "utf-8");
                    subscriber.onNext(pageContent);
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }

            }
        }).map(new Func1<String, List<FmtStreamMap>>() {
            @Override
            public List<FmtStreamMap> call(String pageContent) {
                //转换成StreamMap

                try {
                    Pattern jsPattern = Pattern.compile(JSPLAYER, Pattern.MULTILINE);
                    Matcher matcher = jsPattern.matcher(pageContent);
                    if (matcher.find()) {
                        JSONObject ytplayerConfig = new JSONObject(matcher.group(1));
                        JSONObject args = ytplayerConfig.getJSONObject("args");

                        String html5playerJS = ytplayerConfig.getJSONObject("assets").getString("js");
                        if (html5playerJS.startsWith("//")) {
                            html5playerJS = "http:" + html5playerJS;
                        } else if (html5playerJS.startsWith("/")) {
                            html5playerJS = BASEURL + html5playerJS;
                        }

                        Log.d("Html5PlayerJS", "htmljs:" + html5playerJS);
                        String fmtStream = args.getString("url_encoded_fmt_stream_map");

                        String[] fmtArray = fmtStream.split(",");
                        // 数据格式如下

                        List<FmtStreamMap> streamMaps = new ArrayList<FmtStreamMap>();
                        for (String fmt : fmtArray) {
                            FmtStreamMap parseFmtStreamMap = YoutubeUtils.parseFmtStreamMap(new Scanner(fmt), "utf-8");
                            parseFmtStreamMap.html5playerJS = html5playerJS;
                            parseFmtStreamMap.videoid = args.optString("video_id");
                            parseFmtStreamMap.title = args.optString("title");
                            if (parseFmtStreamMap.resolution != null) {
                                streamMaps.add(parseFmtStreamMap);
                            }
                        }

                        String adaptiveStream = args.getString("adaptive_fmts");

                        String[] adaptiveStreamArray = adaptiveStream.split(",");
                        // 数据格式如下

                        for (String fmt : adaptiveStreamArray) {
                            FmtStreamMap parseFmtStreamMap = YoutubeUtils.parseFmtStreamMap(new Scanner(fmt), "utf-8");
                            parseFmtStreamMap.html5playerJS = html5playerJS;
                            parseFmtStreamMap.videoid = args.optString("video_id");
                            parseFmtStreamMap.title = args.optString("title");
                            if (parseFmtStreamMap.resolution != null) {
                                streamMaps.add(parseFmtStreamMap);
                            }
                        }

                        return streamMaps;
                    }
                } catch (Exception ex) {
                    Observable.error(ex);
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber);
    }

    public static void parseDownloadUrl(final FmtStreamMap fmtStreamMap, Subscriber<String> resultSubscriber) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String downloadUrl = null;
                if (!TextUtils.isEmpty(fmtStreamMap.sig)) {
                    String sig = fmtStreamMap.sig;
                    downloadUrl = String.format("%s&signature=%s", fmtStreamMap.url, sig);
                } else {
                    String jsContent = YoutubeUtils.getContent(fmtStreamMap.html5playerJS);
                    downloadUrl = (decipher(jsContent, fmtStreamMap));
                }
                subscriber.onNext(downloadUrl);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(resultSubscriber);
    }

    private static String decipher(String jsContent, FmtStreamMap fmtStreamMap) {
        String f1 =
            YoutubeUtils.getRegexString(jsContent, "\\w+\\.sig\\|\\|([$a-zA-Z]+)\\([$a-zA-Z]+\\ .[$a-zA-Z]+\\)");
        if (TextUtils.isEmpty(f1)) {
            f1 = YoutubeUtils
                .getRegexString(jsContent,
                    "\\w+\\.sig.*?\\?.*&&\\w+\\.set\\(\\\"signature\\\",([$a-zA-Z]+)\\([$a-zA-Z]+\\"
                        + ".[$a-zA-Z]+\\)\\)");
        }
        String finalF1 = f1;

        for (String aREGEX_PRE : REGEX_PRE) {

            if (f1.contains(aREGEX_PRE)) {
                finalF1 = "\\" + f1;
                break;
            }
        }
        String f1def =
            YoutubeUtils.getRegexString(jsContent, String.format(
                "((function\\s+%s|[{;,]%s\\s*=\\s*function|var\\s+%s\\s*=\\s*function\\s*)\\([^)]*\\)"
                    + "\\s*\\{[^\\{]+\\})",
                finalF1, finalF1, finalF1));

        if (f1def.startsWith(",")) {
            f1def = f1def.replaceFirst(",", "");
        }

        StringBuilder functionSb = new StringBuilder();
        trJs(f1def, jsContent, functionSb);

        if (functionSb.length() > 0) {
            String jsStr = functionSb.toString() + "\n" + String.format("%s('%s')", f1, fmtStreamMap.s);

            Duktape duktape = Duktape.create();
            try {
                String sig = duktape.evaluate(jsStr);
                return String.format("%s&signature=%s", fmtStreamMap.url, sig);
            } finally {
                duktape.close();
            }
        }
        return null;
    }

    private static void trJs(String jsfunction, String jsContent, StringBuilder functionSb) {
        // 将js切成几部分
        String[] split = jsfunction.split(";");
        Pattern funcPattern = Pattern.compile(FUNCCALL);
        Pattern objPattern = Pattern.compile(OBJCALL);
        Matcher matcher = null;
        for (String code : split) {
            String innerFuncCall = null;
            // 判断是否为obj调用
            matcher = objPattern.matcher(code);
            if (matcher.matches()) {// obj调用
                String strObj, strFuncName, strArgs;
                strObj = matcher.group(1);
                strFuncName = matcher.group(2);
                strArgs = matcher.group(3);
                if (!TextUtils.isEmpty(strObj)) {
                    jsfunction = jsfunction.replace(strObj + ".", "");
                }
                String objFunction = "(" + strFuncName + "\\s*:\\s*function\\(.*?\\)\\{[^\\{]+\\})";
                String f1def = YoutubeUtils.getRegexString(jsContent, objFunction);

                if (!TextUtils.isEmpty(f1def)) {
                    String objFuncMain = "function ";
                    f1def = f1def.replace(":function", "");
                    f1def = f1def.replace("}}", "}");
                    objFuncMain += f1def;
                    functionSb.append(objFuncMain);
                    functionSb.append("\n");
                }
            }

            matcher = funcPattern.matcher(code);
            if (matcher.matches()) {
                String strFunName, strArgs;
                strFunName = matcher.group(2);
                if (!TextUtils.isEmpty(strFunName)) {
                    strFunName = Pattern.quote(strFunName);
                }
                strArgs = matcher.group(3);
                if (!TextUtils.isEmpty(strArgs)) {
                    String[] args = strArgs.split(",");
                    if (args.length == 1) {
                        innerFuncCall = String.format("(function %s\\(\\w+\\)\\{[^\\{]+\\})", strFunName);
                    } else {
                        innerFuncCall = String.format("(function %s\\(", strFunName);
                        for (int i = 0; i < args.length - 1; ++i) {
                            innerFuncCall += "\\w+,";
                        }
                        innerFuncCall += "\\w+\\)\\{[^\\{]+\\})";
                    }
                }
                if (!TextUtils.isEmpty(innerFuncCall)) {

                    String f1def = YoutubeUtils.getRegexString(jsContent, innerFuncCall);
                    functionSb.append(f1def);
                    functionSb.append("\n");
                }
            }

        }
        functionSb.append(jsfunction);
    }

}
