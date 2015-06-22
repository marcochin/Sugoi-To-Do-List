package com.mcochin.todolist.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Marco on 6/5/2015.
 */
public class DialogListItem implements Parcelable {
    private String mListName;
    private boolean mIsChecked;

    public DialogListItem(String listName){
        mListName = listName;
    }

    public String getListName() {
        return mListName;
    }

    public void setListName(String listName) {
        mListName = listName;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof DialogListItem){
            if(mListName.equals(((DialogListItem) o).mListName)){
                return true;
            }
        }
        return false;
    }

    // Parcelling part
    public DialogListItem(Parcel in){
        String[] data = new String[3];

        mListName = in.readString();
        mIsChecked = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mListName);
        dest.writeByte((byte) (mIsChecked ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public DialogListItem createFromParcel(Parcel in) {
            return new DialogListItem(in);
        }

        public DialogListItem[] newArray(int size) {
            return new DialogListItem[size];
        }
    };
}
