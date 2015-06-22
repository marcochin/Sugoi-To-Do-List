package com.mcochin.todolist.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.mcochin.todolist.R;

/**
 * Created by Marco on 6/12/2015.
 */
public class AttachmentDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "attachmentDialog";
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private OnImageAttachedListener mOnImageAttachedListener;

    public interface OnImageAttachedListener {
        void onImageAttached(String imagePath);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dialog_attachment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup buttonGallery = (ViewGroup)view.findViewById(R.id.button_gallery);
        ViewGroup buttonCamera = (ViewGroup) view.findViewById(R.id.button_camera);
        Button buttonCancel = (Button) view.findViewById(R.id.button_cancel);

        buttonGallery.setOnClickListener(this);
        buttonCamera.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        Intent i;

        switch (viewID){
            case R.id.button_cancel:
                dismiss();
                break;
            case R.id.button_gallery:
                i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(i, getString(R.string.dialog_attachment_intent_window_title)), REQUEST_IMAGE_PICK);
                break;
            case R.id.button_camera:
                i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(Intent.createChooser(i, getString(R.string.dialog_attachment_intent_window_title)), REQUEST_IMAGE_CAPTURE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICK:
                case REQUEST_IMAGE_CAPTURE:
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);

                    if(cursor == null){
                        Toast.makeText(getActivity(), R.string.toast_image_attach_error, Toast.LENGTH_LONG).show();
                        return;
                    }

                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String imagePath = cursor.getString(columnIndex);
                    cursor.close();

                    //pass information on to the listener
                    if(mOnImageAttachedListener != null){
                        mOnImageAttachedListener.onImageAttached(imagePath);
                    }

                    //content://media/external/images/media/3457
                    ///storage/emulated/0/DCIM/Camera/20150610_205202.jpg
                    //Log.d(TAG, selectedImage.toString() + "\n" + imagePath);
                    Toast.makeText(getActivity(), R.string.toast_image_attached, Toast.LENGTH_LONG).show();
                    dismiss();
                    break;
            }
        }
    }

    public void setOnImageAtachedListener(OnImageAttachedListener onImageAttachedListener){
        mOnImageAttachedListener = onImageAttachedListener;
    }

    public static AttachmentDialog newInstance(){
        return new AttachmentDialog();
    }
}