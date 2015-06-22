package com.mcochin.todolist.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.mcochin.todolist.R;

/**
 * Created by Marco on 6/5/2015.
 */
public class DeleteCurrentListDialog extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "deleteCurrentListDialog";
    public static final String BUNDLE_DIALOG_ID = "dialogId";

    private OnDeleteCurrentListClickListener mOnDeleteCurrentListClickListener;

    public interface OnDeleteCurrentListClickListener{
        void onDeleteCurrentListClick(int dialogId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_delete_current_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getArguments() == null){
            throw new IllegalStateException(getActivity().getString(R.string.exception_use_new_instance));
        }

        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        Button buttonDelete = (Button) view.findViewById(R.id.button_delete);
        buttonCancel.setOnClickListener(this);
        buttonDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            case R.id.button_cancel:
                break;
            case R.id.button_delete:
                if(mOnDeleteCurrentListClickListener != null){
                    mOnDeleteCurrentListClickListener.onDeleteCurrentListClick(getArguments().getInt(BUNDLE_DIALOG_ID));
                }
                break;
        }
        dismiss();
    }

    public void setOnDeleteCurrentListClickListener(OnDeleteCurrentListClickListener onDeleteCurrentListClickListener){
        mOnDeleteCurrentListClickListener = onDeleteCurrentListClickListener;
    }
    public static DeleteCurrentListDialog newInstance(int dialogId){
        DeleteCurrentListDialog deleteCurrentListDialog = new DeleteCurrentListDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_DIALOG_ID, dialogId);
        deleteCurrentListDialog.setArguments(bundle);

        return deleteCurrentListDialog;
    }
}
