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
import com.mcochin.todolist.adapters.MyOpenListDialogAdapter;
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
public class OpenListDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "openListDialog";
    public static final String BUNDLE_CURRENT_LIST_NAME = "currentListName";
    private static final String BUNDLE_DIALOG_LIST_ITEM_LIST = "dialogListItemsList";
    private static final String BUNDLE_DIALOG_LIST_LOAD_POSITION = "listToBeLoadedPosition";

    private RecyclerView mRecyclerView;
    private List<DialogListItem> mDialogListItemsList;
    private int mListToBeLoadedPosition = -1;

    private OnOpenListClickListener mOnOpenListClickListener;

    public interface OnOpenListClickListener {
        void onOpenListClick(String listToBeLoaded);
    }

    public interface SqliteDbRetriever{
        SQLiteDB getSQLiteDB();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_open_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String currentListName = getArguments().getString(BUNDLE_CURRENT_LIST_NAME);
        if(currentListName == null){
            throw new IllegalStateException(getActivity().getString(R.string.exception_use_new_instance));
        }

        if(savedInstanceState != null) {
            mDialogListItemsList = savedInstanceState.getParcelableArrayList(BUNDLE_DIALOG_LIST_ITEM_LIST);
            mListToBeLoadedPosition = savedInstanceState.getInt(BUNDLE_DIALOG_LIST_LOAD_POSITION);
        } else{
            mDialogListItemsList = ((DataRetriever)getActivity()).getSQLiteDB().getAllTableNames();
        }

        mRecyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new MyWrapContentLinearLayoutManager(getActivity()));
        MyOpenListDialogAdapter myOpenListDialogAdapter = new MyOpenListDialogAdapter(getActivity(), mDialogListItemsList, currentListName);
        mRecyclerView.setAdapter(myOpenListDialogAdapter);

        myOpenListDialogAdapter.setOnItemClickListener(new MyOpenListDialogAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MyOpenListDialogAdapter.MyOpenListViewHolder holder) {
                int position = mRecyclerView.getChildLayoutPosition(holder.itemView);
                DialogListItem dialogListNameItem = mDialogListItemsList.get(position);

                //Un-bullet
                if (dialogListNameItem.isChecked()) {
                    dialogListNameItem.setChecked(false);
                    HolderCheckUncheckUtil.showBullet(holder, false);

                    mListToBeLoadedPosition = -1;

                } else { //Bullet
                    dialogListNameItem.setChecked(true);
                    HolderCheckUncheckUtil.showBullet(holder, true);

                    //unbullet the last bulleted holder, sync back the prevItem
                    if (mListToBeLoadedPosition >= 0) {
                        DialogListItem previousBulletedItem = mDialogListItemsList.get(mListToBeLoadedPosition);
                        previousBulletedItem.setChecked(false);

                        MyOpenListDialogAdapter.MyOpenListViewHolder prevHolder =
                                (MyOpenListDialogAdapter.MyOpenListViewHolder) mRecyclerView.findViewHolderForLayoutPosition(mListToBeLoadedPosition);

                        HolderCheckUncheckUtil.showBullet(prevHolder, false);
                    }

                    //update the new bulleted position
                    mListToBeLoadedPosition = position;
                }
            }
        });

        //setup ClickListeners for the buttons
        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        Button buttonOpen = (Button) view.findViewById(R.id.button_open);
        buttonCancel.setOnClickListener(this);
        buttonOpen.setOnClickListener(this);

        //remove empty list msg if there are tables in db (shown by default)
        TextView mEmptyListMsg = (TextView)view.findViewById(R.id.empty_dialog_msg);
        if(!mDialogListItemsList.isEmpty()){
            mEmptyListMsg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            case R.id.button_cancel:
                break;
            case R.id.button_open:
                if(mListToBeLoadedPosition == -1){
                    Toast.makeText(getActivity(), R.string.toast_list_open_no_selection, Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    if(mOnOpenListClickListener != null) {

                        mOnOpenListClickListener.onOpenListClick(mDialogListItemsList
                                .get(mListToBeLoadedPosition).getListName());
                    }
                }
                break;
        }
        dismiss();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUNDLE_DIALOG_LIST_ITEM_LIST, (ArrayList<? extends Parcelable>) mDialogListItemsList);
        outState.putInt(BUNDLE_DIALOG_LIST_LOAD_POSITION, mListToBeLoadedPosition);
        super.onSaveInstanceState(outState);
    }

    public void setOnOpenListener(OnOpenListClickListener onOpenListClickListener){
        mOnOpenListClickListener = onOpenListClickListener;
    }

    public static OpenListDialog newInstance(String currentListName){
        OpenListDialog openListDialog = new OpenListDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_CURRENT_LIST_NAME, currentListName);
        openListDialog.setArguments(bundle);
        return openListDialog;
    }
}
