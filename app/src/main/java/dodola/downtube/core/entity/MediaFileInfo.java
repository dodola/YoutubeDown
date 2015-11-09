/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package dodola.downtube.core.entity;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaFileInfo implements Serializable {

	private static final long serialVersionUID = 401314654472890540L;

	public enum FileCategory implements Serializable {
		Music, Video, Picture, Doc, Apk, Other, Albums, PicBack
	}

	public String folderPath = "";

	public String filePath = "";

	public String fileName = "";

	public long fileSize;

	public int Count;

	public long ModifiedDate;

	public boolean Selected;

	public long duration;

	public boolean canRead;

	public boolean canWrite;

	public boolean isHidden;

	public FileCategory fileType;// 1 app 2 video 3 picture 4 music 5 document

	public long dbId;

}
