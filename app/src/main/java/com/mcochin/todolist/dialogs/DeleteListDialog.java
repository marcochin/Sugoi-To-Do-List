package com.mcochin.todolist.dialogs;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mcochin.todolist.R;
import com.mcochin.todolist.adapters.MyDeleteListDialogAdapter;
import com.mcochin.todolist.custom.MyWrapContentLinearLayoutManager;
import com.mcochin.todolist.data.SQLiteDB;
import com.mcochin.todolist.interfaces.DataRetriever;
import com.mcochin.todolist.pojos.DialogListItem;
import com.mcochin.todolist.utils.HolderCheckUncheckUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marco on 6/4/2015.
 */
public class DeleteListDialog extends DialogFragment implements View.OnClickListener, DeleteCurrentListDialog.OnDeleteCurrentListClickListener {
    public static final String TAG = "deleteListDialog";
    public static final String BUNDLE_CURRENT_LIST_NAME = "currentListName";
    private static final String BUNDLE_CURRENT_LIST_SELECTED = "isCurrentListSelectedForDeletion";
    private static final String BUNDLE_DIALOG_LIST_ITEM_LIST = "dialogListItemsList";
    private static final String BUNDLE_LISTS_TO_BE_DELETED = "listsToBeDelted";

    private RecyclerView mRecyclerView;
    private MyDeleteListDialogAdapter mMyDeleteListDialogAdapter;
    private List<DialogListItem> mDialogListItemsList;
    private List<String> mListsToBeDeleted = new ArrayList<>();

    private boolean mIsCurrentListSelectedForDeletion;

    private OnDeleteListEventListener mOnDeleteEventListener;
    private SQLiteDB mSQLiteDB;


    public interface OnDeleteListEventListener {
        void onDeleteCurrentList();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_delete_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() == null){
            throw new IllegalStateException(getActivity().getString(R.string.exception_use_new_instance));
        }

        final String currentListName = getArguments().getString(BUNDLE_CURRENT_LIST_NAME);
        mSQLiteDB = ((DataRetriever)getActivity()).getSQLiteDB();

        if(savedInstanceState != null){
            mIsCurrentListSelectedForDeletion = savedInstanceState.getBoolean(BUNDLE_CURRENT_LIST_NAME);
            mDialogListItemsList = savedInstanceState.getParcelableArrayList(BUNDLE_DIALOG_LIST_ITEM_LIST);
            mListsToBeDeleted = savedInstanceState.getStringArrayList(BUNDLE_LISTS_TO_BE_DELETED);
        } else{
            mDialogListItemsList = mSQLiteDB.getAllTableNames();
        }

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new MyWrapContentLinearLayoutManager(getActivity()));
        mMyDeleteListDialogAdapter = new MyDeleteListDialogAdapter(getActivity(), mDialogListItemsList, currentListName);
        mRecyclerView.setAdapter(mMyDeleteListDialogAdapter);

        //OnClickListener for the items in the list
        mMyDeleteListDialogAdapter.setOnItemClickListener(new MyDeleteListDialogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyDeleteListDialogAdapter.MyDeleteListViewHolder holder) {
                int position = mRecyclerView.getChildLayoutPosition(holder.itemView);
                DialogListItem dialogListNameItem = mDialogListItemsList.get(position);

                //Un-check
                if(dialogListNameItem.isChecked()) {
                    dialogListNameItem.setChecked(false);
                    HolderCheckUncheckUtil.showCheck(holder, false);
                    mListsToBeDeleted.remove(dialogListNameItem.getListName());

                    if(currentListName.equals(dialogListNameItem.getListName())){
                        mIsCurrentListSelectedForDeletion = false;
                    }
                } else { //Check
                    dialogListNameItem.setChecked(true);
                    HolderCheckUncheckUtil.showCheck(holder, true);
                    mListsToBeDeleted.add(dialogListNameItem.getListName());

                    if(currentListName.equals(dialogListNameItem.getListName())){
                        mIsCurrentListSelectedForDeletion = true;
                    }
                }
            }
        });

        //setup ClickListeners for the buttons
        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        Button buttonDelete = (Button) view.findViewById(R.id.button_delete);
        buttonCancel.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);

        //remove empty list msg if there are tables in db (shown by default)
        TextView mEmptyListMsg = (TextView)view.findViewById(R.id.empty_dialog_msg);
        if(!mDialogListItemsList.isEmpty()){
            mEmptyListMsg.setVisibility(View.GONE);
        }

        //save listener on rotation
        if(savedInstanceState != null){
            DeleteCurrentListDialog deleteCurrentListDialog;

            if((deleteCurrentListDialog = (DeleteCurrentListDialog)getActivity()
                    .getSupportFragmentManager().findFragmentByTag(DeleteCurrentListDialog.TAG)) != null){

                    deleteCurrentListDialog.setOnDeleteCurrentListClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_delete:
                //are you sure you want to delete current list?
                if(mIsCurrentListSelectedForDeletion){
                    DeleteCurrentListDialog deleteCurrentListDialog = DeleteCurrentListDialog.newInstance(0);
                    deleteCurrentListDialog.setOnDeleteCurrentListClickListener(this);

                    deleteCurrentListDialog.show(getActivity()
                            .getSupportFragmentManager(), DeleteCurrentListDialog.TAG);
                } else {
                    deleteCheckedLists();
                }
                break;
        }
    }

    @Override
    public void onDeleteCurrentListClick(int dialogId) {
        deleteCheckedLists();
        mIsCurrentListSelectedForDeletion = false;

        if (mOnDeleteEventListener != null) {
            mOnDeleteEventListener.onDeleteCurrentList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_CURRENT_LIST_SELECTED, mIsCurrentListSelectedForDeletion);
        outState.putParcelableArrayList(BUNDLE_DIALOG_LIST_ITEM_LIST, (ArrayList<? extends Parcelable>) mDialogListItemsList);
        outState.putStringArrayList(BUNDLE_LISTS_TO_BE_DELETED, (ArrayList<String>)mListsToBeDeleted);

        super.onSaveInstanceState(outState);
    }

    public void deleteCheckedLists(){

        //remove the checked items
        for (String deleteItem : mListsToBeDeleted) {
                mDialogListItemsList.remove(new DialogListItem(deleteItem));
        }

        //tables deleted from db
        if(mSQLiteDB.deleteTable(mListsToBeDeleted)){
            //Toast Message
            Toast.makeText(getActivity(),
                    mListsToBeDeleted.size() == 1?
                            getString(R.string.toast_delete_list_singular):
                            getString(R.string.toast_delete_list_plural),
                    Toast.LENGTH_LONG)
                    .show();
        } else{
            Toast.makeText(getActivity(),
                    mListsToBeDeleted.size() == 1?
                            getString(R.string.toast_delete_list_singular_error):
                            getString(R.string.toast_delete_list_plural_error),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        //re-sync ui of the dialog and delete list auxiliary
        mMyDeleteListDialogAdapter.notifyDataSetChanged();
        mListsToBeDeleted.clear();

        //close dialog is nothing to delete
        if(mDialogListItemsList.isEmpty()){
            dismiss();
        }
    }

    public void setOnDeleteListListener(OnDeleteListEventListener onDeleteListEventListener){
        mOnDeleteEventListener = onDeleteListEventListener;
    }

    public static DeleteListDialog newInstance(String currentListName){
        DeleteListDialog deleteListDialog = new DeleteListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_CURRENT_LIST_NAME, currentListName);
        deleteListDialog.setArguments(bundle);
        return deleteListDialog;
    }
}
