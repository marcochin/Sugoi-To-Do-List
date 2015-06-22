package com.mcochin.todolist.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcochin.todolist.R;

/**
 * Created by Marco on 6/16/2015.
 * Combined both add and edit dialogs because they both have the same layout
 * and both share almost the same code
 */
public class AddEditItemDialog extends DialogFragment implements View.OnClickListener,
        SublimeDateTimeDialog.OnTimeSetListener, SublimeDateTimeDialog.OnDateSetListener,
        AttachmentDialog.OnImageAttachedListener {

    public static final String TAG = "addEditItemDialog";
    public static final int ADD = 0;
    public static final int EDIT = 1;

    //variables for edit dialog
    private static final String BUNDLE_ITEM_TEXT = "itemText";
    private static final String BUNDLE_ITEM_IMAGE_PATH = "imagePath";
    private static final String BUNDLE_ITEM_DATE = "itemDate";
    private static final String BUNDLE_ITEM_TIME = "itemTime";
    private static final String BUNDLE_ITEM_POSITION = "itemPosition";
    public static final String BUNDLE_ADD_OR_EDIT = "addOrEdit";
    private OnEditItemEventListener mOnEditItemEventListener;

    //variables for add dialog
    private OnAddItemCLickListener mOnAddItemCLickListener;

    //shared variables between both dialogs
    private EditText mEditText;

    private int mItemPosition;
    private String mImagePath = "";
    private String mItemDate = "";
    private String mItemTime = "";

    private TextView mImageAttachedTextView;
    private TextView mDateTextView;
    private TextView mTimeTextView;

    private ImageView mXMarkDeleteAttachment;
    private ImageView mXMarkDeleteDate;
    private ImageView mXMarkDeleteTime;

    public interface OnAddItemCLickListener {
        void onAddItemClick(String addText, String imagePath, String date, String time);
    }

    public interface OnEditItemEventListener {
        void onEditItemClick(int position, String editText, String imagePath, String date, String time);
        void onEditItemDialogDismiss(int position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_add_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalStateException(getActivity().getString(R.string.exception_use_new_instance));
        }
        int addOrEdit = getArguments().getInt(BUNDLE_ADD_OR_EDIT);

        mEditText = (EditText) view.findViewById(R.id.edit_text);

        mImageAttachedTextView = (TextView)view.findViewById(R.id.image_attached_msg);
        mDateTextView = (TextView)view.findViewById(R.id.date_msg);
        mTimeTextView = (TextView)view.findViewById(R.id.time_msg);

        mXMarkDeleteAttachment = (ImageView)view.findViewById(R.id.x_mark_delete_image);
        mXMarkDeleteDate = (ImageView)view.findViewById(R.id.x_mark_delete_date);
        mXMarkDeleteTime = (ImageView)view.findViewById(R.id.x_mark_delete_time);

        ViewGroup buttonAttachment = (ViewGroup) view.findViewById(R.id.button_attachment);
        ViewGroup buttonDate = (ViewGroup) view.findViewById(R.id.button_date);
        ViewGroup buttonTime = (ViewGroup) view.findViewById(R.id.button_time);

        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);
        Button buttonAdd = (Button) view.findViewById(R.id.button_add);
        Button buttonEdit = (Button) view.findViewById(R.id.button_edit);

        buttonCancel.setOnClickListener(this);
        buttonAttachment.setOnClickListener(this);
        buttonDate.setOnClickListener(this);
        buttonTime.setOnClickListener(this);

        //Code for Add Dialog
        if(addOrEdit == ADD){
            //remove edit button
            buttonEdit.setVisibility(View.GONE);
            buttonAdd.setOnClickListener(this);
        }

        //Code for Edit Dialog
        else if(addOrEdit == EDIT){
            //remove add button
            buttonAdd.setVisibility(View.GONE);
            buttonEdit.setOnClickListener(this);

            if(savedInstanceState == null) {
                mEditText.setText(getArguments().getString(BUNDLE_ITEM_TEXT));
                mImagePath = getArguments().getString(BUNDLE_ITEM_IMAGE_PATH);
                mItemDate = getArguments().getString(BUNDLE_ITEM_DATE);
                mItemTime = getArguments().getString(BUNDLE_ITEM_TIME);
                mItemPosition = getArguments().getInt(BUNDLE_ITEM_POSITION);

                //place cursor at the end of the edit-text
                mEditText.setSelection(mEditText.getText().length());
            }
        }

        //save variables and listeners on rotation for both Add and Edit
        if(savedInstanceState != null){
            mImagePath = savedInstanceState.getString(BUNDLE_ITEM_IMAGE_PATH);
            mItemDate = savedInstanceState.getString(BUNDLE_ITEM_DATE);
            mItemTime = savedInstanceState.getString(BUNDLE_ITEM_TIME);

            if(addOrEdit == EDIT) {
                mItemPosition = savedInstanceState.getInt(BUNDLE_ITEM_POSITION);
            }

            SublimeDateTimeDialog sublimeDateTimeDialog;
            AttachmentDialog attachmentDialog;

            if((sublimeDateTimeDialog = (SublimeDateTimeDialog)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(SublimeDateTimeDialog.TAG)) != null){
                sublimeDateTimeDialog.setOnTimeSetListener(this);
                sublimeDateTimeDialog.setOnDateSetListener(this);
            }
            if((attachmentDialog = (AttachmentDialog)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(AttachmentDialog.TAG)) != null){
                attachmentDialog.setOnImageAtachedListener(this);
            }
        }

        if(addOrEdit == EDIT || savedInstanceState != null){
            if(mImagePath != null && !mImagePath.isEmpty()){
                String[] pathSegments = mImagePath.split("/");
                mImageAttachedTextView.setText(pathSegments[pathSegments.length - 1]);
                mImageAttachedTextView.setVisibility(View.VISIBLE);
                mXMarkDeleteAttachment.setVisibility(View.VISIBLE);
            }

            if(mItemDate != null && !mItemDate.isEmpty()){
                mDateTextView.setText(mItemDate);
                mDateTextView.setVisibility(View.VISIBLE);
                mXMarkDeleteDate.setVisibility(View.VISIBLE);
            }

            if(mItemTime != null && !mItemTime.isEmpty()){
                mTimeTextView.setText(mItemTime);
                mTimeTextView.setVisibility(View.VISIBLE);
                mXMarkDeleteTime.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_cancel:
                dismiss();
                break;

            case R.id.button_add:
                if(mOnAddItemCLickListener != null){
                    mOnAddItemCLickListener.onAddItemClick(mEditText.getText().toString(), mImagePath, mItemDate, mItemTime);
                }
                dismiss();
                break;

            case R.id.button_edit:
                if(mOnEditItemEventListener != null){
                    mOnEditItemEventListener.onEditItemClick(mItemPosition, mEditText.getText().toString(), mImagePath, mItemDate, mItemTime);
                }
                dismiss();
                break;

            case R.id.button_date:
                if(mDateTextView.getVisibility() == View.GONE) {
                    SublimeDateTimeDialog sublimeDateTimeDialog = SublimeDateTimeDialog.newDateInstance();
                    sublimeDateTimeDialog.setOnDateSetListener(this);
                    sublimeDateTimeDialog.show(getActivity().getSupportFragmentManager(), SublimeDateTimeDialog.TAG);
                } else{
                    //delete date
                    mItemDate = "";
                    mDateTextView.setVisibility(View.GONE);
                    mXMarkDeleteDate.setVisibility(View.GONE);
                }
                break;

            case R.id.button_time:
                if(mTimeTextView.getVisibility() == View.GONE) {
                    SublimeDateTimeDialog sublimeDateTimeDialog = SublimeDateTimeDialog.newTimeInstance();
                    sublimeDateTimeDialog.setOnTimeSetListener(this);

                    sublimeDateTimeDialog.show(getActivity().getSupportFragmentManager(), SublimeDateTimeDialog.TAG);
                } else{
                    //delete time
                    mItemTime = "";
                    mTimeTextView.setVisibility(View.GONE);
                    mXMarkDeleteTime.setVisibility(View.GONE);
                }
                break;

            case R.id.button_attachment:
                if(mImageAttachedTextView.getVisibility() == View.GONE) {
                    AttachmentDialog attachmentDialog = AttachmentDialog.newInstance();
                    attachmentDialog.setOnImageAtachedListener(this);

                    attachmentDialog.show(getActivity().getSupportFragmentManager(), AttachmentDialog.TAG);
                } else{
                    //delete image
                    mImagePath = "";
                    mImageAttachedTextView.setVisibility(View.GONE);
                    mXMarkDeleteAttachment.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        String localTime = SublimeDateTimeDialog.dateTimeLocaleConversion("h:mm", hourOfDay + ":" + minute);

        if (localTime != null) {
            mItemTime = "@ " + localTime + " " + (hourOfDay > 11 ? "pm" : "am");
            mTimeTextView.setText(mItemTime);
            mTimeTextView.setVisibility(View.VISIBLE);
            mXMarkDeleteTime.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(),R.string.toast_time_set_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
        String localDate = SublimeDateTimeDialog
                .dateTimeLocaleConversion("MM/dd/yy", monthOfYear + "/" + dayOfMonth + "/" + year);

        if(localDate != null) {
            mItemDate = localDate + " ";
            mDateTextView.setText(mItemDate);
            mDateTextView.setVisibility(View.VISIBLE);
            mXMarkDeleteDate.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(),R.string.toast_date_set_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onImageAttached(String imagePath) {
        mImagePath = imagePath;

        //show the last segment of the image path on the dialog
        String[] pathSegments = imagePath.split("/");
        mImageAttachedTextView.setText(pathSegments[pathSegments.length - 1]);
        mImageAttachedTextView.setVisibility(View.VISIBLE);
        mXMarkDeleteAttachment.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_ITEM_IMAGE_PATH, mImagePath);
        outState.putString(BUNDLE_ITEM_DATE, mItemDate);
        outState.putString(BUNDLE_ITEM_TIME, mItemTime);
        outState.putInt(BUNDLE_ITEM_POSITION, mItemPosition);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mOnEditItemEventListener != null){
            mOnEditItemEventListener.onEditItemDialogDismiss(mItemPosition);
        }
        super.onDismiss(dialog);
    }

    public void setOnAddItemListener(OnAddItemCLickListener onAddItemCLickListener){
        mOnAddItemCLickListener = onAddItemCLickListener;
    }

    public void setOnEditItemListener(OnEditItemEventListener onEditItemEventListener){
        mOnEditItemEventListener = onEditItemEventListener;
    }

    public static AddEditItemDialog newEditInstance(int position, String itemText, String imagePath, String date, String time){
        AddEditItemDialog addEditItemDialog = new AddEditItemDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ADD_OR_EDIT, EDIT);
        bundle.putInt(BUNDLE_ITEM_POSITION, position);
        bundle.putString(BUNDLE_ITEM_TEXT, itemText);
        bundle.putString(BUNDLE_ITEM_IMAGE_PATH, imagePath);
        bundle.putString(BUNDLE_ITEM_DATE, date);
        bundle.putString(BUNDLE_ITEM_TIME, time);
        addEditItemDialog.setArguments(bundle);

        return addEditItemDialog;
    }

    public static AddEditItemDialog newAddInstance(){
        AddEditItemDialog addEditItemDialog = new AddEditItemDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ADD_OR_EDIT, ADD);
        addEditItemDialog.setArguments(bundle);

        return addEditItemDialog;
    }
}
