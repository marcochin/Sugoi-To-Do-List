package com.mcochin.todolist.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.mcochin.todolist.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Marco on 6/16/2015.
 */
public class SublimeDateTimeDialog extends DialogFragment {
    public static final String TAG = "sublimeDateTimeDialog";
    public static final int DATE = 0;
    public static final int TIME = 1;

    private static final String BUNDLE_DATE_OR_TIME = "dateOrTime";

    private OnTimeSetListener mOnTimeSetListener;
    private OnDateSetListener mOnDateSetListener;


    public interface OnDateSetListener{
        void onDateSet( int year, int monthOfYear, int dayOfMonth);
    }

    public interface OnTimeSetListener{
        void onTimeSet(int hourOfDay, int minute);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(STYLE_NO_TITLE);

        if(getArguments()==null){
            throw new IllegalStateException(getActivity().getString(R.string.exception_use_new_instance));
        }
        int displayOptions;
        SublimeOptions options = new SublimeOptions();

        final int dateOrTime = getArguments().getInt(BUNDLE_DATE_OR_TIME);
        if(dateOrTime == DATE) {
            displayOptions = SublimeOptions.ACTIVATE_DATE_PICKER;
            options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        } else {
            displayOptions = SublimeOptions.ACTIVATE_TIME_PICKER;
            options.setPickerToShow(SublimeOptions.Picker.TIME_PICKER);
        }
        options.setDisplayOptions(displayOptions);

        SublimePicker sublimePicker = (SublimePicker) getActivity()
                .getLayoutInflater().inflate(R.layout.sublime_picker, container);

        sublimePicker.initializePicker(options, new SublimeListenerAdapter() {
            @Override
            public void onDateTimeRecurrenceSet(SublimePicker sublimePicker, int year,
                                                int monthOfYear, int dayOfMonth, int hourOfDay, int minute,
                                                SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                                String recurrenceRule) {
                if (dateOrTime == DATE && mOnDateSetListener != null) {
                    mOnDateSetListener.onDateSet(year, monthOfYear, dayOfMonth);
                }

                else if (mOnTimeSetListener != null) {
                    mOnTimeSetListener.onTimeSet(hourOfDay, minute);
                }
                dismiss();
            }

            @Override
            public void onCancelled() {
                dismiss();
            }
        });
        return sublimePicker;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void setOnDateSetListener(OnDateSetListener onDateSetListener){
        mOnDateSetListener = onDateSetListener;
    }

    public void setOnTimeSetListener(OnTimeSetListener onTimeSetListener){
        mOnTimeSetListener = onTimeSetListener;
    }

    public static String dateTimeLocaleConversion(String template, String formatString){
        SimpleDateFormat formatDate = new SimpleDateFormat(template, Locale.getDefault());
        try {
            Date date = formatDate.parse(formatString);
            return formatDate.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "" + e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
        return null;
    }

    public static SublimeDateTimeDialog newDateInstance(){
        SublimeDateTimeDialog sublimeDateTimeDialog = new SublimeDateTimeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_DATE_OR_TIME, DATE);
        sublimeDateTimeDialog.setArguments(bundle);

        return sublimeDateTimeDialog;
    }

    public static SublimeDateTimeDialog newTimeInstance(){
        SublimeDateTimeDialog sublimeDateTimeDialog = new SublimeDateTimeDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_DATE_OR_TIME, TIME);
        sublimeDateTimeDialog.setArguments(bundle);

        return sublimeDateTimeDialog;
    }

}
