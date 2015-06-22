package com.mcochin.todolist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcochin.todolist.R;
import com.mcochin.todolist.pojos.DialogListItem;
import com.mcochin.todolist.utils.HolderCheckUncheckUtil;

import java.util.List;

/**
 * Created by Marco on 6/4/2015.
 */
public class MyDeleteListDialogAdapter extends RecyclerView.Adapter<MyDeleteListDialogAdapter.MyDeleteListViewHolder> {
    List<DialogListItem> mDialogListItemsList;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private String mCurrentListName;

    public MyDeleteListDialogAdapter(Context context, List<DialogListItem> dialogListItemsList, String currentListName){
        mDialogListItemsList = dialogListItemsList;
        mCurrentListName = currentListName;
        mContext = context;
    }

    public interface OnItemClickListener{
        void onItemClick(MyDeleteListViewHolder holder);
    }

    public static class MyDeleteListViewHolder extends RecyclerView.ViewHolder{
        public ImageView mCheckmark;
        public TextView mListName;
        public TextView mItemNumber;
        public ViewGroup mContainer;

        public MyDeleteListViewHolder(View v) {
            super(v);
            mCheckmark = (ImageView)v.findViewById(R.id.ic_checkmark);
            mItemNumber = (TextView)v.findViewById(R.id.item_number);
            mListName = (TextView)v.findViewById(R.id.list_name);
            mContainer = (ViewGroup)v.findViewById(R.id.delete_list_container);
        }
    }

    @Override
    public MyDeleteListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_delete_list, parent, false);

        return new MyDeleteListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyDeleteListViewHolder holder, int position) {
        holder.mItemNumber.setText(position + 1 + ".");
        String listName = mDialogListItemsList.get(position).getListName();
        holder.mListName.setText(listName);

        holder.mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(holder);
                }
            }
        });

        if(mDialogListItemsList.get(position).isChecked()){
            HolderCheckUncheckUtil.showCheck(holder, true);
        }else{
            HolderCheckUncheckUtil.showCheck(holder, false);
        }

        //Highlight current list name
        if(listName.equals(mCurrentListName)){
            holder.mListName.setTextColor(mContext.getResources().getColor(R.color.dialog_current_list_highlight_color));
            holder.mItemNumber.setTextColor(mContext.getResources().getColor(R.color.dialog_current_list_highlight_color));
        } else{
            holder.mListName.setTextColor(mContext.getResources().getColor(R.color.primary_text_default_material_light));
            holder.mItemNumber.setTextColor(mContext.getResources().getColor(R.color.primary_text_default_material_light));
        }
    }

    @Override
    public int getItemCount() {
        return mDialogListItemsList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }
}
