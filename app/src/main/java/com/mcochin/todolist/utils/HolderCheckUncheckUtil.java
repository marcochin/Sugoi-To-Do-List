package com.mcochin.todolist.utils;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mcochin.todolist.adapters.MyDeleteListDialogAdapter;
import com.mcochin.todolist.adapters.MyDragSwipeItemAdapter;
import com.mcochin.todolist.adapters.MyOpenListDialogAdapter;

/**
 * Created by Marco on 5/26/2015.
 */
public class HolderCheckUncheckUtil {

    public static void showCheckAndStrikeThru(RecyclerView.ViewHolder holder, boolean showCheckAndStrikeThru){
        if(holder instanceof MyDragSwipeItemAdapter.MyDragSwipeViewHolder) {
            MyDragSwipeItemAdapter.MyDragSwipeViewHolder dragSwipeViewHolder = (MyDragSwipeItemAdapter.MyDragSwipeViewHolder)holder;

            if (showCheckAndStrikeThru) {
                dragSwipeViewHolder.mItemText.setPaintFlags(dragSwipeViewHolder.mItemText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                dragSwipeViewHolder.mItemNumber.setVisibility(View.INVISIBLE);
                dragSwipeViewHolder.mCheckmark.setVisibility(View.VISIBLE);
            } else {
                dragSwipeViewHolder.mItemText.setPaintFlags(dragSwipeViewHolder.mItemText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                dragSwipeViewHolder.mItemNumber.setVisibility(View.VISIBLE);
                dragSwipeViewHolder.mCheckmark.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void showCheck(RecyclerView.ViewHolder holder, boolean showCheck){
        if(holder instanceof MyDeleteListDialogAdapter.MyDeleteListViewHolder) {
            MyDeleteListDialogAdapter.MyDeleteListViewHolder deleteListViewHolder = (MyDeleteListDialogAdapter.MyDeleteListViewHolder)holder;

            if (showCheck) {
                deleteListViewHolder.mItemNumber.setVisibility(View.INVISIBLE);
                deleteListViewHolder.mCheckmark.setVisibility(View.VISIBLE);
            } else {
                deleteListViewHolder.mItemNumber.setVisibility(View.VISIBLE);
                deleteListViewHolder.mCheckmark.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void showBullet(RecyclerView.ViewHolder holder, boolean showBullet){
        if(holder instanceof MyOpenListDialogAdapter.MyOpenListViewHolder) {
            MyOpenListDialogAdapter.MyOpenListViewHolder openListViewHolder = (MyOpenListDialogAdapter.MyOpenListViewHolder)holder;

            if (showBullet) {
                openListViewHolder.mItemNumber.setVisibility(View.INVISIBLE);
                openListViewHolder.mBulletpoint.setVisibility(View.VISIBLE);
            } else {
                openListViewHolder.mItemNumber.setVisibility(View.VISIBLE);
                openListViewHolder.mBulletpoint.setVisibility(View.INVISIBLE);
            }
        }
    }
}
