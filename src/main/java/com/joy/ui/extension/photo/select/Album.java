package com.joy.ui.extension.photo.select;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daisw on 2017/11/22.
 */

public class Album implements Parcelable {

    private String displayName;
    private int coverId;
    private String coverPath;
    private int elementSize;
    private List<Photo> elements;

    public Album() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getCoverId() {
        return coverId;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public int getElementSize() {
        return elementSize;
    }

    public void setElementSize(int elementSize) {
        this.elementSize = elementSize;
    }

    public List<Photo> getElements() {
        return elements;
    }

    public void setElements(List<Photo> elements) {
        this.elements = elements;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeInt(coverId);
        dest.writeString(coverPath);
        dest.writeInt(elementSize);
        dest.writeList(elements);
    }

    protected Album(Parcel in) {
        displayName = in.readString();
        coverId = in.readInt();
        coverPath = in.readString();
        elementSize = in.readInt();
        elements = elements == null ? new ArrayList<>() : elements;
        in.readList(elements, getClass().getClassLoader());
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
