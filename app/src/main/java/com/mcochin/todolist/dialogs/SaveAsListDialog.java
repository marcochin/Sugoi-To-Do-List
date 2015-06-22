package com.mcochin.todolist.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.mcochin.todolist.R;

/**
 * Created by Marco on 6/4/2015.
 */
public class SaveAsListDialog extends DialogFragment implements View.OnClickListener {
    public final static String TAG = "saveAsListDialog";

    private EditText mEditText;
    private OnSaveAsClickListener mOnSaveAsClickListener;

    public interface OnSaveAsClickListener {
        void OnSaveAsClick(Context context, String savedListName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_save_as, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = (EditText) view.findViewById(R.id.edit_text);

        //setup ClickListeners for the buttons
        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        Button buttonSaveAs = (Button) view.findViewById(R.id.button_save_as);
        buttonCancel.setOnClickListener(this);
        buttonSaveAs.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch(viewId){
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_save_as:
                if(mOnSaveAsClickListener != null){
                    mOnSaveAsClickListener.OnSaveAsClick(getActivity(), mEditText.getText().toString());
                }
                break;
        }
    }

    public void setOnSaveListListener(OnSaveAsClickListener onSaveAsClickListener){
        mOnSaveAsClickListener = onSaveAsClickListener;
    }

    public static SaveAsListDialog newInstance(){
        return new SaveAsListDialog();
    }
}
