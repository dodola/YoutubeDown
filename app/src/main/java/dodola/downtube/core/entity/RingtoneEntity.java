/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core.entity;

public class RingtoneEntity {
	public long currentPosition;
	public boolean Selected;
	public long ModifiedDate;
	public String donwloadUrl;
	public int mDuration;
	public String mName;
	private String mOriginUrl;
	private String videoId;

	/** 播放状态 0：初始状态 1：播放状态 2：暂停状态 3: loading */
	public enum PLAYSTATE {
		INIT_STATE, PLAY_STATE, PAUSE_STATE, LOADING_STATE
	};

	private PLAYSTATE playState = PLAYSTATE.INIT_STATE; // updatePlayState playInterrupt

	// 两个地方修改了playState

	public boolean mPlaying = false;

	private String desc;

	public RingtoneEntity() {
		initParams();
	}

	private void initParams() {
		playState = PLAYSTATE.INIT_STATE;
		mPlaying = false;
		currentPosition = 0;
	}

	public void setId(int id) {
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setPlayState(PLAYSTATE initState) {
		playState = initState;
	}

	public String getDownloadUrl() {
		return donwloadUrl;
	}

	public void setDownloadUrl(String url) {
		donwloadUrl = url;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public int getDuration() {
		return mDuration;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public PLAYSTATE getPlayState() {
		return playState;
	}

	public String getOriginUrl() {
		return mOriginUrl;
	}

	public void setOriginUrl(String mOriginUrl) {
		this.mOriginUrl = mOriginUrl;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
}
