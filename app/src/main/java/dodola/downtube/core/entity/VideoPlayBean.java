/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core.entity;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayBean {
	/**
	 * 视频id
	 */
	public String vid;

	/**
	 * 视频的播放流
	 */
	public List<FmtStreamMap> currentMaps = new ArrayList<FmtStreamMap>();
}
