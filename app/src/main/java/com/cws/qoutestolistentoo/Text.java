package com.cws.qoutestolistentoo;

import android.os.Parcel;
import android.os.Parcelable;

public class Text implements Parcelable {

    private String author;
    private String title;
    private String CC;
    private int resid;

    Text(String author, String title, String CC, int resid){
        this.author = author;
        this.title = title;
        this.CC = CC;
        this.resid = resid;
    }

    protected Text(Parcel in) {
        author = in.readString();
        title = in.readString();
        CC = in.readString();
        resid = in.readInt();
    }

    public static final Creator<Text> CREATOR = new Creator<Text>() {
        @Override
        public Text createFromParcel(Parcel in) {
            return new Text(in);
        }

        @Override
        public Text[] newArray(int size) {
            return new Text[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCC() {
        return CC;
    }

    public void setCC(String CC) {
        this.CC = CC;
    }

    public int getResid() {
        return resid;
    }

    public void setResid(int resid) {
        this.resid = resid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.resid);
        parcel.writeString(this.title);
        parcel.writeString(this.author);
        parcel.writeString(this.CC);
    }
}
