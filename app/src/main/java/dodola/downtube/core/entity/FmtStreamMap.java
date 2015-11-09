/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core.entity;


public class FmtStreamMap {
	public String fallbackHost;
	/**
	 * 视频签名
	 */
	public String s;
	public String itag;
	public String type;
	/**
	 * 
	 */
	public String quality;
	/**
	 * 原始地址[如无需解密则该链接为真实下载地址]
	 */
	public String url;
	/**
	 * 加密签名
	 */
	public String sig;
	/**
	 * 视频标题
	 */
	public String title;

	public String mediatype;

	public boolean encrypted;
	/**
	 * 视频类型[mp4,3gp]
	 */
	public String extension;
	/**
	 * 分辨率相关信息
	 */
	public Resolution resolution;
	/**
	 * 该视频对应的JS解析代码
	 */
	public String html5playerJS;
	/**
	 * 视频ID
	 */
	public CharSequence videoid;
	/**
	 * 视频下载地址
	 */
	public String realUrl;

	@Override
	public String toString() {
		return "FmtStreamMap [fallbackHost=" + fallbackHost + ", s=" + s + ", itag=" + itag + ", type=" + type + ", quality=" + quality
				+ ", url=" + url + ", sig=" + sig + ", title=" + title + ", mediatype=" + mediatype + ", encrypted=" + encrypted
				+ ", extension=" + extension + ", resolution=" + resolution + ", html5playerJS=" + html5playerJS + ", videoid=" + videoid
				+ "]";
	}

	public String getStreamString() {
		if (resolution != null) {
			return String.format("%s (%s)", extension, resolution.resolution);
		} else {
			return null;
		}
	}
}