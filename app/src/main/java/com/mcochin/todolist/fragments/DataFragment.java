package com.mcochin.todolist.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mcochin.todolist.MainActivity;
import com.mcochin.todolist.R;
import com.mcochin.todolist.data.DataProvider;
import com.mcochin.todolist.data.SQLiteDB;

/**
 * Created by Marco on 5/28/2015.
 */
public class DataFragment extends Fragment {
    private static final String TAG = "DataFragment";
    public static final String KEY_PREVIOUS_OPENED_LIST = "previousOpenedList";
    private static final String KEY_ABSOLUTE_FIRST_TIME = "defaultValue60552134";

    private DataProvider mDataProvider;
    private SQLiteDB mSQLiteDB;
    private SharedPreferences mSharedPrefs;
    private String mListThatWasOpenOnExit;
    private OnDataFragmentLoadedListener mOnDataFragmentLoadedListener;

    public interface OnDataFragmentLoadedListener {
        void onDataFragmentLoaded();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSQLiteDB = new SQLiteDB(getActivity());
        Cursor cursor= null;

        //load the list that was opened when the app closed
        try {
            mSharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            String listThatWasOpenOnExit = mSharedPrefs.getString(KEY_PREVIOUS_OPENED_LIST, KEY_ABSOLUTE_FIRST_TIME);

            if (listThatWasOpenOnExit != null && !listThatWasOpenOnExit.equals(KEY_ABSOLUTE_FIRST_TIME)) {
                //load last opened list from SQLite here
                cursor = mSQLiteDB.getTable(listThatWasOpenOnExit);
                mListThatWasOpenOnExit = listThatWasOpenOnExit;

            } else{
                //set to app_name if first time opening app
                mListThatWasOpenOnExit = getActivity().getString(R.string.app_name);
            }
        } catch (ClassCastException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
        }

        mDataProvider = new DataProvider(cursor);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mOnDataFragmentLoadedListener != null){
            mOnDataFragmentLoadedListener.onDataFragmentLoaded();
        }
    }

    @Override
    public void onStop() {
        mListThatWasOpenOnExit = ((MainActivity) getActivity()).getCurrentListName();

        //save previous opened list in SharedPrefs
        SharedPreferences.Editor prefsEditor = mSharedPrefs.edit();
        prefsEditor.putString(KEY_PREVIOUS_OPENED_LIST, mListThatWasOpenOnExit);
        prefsEditor.apply();

        //save the list in SQLite
        mSQLiteDB.saveTable(mListThatWasOpenOnExit, mDataProvider);

        //close db
        mSQLiteDB.closeDB();
        super.onStop();
    }


    public String getListThatWasOpenOnExit() {
        return mListThatWasOpenOnExit;
    }
    public DataProvider getDataProvider() {
        return mDataProvider;
    }
    public SQLiteDB getSQLiteDB() {
        return mSQLiteDB;
    }
    public void setOnDataFragmentLoadedListener(OnDataFragmentLoadedListener onDataFragmentLoadedListener){
        mOnDataFragmentLoadedListener = onDataFragmentLoadedListener;
    }
}
