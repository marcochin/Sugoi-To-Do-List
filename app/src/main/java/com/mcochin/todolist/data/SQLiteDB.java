package com.mcochin.todolist.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mcochin.todolist.R;
import com.mcochin.todolist.pojos.DialogListItem;
import com.mcochin.todolist.pojos.ToDoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco on 5/31/2015.
 */
public class SQLiteDB {

    private static final String TAG = "SQLiteDB";
    private static final String KEY_ROW_ID = "_id"; //primary key
    public static final String KEY_ITEM_TEXT = "itemText";
    public static final String KEY_ITEM_IMAGE_PATH = "imagePath";
    public static final String KEY_ITEM_DATE = "itemDate";
    public static final String KEY_ITEM_TIME = "itemTime";
    public static final String KEY_IS_CHECKED = "isChecked";

    private Context mContext;
    private DBHelper mDBHelper;

    public SQLiteDB(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }

    private static class DBHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "ToDoListDB";
        private static final int DATABASE_VERSION = 1;

        public DBHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //begin and endTransaction() not need in onCreate() and onUpgrade() because it is used by default
        @Override
        public void onCreate(SQLiteDatabase db) {
            //Does not have a table until user opens the app
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //onUpgrade not necessary because this is the first version of the app
        }
    }

    public boolean isTableExist(String tableName){
        Cursor cursor;
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        cursor = db.rawQuery(
                "SELECT name FROM sqlite_master " +
                        "WHERE type = 'table' " +
                        "AND name = ?", new String[]{tableName} ); //gets name of every table in db

        int cursorCount = cursor.getCount();
        cursor.close();
        //returns true if table already exists else false
        return cursorCount > 0;
    }

    public boolean createTable(String tableName, DataProvider dataProvider){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        db.beginTransaction();
        try{
            //create a table
            createTableDelegate(tableName, db, dataProvider);
            db.setTransactionSuccessful();
            //Log.d(TAG, "Table " + tableName + " created");

        }catch(SQLException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
            return false;
        } finally {
            db.endTransaction();
        }

        return true;
    }

    private void createTableDelegate(String tableName, SQLiteDatabase db, DataProvider dataProvider){
        String tableNameModified = quoteTableNameWithBackTicks(tableName);
        ContentValues cv = new ContentValues();

        //create a table
        db.execSQL("CREATE TABLE " + tableNameModified + " (" +
                KEY_ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_ITEM_TEXT + " TEXT, " +
                KEY_ITEM_IMAGE_PATH + " VARCHAR(255), " +
                KEY_ITEM_DATE + " VARCHAR(255), " +
                KEY_ITEM_TIME + " VARCHAR(255), " +
                KEY_IS_CHECKED + " INTEGER);");

        //insert events for that date in the table
        for(int i = 0; i < dataProvider.getCount(); i++){
            ToDoItem toDoItem = dataProvider.getItem(i);
            cv.put(KEY_ITEM_TEXT, toDoItem.getText());
            cv.put(KEY_ITEM_DATE, toDoItem.getDate());
            cv.put(KEY_ITEM_TIME, toDoItem.getTime());
            cv.put(KEY_ITEM_IMAGE_PATH, toDoItem.getImagePath());
            cv.put(KEY_IS_CHECKED, toDoItem.isChecked()? 1 : 0);

            db.insert(tableNameModified, null, cv);
        }
    }

    public boolean deleteTable(List<String> tableNames){
        if(tableNames.isEmpty()){
            return false;
        }

        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        String tablesDeleted = "Tables Deleted: ";

        db.beginTransaction();
        try{
            for(String tableName: tableNames){
                String tableNameModified = quoteTableNameWithBackTicks(tableName);
                db.execSQL("DROP TABLE IF EXISTS " + tableNameModified);
                tablesDeleted += tableName;
            }

            db.setTransactionSuccessful();
            //Log.d(TAG, tablesDeleted);

        }catch(SQLException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
            return false;
        } finally {
            db.endTransaction();
        }

        return true;
    }

    // delete and create
    public boolean saveTable(String tableName, DataProvider dataProvider){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.beginTransaction();

        try{
            //delete table
            String tableNameModified = quoteTableNameWithBackTicks(tableName);
            db.execSQL("DROP TABLE IF EXISTS " + tableNameModified);
            //create a table
            createTableDelegate(tableName, db, dataProvider);
            db.setTransactionSuccessful();

        }catch(SQLException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
            return false;
        } finally {
            db.endTransaction();
        }

        //Log.d(TAG, "Table " + tableName + " saved.");
        return true;
    }

    public Cursor getTable(String tableName){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        Cursor cursor = null;

        String tableNameModified = quoteTableNameWithBackTicks(tableName);

        db.beginTransaction();
        try {
            cursor = db.query(tableNameModified, new String[]{KEY_ITEM_TEXT, KEY_ITEM_IMAGE_PATH, KEY_ITEM_DATE, KEY_ITEM_TIME, KEY_IS_CHECKED}, null, null, null, null, null);

            //Log.d(TAG, "Table Read: " + tableName);
            db.setTransactionSuccessful();
        }catch(SQLException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));

            if(cursor != null) {
                cursor.close();
            }
            return null;
        } finally {
            db.endTransaction();
        }

        return cursor;
    }

    public List<DialogListItem> getAllTableNames(){
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        List<DialogListItem> tableNames = new ArrayList<>();
        Cursor cursor = null;
        String tablesRetrieved = "Tables Retrieved: ";

        db.beginTransaction();
        try{
            //gets name of every table in db, name is the variable we assign
            String appName = mContext.getString(R.string.app_name);

            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master " +
                            "WHERE type = 'table' " +
                            "AND name NOT LIKE 'sqlite_%' " +
                            "AND name NOT LIKE 'android_metadata' " +
                            "AND name NOT LIKE '" + appName + "'", null);
            //cant surround appName in back-ticks for some reason or it will crash, so using single quote
            //maybe you can't use back-ticks in a place where quote is expected.

            while(cursor.moveToNext()){
                tableNames.add(new DialogListItem(cursor.getString(cursor.getColumnIndex("name"))));
                tablesRetrieved += cursor.getString(cursor.getColumnIndex("name"));
            }

            db.setTransactionSuccessful();
            //Log.d(TAG, tablesRetrieved);

        }catch(SQLException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
        } finally {
            if(cursor != null)
                cursor.close();

            db.endTransaction();
        }

        return tableNames;
    }

    private String quoteTableNameWithBackTicks(String tableName){
        return "`" + tableName + "`";
    }

    public void closeDB(){
        mDBHelper.close();
    }
}
