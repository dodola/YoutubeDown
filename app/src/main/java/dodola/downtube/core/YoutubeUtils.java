/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;
import android.util.Log;
import dodola.downtube.core.entity.FmtStreamMap;
import dodola.downtube.core.entity.Resolution;
import dodola.downtube.utils.LogUtil;

/**
 * @Description://youtube工具类
 * @Author sunpengfei
 * @Date 2014-3-22 下午4:51:31
 * @Version
 */
public class YoutubeUtils {
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
    /**
     * 解析地址使用的Useragent
     */
    public static final String USERAGENT = "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)";

    public static List<Resolution> playResolutions = new ArrayList<Resolution>();

    static {
        playResolutions.add(new Resolution("17", "176x144", "3gp", "normal", ResolutionNote.LHD));
        playResolutions.add(new Resolution("36", "320x240", "3gp", "normal", ResolutionNote.LHD));
        playResolutions.add(new Resolution("18", "640x360", "mp4", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("242", "360x240", "webm", "normal", ResolutionNote.LHD));
        playResolutions.add(new Resolution("242", "360x240", "webm", "normal", ResolutionNote.LHD));
        playResolutions.add(new Resolution("243", "480x360", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("243", "480x360", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("43", "640x360", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("244", "640x480", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("245", "640x480", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("167", "640x480", "webm", "video", ResolutionNote.MHD));
        playResolutions.add(new Resolution("246", "640x480", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("247", "720x480", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("44", "854x480", "webm", "normal", ResolutionNote.MHD));
        playResolutions.add(new Resolution("168", "854x480", "webm", "video", ResolutionNote.MHD));
    }

    /**
     * 下载用的分辨率和格式
     */
    public static HashMap<String, Resolution> Resolutions = new HashMap<String, Resolution>();

    static {
        Resolutions.put("5", new Resolution("5", "400x240", "flv", "normal", ResolutionNote.LHD));//
        Resolutions.put("6", new Resolution("6", "450x270", "flv", "normal", ResolutionNote.MHD));
        Resolutions.put("17", new Resolution("17", "176x144", "3gp", "normal", ResolutionNote.LHD));//
        Resolutions.put("18", new Resolution("18", "640x360", "mp4", "normal", ResolutionNote.MHD));
        Resolutions.put("22", new Resolution("22", "1280x720", "mp4", "normal", ResolutionNote.HD));
        Resolutions.put("34", new Resolution("34", "640x360", "flv", "normal", ResolutionNote.MHD));
        Resolutions.put("35", new Resolution("35", "854x480", "flv", "normal", ResolutionNote.MHD));
        Resolutions.put("36", new Resolution("36", "320x240", "3gp", "normal", ResolutionNote.LHD));//
        Resolutions.put("37", new Resolution("37", "1920x1080", "mp4", "normal", ResolutionNote.XLHD));
        Resolutions.put("38", new Resolution("38", "4096x3072", "mp4", "normal", ResolutionNote.XLHD));
        Resolutions.put("43", new Resolution("43", "640x360", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("44", new Resolution("44", "854x480", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("45", new Resolution("45", "1280x720", "webm", "normal", ResolutionNote.HD));
        Resolutions.put("46", new Resolution("46", "1920x1080", "webm", "normal", ResolutionNote.XLHD));
        Resolutions.put("167", new Resolution("167", "640x480", "webm", "video", ResolutionNote.MHD));
        Resolutions.put("168", new Resolution("168", "854x480", "webm", "video", ResolutionNote.MHD));
        Resolutions.put("169", new Resolution("169", "1280x720", "webm", "video", ResolutionNote.HD));
        Resolutions.put("170", new Resolution("170", "1920x1080", "webm", "video", ResolutionNote.XLHD));
        Resolutions.put("242", new Resolution("242", "360x240", "webm", "normal", ResolutionNote.LHD));//
        Resolutions.put("243", new Resolution("243", "480x360", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("244", new Resolution("244", "640x480", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("245", new Resolution("245", "640x480", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("246", new Resolution("246", "640x480", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("247", new Resolution("247", "720x480", "webm", "normal", ResolutionNote.MHD));

        Resolutions.put("82", new Resolution("82", "360p", "mp4", "normal", ResolutionNote.MHD));
        Resolutions.put("83", new Resolution("83", "480p", "mp4", "normal", ResolutionNote.MHD));
        Resolutions.put("84", new Resolution("84", "720p", "mp4", "normal", ResolutionNote.MHD));
        Resolutions.put("85", new Resolution("85", "1080p", "mp4", "normal", ResolutionNote.MHD));
        Resolutions.put("100", new Resolution("100", "360p", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("101", new Resolution("101", "480p", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("102", new Resolution("102", "720p", "webm", "normal", ResolutionNote.MHD));

        // 直播流 ，暂不增加
        // '92': {'ext': 'mp4', 'height': 240, 'format_note': 'HLS',
        // 'preference': -10},
        // '93': {'ext': 'mp4', 'height': 360, 'format_note': 'HLS',
        // 'preference': -10},
        // '94': {'ext': 'mp4', 'height': 480, 'format_note': 'HLS',
        // 'preference': -10},
        // '95': {'ext': 'mp4', 'height': 720, 'format_note': 'HLS',
        // 'preference': -10},
        // '96': {'ext': 'mp4', 'height': 1080, 'format_note': 'HLS',
        // 'preference': -10},
        // '132': {'ext': 'mp4', 'height': 240, 'format_note': 'HLS',
        // 'preference': -10},
        // '151': {'ext': 'mp4', 'height': 72, 'format_note': 'HLS',
        // 'preference': -10},

        //		Resolutions.put("133", new Resolution("133", "240p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("134", new Resolution("134", "360p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("135", new Resolution("135", "480p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("136", new Resolution("136", "720p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("137", new Resolution("137", "1080p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("138", new Resolution("138", "2160p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("160", new Resolution("160", "144p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("264", new Resolution("264", "1440p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("298", new Resolution("298", "720p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("299", new Resolution("299", "1080p", "mp4", "normal", ResolutionNote.MHD));
        //		Resolutions.put("266", new Resolution("266", "2160p", "mp4", "normal", ResolutionNote.MHD));
        // m4a的音频
        Resolutions.put("139", new Resolution("139", "Audio only", "m4a", "normal", ResolutionNote.MHD));
        Resolutions.put("140", new Resolution("140", "Audio only", "m4a", "normal", ResolutionNote.MHD));
        Resolutions.put("141", new Resolution("141", "Audio only", "m4a", "normal", ResolutionNote.MHD));

        //		Resolutions.put("167", new Resolution("167", "360p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("168", new Resolution("168", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("169", new Resolution("169", "720p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("170", new Resolution("170", "1080p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("218", new Resolution("218", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("219", new Resolution("219", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("278", new Resolution("278", "144p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("242", new Resolution("242", "240p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("243", new Resolution("243", "360p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("244", new Resolution("244", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("245", new Resolution("245", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("246", new Resolution("246", "480p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("247", new Resolution("247", "720p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("248", new Resolution("248", "1080p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("271", new Resolution("271", "1440p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("272", new Resolution("272", "2160p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("302", new Resolution("302", "720 p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("303", new Resolution("303", "1080p", "webm", "normal", ResolutionNote.MHD));
        //		Resolutions.put("313", new Resolution("313", "2160p", "webm", "normal", ResolutionNote.MHD));

        Resolutions.put("171", new Resolution("313", "Audio only", "webm", "normal", ResolutionNote.MHD));
        Resolutions.put("172", new Resolution("313", "Audio only", "webm", "normal", ResolutionNote.MHD));
    }

    /**
     * 构造正则表达式对象
     *
     * @param content
     * @param pattern
     *
     * @return
     */
    public static String getRegexString(String content, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(content);
        String group = null;
        if (matcher.find()) {
            LogUtil.d("group:" + matcher.group(0));
            group = matcher.group(1);
        }
        return group;
    }

    /**
     * 从youtube url中解析vid
     *
     * @param url
     *
     * @return
     */
    public static String extractVideoId(String url) {
        try {
            Pattern p = Pattern.compile("(?:^|[^\\w-]+)([\\w-]{11})(?:[^\\w-]+|$)");
            Matcher matcher = p.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    public static void parse(final HashMap<String, String> parameters, final Scanner scanner, final String encoding) {
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2) {
                throw new IllegalArgumentException("bad parameter");
            }

            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2) {
                value = decode(nameValue[1], encoding);
            }
            parameters.put(name, value);
        }
    }

    public static FmtStreamMap parseFmtStreamMap(final Scanner scanner, final String encoding) {
        FmtStreamMap streamMap = new FmtStreamMap();
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 0 || nameValue.length > 2) {
                throw new IllegalArgumentException("bad parameter");
            }

            final String name = decode(nameValue[0], encoding);
            String value = null;
            if (nameValue.length == 2) {
                value = decode(nameValue[1], encoding);
            }
            Log.d("YoutubeUtils", "name:" + name + ",values:" + value);

            // fallback_host=tc.v1.cache8.googlevideo.com&
            // s=9E89E8DE8FF59D59BA5F96D9A220724C1A304F634B2C19.55E8C8A3A7C02C3FBF4D274A85A41F5F55F0401B&
            // itag=17&
            // type=video%2F3gpp%3B+codecs%3D%22mp4v.20.3%2C+mp4a.40.2%22&
            // quality=small&
            // url=http%3A%2F%2Fr20---sn-a5m7lne6.googlevideo.com%2Fvideoplayback%3Fkey%3Dyt5%26ip%3D173.254.202
            // .174%26mt%3D1393571459%26fexp%3D936112%252C937417%252C937416%252C913434%252C936910%252C936913
            // %252C902907%26itag%3D17%26source%3Dyoutube%26sver%3D3%26mv%3Dm%26ms%3Dau%26sparams%3Dgcr%252Cid%252Cip
            // %252Cipbits%252Citag%252Csource%252Cupn%252Cexpire%26ipbits%3D0%26expire%3D1393597755%26gcr%3Dus%26upn
            // %3Du-4gaUCuZCM%26id%3D782b01f5511b174f

            if (TextUtils.equals("fallback_host", name)) {
                streamMap.fallbackHost = value;
            }
            if (TextUtils.equals("s", name)) {
                streamMap.s = value;
            }
            if (TextUtils.equals("itag", name)) {
                streamMap.itag = value;
            }
            if (TextUtils.equals("type", name)) {
                streamMap.type = value;
            }
            if (TextUtils.equals("quality", name)) {
                streamMap.quality = value;
            }
            if (TextUtils.equals("url", name)) {
                streamMap.url = value;
            }
            if (TextUtils.equals("sig", name)) {
                streamMap.sig = value;
            }
            if (TextUtils.equals("signature", name)) {

            }
            if (!TextUtils.isEmpty(streamMap.itag)) {
                streamMap.resolution = Resolutions.get(streamMap.itag);// 下载的格式要比播放的格式多
            }
            if (streamMap.resolution != null) {
                streamMap.extension = streamMap.resolution.format;
            }

        }
        return streamMap;
    }

    /**
     * url解码
     *
     * @param content
     * @param encoding
     *
     * @return
     */
    public static String decode(final String content, final String encoding) {
        try {
            return URLDecoder.decode(content, encoding != null ? encoding : HTTP.DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }

    /**
     * 获取url里的内容
     *
     * @param url
     *
     * @return 解析失败返回 Null
     */
    //    public static String getContent(String url) {
    //        DefaultHttpClient client = new DefaultHttpClient();
    //        HttpGet getData = new HttpGet(url);
    //        getData.setHeader("User-Agent", USERAGENT);
    //        HttpResponse execute;
    //        String data = null;
    //        try {
    //            execute = client.execute(getData);
    //            HttpEntity entity = execute.getEntity();
    //            data = EntityUtils.toString(entity, "utf-8");
    //        } catch (IOException e) {
    //            LogUtil.e(e);
    //        }
    //
    //        return data;
    //    }
    public static String getContent(String url) {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuilder resultBuffer = new StringBuilder();
        String tempLine = null;
        try {
            URL localURL = new URL(url);

            URLConnection connection = openConnection(localURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                    "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }

        } catch (Exception ex) {
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return resultBuffer.toString();
    }

    private static URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        connection = localURL.openConnection();
        return connection;
    }

}
