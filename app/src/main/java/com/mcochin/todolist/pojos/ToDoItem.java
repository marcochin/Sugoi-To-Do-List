package com.mcochin.todolist.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Marco on 5/23/2015.
 */
public class ToDoItem implements Parcelable {

    private int mId;
    private String mText = "";
    private String mImagePath = "";
    private String mTime = "";
    private String mDate = "";
    private boolean mIsChecked;
    private boolean mCanDrag;
    private boolean mIsBeingEdited;

    public ToDoItem(String text){
        mText = text;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String itemText) {
        mText = itemText;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean isChecked) {
        mIsChecked = isChecked;
    }

    public boolean canDrag() {
        return mCanDrag;
    }

    public void setCanDrag(boolean canDrag) {
        mCanDrag = canDrag;
    }

    public boolean isBeingEdited() {
        return mIsBeingEdited;
    }

    public void setBeingEdited(boolean isBeingEdited) {
        mIsBeingEdited = isBeingEdited;
    }


    //Parcelable Code
    protected ToDoItem(Parcel in) {
        mId = in.readInt();
        mText = in.readString();
        mImagePath = in.readString();
        mTime = in.readString();
        mDate = in.readString();
        mIsChecked = in.readByte() != 0x00;
        mCanDrag = in.readByte() != 0x00;
        mIsBeingEdited = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mText);
        dest.writeString(mImagePath);
        dest.writeString(mTime);
        dest.writeString(mDate);
        dest.writeByte((byte) (mIsChecked ? 0x01 : 0x00));
        dest.writeByte((byte) (mCanDrag ? 0x01 : 0x00));
        dest.writeByte((byte) (mIsBeingEdited ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ToDoItem> CREATOR = new Parcelable.Creator<ToDoItem>() {
        @Override
        public ToDoItem createFromParcel(Parcel in) {
            return new ToDoItem(in);
        }

        @Override
        public ToDoItem[] newArray(int size) {
            return new ToDoItem[size];
        }
    };
}