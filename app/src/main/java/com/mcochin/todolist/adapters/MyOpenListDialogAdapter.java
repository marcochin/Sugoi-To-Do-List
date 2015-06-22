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
 * Created by Marco on 6/6/2015.
 */
public class MyOpenListDialogAdapter extends RecyclerView.Adapter<MyOpenListDialogAdapter.MyOpenListViewHolder> {

    List<DialogListItem> mDialogListItemsList;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;
    private String mCurrentListName;

    public MyOpenListDialogAdapter(Context context, List<DialogListItem> dialogListItemsList, String currentListName){
        mDialogListItemsList = dialogListItemsList;
        mCurrentListName = currentListName;
        mContext = context;
    }

    public interface OnItemClickListener{
        void onItemClick(MyOpenListViewHolder holder);
    }

    public static class MyOpenListViewHolder extends RecyclerView.ViewHolder{
        public ImageView mBulletpoint;
        public TextView mListName;
        public TextView mItemNumber;
        public ViewGroup mContainer;

        public MyOpenListViewHolder(View v) {
            super(v);
            mBulletpoint = (ImageView)v.findViewById(R.id.ic_bulletpoint);
            mItemNumber = (TextView)v.findViewById(R.id.item_number);
            mListName = (TextView)v.findViewById(R.id.list_name);
            mContainer = (ViewGroup)v.findViewById(R.id.delete_list_container);
        }
    }

    @Override
    public MyOpenListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_open_list, parent, false);

        return new MyOpenListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyOpenListViewHolder holder, int position) {
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
            HolderCheckUncheckUtil.showBullet(holder, true);
        }else{
            HolderCheckUncheckUtil.showBullet(holder, false);
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
