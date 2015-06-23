package com.mcochin.todolist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.mcochin.todolist.data.DataProvider;
import com.mcochin.todolist.data.SQLiteDB;
import com.mcochin.todolist.dialogs.DeleteCurrentListDialog;
import com.mcochin.todolist.dialogs.DeleteListDialog;
import com.mcochin.todolist.dialogs.OpenListDialog;
import com.mcochin.todolist.dialogs.SaveAsListDialog;
import com.mcochin.todolist.fragments.DataFragment;
import com.mcochin.todolist.fragments.ListFragment;
import com.mcochin.todolist.interfaces.DataRetriever;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements DataRetriever,
        DataFragment.OnDataFragmentLoadedListener, DeleteCurrentListDialog.OnDeleteCurrentListClickListener,
        OpenListDialog.OnOpenListClickListener, DeleteListDialog.OnDeleteListEventListener,
        SaveAsListDialog.OnSaveAsClickListener {
    private static final String TAG = "mainActivity";
    private static final String FRAGMENT_LIST = "listFragment";
    private static final String FRAGMENT_DATA = "dataFragment";
    private static final String BUNDLE_LIST_TO_BE_LOADED = "listToBeLoaded";

    private TextView mToolbarTitle;
    //used to store the listName so onOpenList() has reference to it.
    private String mListToBeLoaded="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        //set the default title to empty because we are using our own textView for title.
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DataFragment dataFragment;

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add((dataFragment = new DataFragment()), FRAGMENT_DATA)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_main, new ListFragment(), FRAGMENT_LIST)
                    .commit();

            dataFragment.setOnDataFragmentLoadedListener(this);
        }

        if(savedInstanceState != null) {
            DeleteCurrentListDialog deleteCurrentListDialog;
            OpenListDialog openListDialog;
            DeleteListDialog deleteListDialog;
            SaveAsListDialog saveAsListDialog;
            
            mListToBeLoaded = savedInstanceState.getString(BUNDLE_LIST_TO_BE_LOADED);

            //save listeners on rotation
            if ((dataFragment = (DataFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_DATA)) != null) {
                //have to set it here too because it does not get add to the
                // FragmentManager until activity's onCreate finishes
                dataFragment.setOnDataFragmentLoadedListener(this);
            }
            if ((openListDialog = (OpenListDialog) getSupportFragmentManager().findFragmentByTag(OpenListDialog.TAG)) != null) {
                openListDialog.setOnOpenListener(this);
            }
            if ((deleteListDialog = (DeleteListDialog) getSupportFragmentManager().findFragmentByTag(DeleteListDialog.TAG)) != null) {
                deleteListDialog.setOnDeleteListListener(this);
            }
            if ((saveAsListDialog = (SaveAsListDialog) getSupportFragmentManager().findFragmentByTag(SaveAsListDialog.TAG)) != null) {
                saveAsListDialog.setOnSaveListListener(this);
            }
            if ((deleteCurrentListDialog = (DeleteCurrentListDialog) getSupportFragmentManager().findFragmentByTag(DeleteCurrentListDialog.TAG)) != null) {
                deleteCurrentListDialog.setOnDeleteCurrentListClickListener(this);
            }
        }
    }

    @Override
    public boolean onMenuOpened(final int featureId, final Menu menu) {
        super.onMenuOpened(featureId, menu);
        //return false to disable the hardware menu button
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){

            //Creating a new list
            case R.id.menu_create_new:
                if(getCurrentListName().equals(getString(R.string.app_name)) && getDataProvider().getCount() != 0){
                    DeleteCurrentListDialog deleteCurrentListDialog = DeleteCurrentListDialog.newInstance(0);
                    deleteCurrentListDialog.setOnDeleteCurrentListClickListener(this);

                    deleteCurrentListDialog.show(getSupportFragmentManager(), DeleteCurrentListDialog.TAG);
                } else {
                    createNewList();
                }
                break;

            //Opening an existing list
            case R.id.menu_open:
                OpenListDialog openListDialog = OpenListDialog.newInstance(getCurrentListName());
                openListDialog.setOnOpenListener(this);

                openListDialog.show(getSupportFragmentManager(), OpenListDialog.TAG);
                break;

            //Deleting an existing list
            case R.id.menu_delete:
                DeleteListDialog deleteListDialog = DeleteListDialog.newInstance(getCurrentListName());
                //clear the main screen if the current list is deleted
                deleteListDialog.setOnDeleteListListener(this);

                deleteListDialog.show(getSupportFragmentManager(), DeleteListDialog.TAG);
                break;

            //Saving a list
            case R.id.menu_save:
                if(!getCurrentListName().equals(getString(R.string.app_name))){
                    saveList(true);
                } else{
                    saveListAs();
                }
                break;

            //Saving a list as
            case R.id.menu_save_as:
                saveListAs();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(BUNDLE_LIST_TO_BE_LOADED, mListToBeLoaded);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onDataFragmentLoaded() {
        //set toolbar to the list that was open on exit
        //if first time it will set it to the app_name
        String listThatWasOpenOnExit = ((DataFragment)getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_DATA))
                .getListThatWasOpenOnExit();

        setCurrentListName(listThatWasOpenOnExit);
    }

    //from OpenListDialog
    @Override
    public void onOpenListClick(String listToBeLoaded) {
        if(getCurrentListName().equals(getString(R.string.app_name)) && getDataProvider().getCount() != 0){
            mListToBeLoaded = listToBeLoaded;
            DeleteCurrentListDialog deleteCurrentListDialog = DeleteCurrentListDialog.newInstance(1);
            deleteCurrentListDialog.setOnDeleteCurrentListClickListener(this);
            deleteCurrentListDialog.show(getSupportFragmentManager(),DeleteCurrentListDialog.TAG);

        } else {
            saveList(false);
            openList(listToBeLoaded);
        }
    }

    //from DeleteListDialog
    @Override
    public void onDeleteCurrentList() {
        List<String> deleteList = new ArrayList<>();
        deleteList.add(getCurrentListName());
        getSQLiteDB().deleteTable(deleteList);
        clearMainScreen();
    }

    //from DeleteCurrentListDialog
    @Override
    public void onDeleteCurrentListClick(int dialogId) {
        switch(dialogId){
            case 0:
                createNewList();
                break;
            case 1:
                openList(mListToBeLoaded);
                break;
        }
    }

    //from SaveAsListDialog
    @Override
    public void OnSaveAsClick(Context context, String savedListName) {
        //return if name already exists
        if(savedListName.equals(getString(R.string.app_name))){
            Toast.makeText(context, R.string.toast_same_as_app_name_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (getSQLiteDB().isTableExist(savedListName)) {
            Toast.makeText(context, R.string.toast_duplicate_list_name, Toast.LENGTH_LONG).show();
        } else {
            //save table if name is unique
            if (getSQLiteDB().createTable(savedListName, getDataProvider())) {
                Toast.makeText(context, R.string.toast_list_saved, Toast.LENGTH_LONG).show();
                setCurrentListName(savedListName);
            } else {
                Toast.makeText(context, R.string.toast_list_saved_error, Toast.LENGTH_LONG).show();
            }
            //dismiss the fragment
            ((DialogFragment) getSupportFragmentManager()
                    .findFragmentByTag(SaveAsListDialog.TAG)).dismiss();
        }
    }

    private void createNewList(){
        if(getSQLiteDB().saveTable(getCurrentListName(), getDataProvider())){
            Toast.makeText(this, R.string.toast_new_list_created, Toast.LENGTH_LONG).show();
            clearMainScreen();
        } else{
            Toast.makeText(this, R.string.toast_new_list_created_error, Toast.LENGTH_LONG).show();
        }
    }

    private void  openList(String listToBeLoaded){
        Cursor cursor = getSQLiteDB().getTable(listToBeLoaded);
        if(cursor != null) {
            getDataProvider().setDataCursor(cursor);
            ((ListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST)).notifyDataSetChanged();
            setCurrentListName(listToBeLoaded);
            Toast.makeText(this, R.string.toast_list_opened, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_list_opened_error, Toast.LENGTH_LONG).show();
        }
    }

    private void saveList(boolean showToast){
        if(getSQLiteDB().saveTable(getCurrentListName(), getDataProvider())){
            if(showToast) {
                Toast.makeText(this, R.string.toast_list_saved, Toast.LENGTH_LONG).show();
            }
        } else{
            if(showToast) {
                Toast.makeText(this, R.string.toast_list_saved_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveListAs(){
        SaveAsListDialog saveAsListDialog = SaveAsListDialog.newInstance();
        saveAsListDialog.setOnSaveListListener(this);
        saveAsListDialog.show(getSupportFragmentManager(), SaveAsListDialog.TAG);
    }

    private void clearMainScreen(){
        getDataProvider().setDataCursor(null);
        ((ListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_LIST)).notifyDataSetChanged();
        setCurrentListName(getString(R.string.app_name));
    }

    public String getCurrentListName() {
        return mToolbarTitle.getText().toString();
    }

    public void setCurrentListName(String listName) {
        mToolbarTitle.setText(listName);
    }

    public DataProvider getDataProvider(){
        return ((DataFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_DATA)).getDataProvider();
    }

    public SQLiteDB getSQLiteDB(){
        return ((DataFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENT_DATA)).getSQLiteDB();
    }
}
