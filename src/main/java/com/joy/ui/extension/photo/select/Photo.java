package com.joy.ui.extension.photo.select;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Daisw on 2017/11/22.
 */

public class Photo implements Parcelable {

    private int id;
    private boolean isSelected;
    private String path;
//    private String thumbnailPath;
    private String createTime;
    private int orientation;
    private int width;
    private int height;

    private boolean enable = true;

    public Photo() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

//    public String getThumbnailPath() {
//        return thumbnailPath;
//    }
//
//    public void setThumbnailPath(String thumbnailPath) {
//        this.thumbnailPath = thumbnailPath;
//    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeString(path);
//        dest.writeString(thumbnailPath);
        dest.writeString(createTime);
        dest.writeInt(orientation);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    protected Photo(Parcel in) {
        id = in.readInt();
        isSelected = in.readByte() != 0;
        path = in.readString();
//        thumbnailPath = in.readString();
        createTime = in.readString();
        orientation = in.readInt();
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
