package com.mcochin.todolist.data;

import android.database.Cursor;

import com.mcochin.todolist.pojos.ToDoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco on 5/22/2015.
 */
public class DataProvider{
    private List<ToDoItem> mToDoItemList = new ArrayList<>();
    private ToDoItem mLastRemovedItem = null;
    private int mLastRemovedPosition = -1;
    private int uniqueId = 0;

    private OnLoadListListener mOnLoadListListener;

    public DataProvider(Cursor cursor){
        loadCursor(cursor);
    }

    public interface OnLoadListListener{
        void onLoadList();
    }

    public void setDataCursor(Cursor cursor){
        loadCursor(cursor);
    }

    private void loadCursor(Cursor cursor) {
        mToDoItemList.clear();
        uniqueId = 0;

        //Read the database cursor and populate the list with ToDoItems.
        if (cursor != null){
            while (cursor.moveToNext()) {
                String itemText = cursor.getString(cursor.getColumnIndex(SQLiteDB.KEY_ITEM_TEXT));
                String imagePath = cursor.getString(cursor.getColumnIndex(SQLiteDB.KEY_ITEM_IMAGE_PATH));
                String itemDate = cursor.getString(cursor.getColumnIndex(SQLiteDB.KEY_ITEM_DATE));
                String itemTime = cursor.getString(cursor.getColumnIndex(SQLiteDB.KEY_ITEM_TIME));
                boolean isChecked = cursor.getInt(cursor.getColumnIndex(SQLiteDB.KEY_IS_CHECKED)) != 0;

                ToDoItem toDoItem = new ToDoItem(itemText);
                toDoItem.setImagePath(imagePath);
                toDoItem.setDate(itemDate);
                toDoItem.setTime(itemTime);
                toDoItem.setChecked(isChecked);
                toDoItem.setId(uniqueId);
                mToDoItemList.add(toDoItem);
                uniqueId++;
            }
            cursor.close();
        }

        if(mOnLoadListListener != null) {
            mOnLoadListListener.onLoadList();
        }
    }

    public int getCount(){
        return mToDoItemList.size();
    }

    public void addItem(ToDoItem toDoItem){
        mToDoItemList.add(toDoItem);
        //need to set id or the added item will contain text from the previous last item
        toDoItem.setId(uniqueId++);
    }

    public ToDoItem getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }
        return mToDoItemList.get(index);
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        ToDoItem toDoItem = mToDoItemList.remove(fromPosition);
        mToDoItemList.add(toPosition, toDoItem);
    }

    public void removeItem(int position) {
        mLastRemovedItem = mToDoItemList.remove(position);
        mLastRemovedPosition = position;
    }

    public int undoLastRemoveItem() {
        if (mLastRemovedItem != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mToDoItemList.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mToDoItemList.size();
            }

            mToDoItemList.add(insertedPosition, mLastRemovedItem);

            mLastRemovedItem = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public void setOnLoadListListener(OnLoadListListener onLoadListListener){
        mOnLoadListListener = onLoadListListener;
    }
}
