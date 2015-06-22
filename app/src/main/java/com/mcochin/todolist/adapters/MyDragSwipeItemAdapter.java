package com.mcochin.todolist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.mcochin.todolist.R;
import com.mcochin.todolist.data.DataProvider;
import com.mcochin.todolist.pojos.ToDoItem;
import com.mcochin.todolist.utils.HolderCheckUncheckUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Marco on 5/21/2015.
 */
public class MyDragSwipeItemAdapter
        extends RecyclerView.Adapter<MyDragSwipeItemAdapter.MyDragSwipeViewHolder>
        implements DraggableItemAdapter<MyDragSwipeItemAdapter.MyDragSwipeViewHolder>,
        SwipeableItemAdapter<MyDragSwipeItemAdapter.MyDragSwipeViewHolder> {

    private static final String TAG = "MyDragSwipeItemAdapter";
    private Context mContext;
    private ItemEventListener mItemEventListener;
    private DataProvider mDataProvider;
    private boolean mIsOnGetItemDraggableRangeCalled;

    public MyDragSwipeItemAdapter(Context context, DataProvider dataProvider) {
        mContext = context;
        mDataProvider = dataProvider;
        // DraggableItemAdapter and SwipeableItemAdapter require stable ID, and also
        // have to implement the getItemId() method appropriately.
        // If there are no stable ids it still works properly, but creates this
        // unnecessary animation for switching items (dragging).
        setHasStableIds(true);
    }

    public interface ItemEventListener {
        void onItemLongClick(MyDragSwipeViewHolder holder);
        void onItemTouch(MyDragSwipeViewHolder holder, MotionEvent event, boolean isOnGetItemDraggableRangeCalled);
        void onItemSwipeLeft(int position);
        void onItemSwipeRight(int position);
        void onImageThumbnailTouchListener(View v, MotionEvent event, String imagePath);
    }

    public static class MyDragSwipeViewHolder extends AbstractDraggableSwipeableItemViewHolder {
        public ViewGroup mContainer;
        public TextView mItemNumber;
        public TextView mItemText;
        public TextView mItemDate;
        public TextView mItemTime;
        public ImageView mCheckmark;
        public ImageView mImageThumbnail;

        public MyDragSwipeViewHolder(View v) {
            super(v);
            mContainer = (ViewGroup) v.findViewById(R.id.swipe_container);
            mItemNumber = (TextView)v.findViewById(R.id.item_number);
            mItemText = (TextView) v.findViewById(R.id.item_text);
            mItemDate = (TextView) v.findViewById(R.id.item_date);
            mItemTime = (TextView) v.findViewById(R.id.item_time);
            mCheckmark = (ImageView) v.findViewById(R.id.ic_checkmark);
            mImageThumbnail = (ImageView) v.findViewById(R.id.image_thumbnail);
        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    @Override
    public MyDragSwipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.d(TAG, "onCreateViewHolder");
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_list, parent, false);
        return new MyDragSwipeViewHolder(v);
    }

    //Important: onBindViewHolder is called once when first creating a view for a row,
    //but also when begin drag, end drag, begin swipe, and end swipe
    // int position should not be final or else it might cause bugs when you delete
    // an item and bring it back causing 2 items be be associated with the same position
    @Override
    public void onBindViewHolder(final MyDragSwipeViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder " + position);

        ToDoItem toDoItem = mDataProvider.getItem(position);

        //Set ItemNumber and ItemText
        holder.mItemNumber.setText(position + 1 + ".");
        holder.mItemText.setText(toDoItem.getText());

        //Set Date
        String date = toDoItem.getDate();
        if(!date.isEmpty()){
            holder.mItemDate.setVisibility(View.VISIBLE);
            holder.mItemDate.setText(date);
        }else{
            holder.mItemDate.setVisibility(View.GONE);
        }

        //Set Time
        String time = toDoItem.getTime();
        if(!time.isEmpty()){
            holder.mItemTime.setVisibility(View.VISIBLE);
            holder.mItemTime.setText(time);
        }else{
            holder.mItemTime.setVisibility(View.GONE);
        }

        //Set dynamic padding for the SwipeContainer
        int paddingLeft = holder.mContainer.getPaddingLeft();
        int paddingRight = holder.mContainer.getPaddingRight();
        int paddingBottom = holder.mContainer.getPaddingBottom();

        //and also set dynamic margin for thumbnail image
        RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams)holder.mImageThumbnail.getLayoutParams();
        int imageMarginLeft = (int) mContext.getResources().getDimension(R.dimen.list_item_image_thumbnail_margin_left);

        if(holder.mItemDate.getVisibility() == View.GONE &&  holder.mItemTime.getVisibility() == View.GONE){
            holder.mContainer.setPadding(paddingLeft, paddingBottom, paddingRight, paddingBottom);
            imageParams.setMargins(imageMarginLeft, 0, 0, 0);
        }else{
            holder.mContainer.setPadding(paddingLeft, 0, paddingRight, paddingBottom);

            int imageMarginTop = (int) mContext.getResources().getDimension(R.dimen.list_item_image_thumbnail_margin_top);
            imageParams.setMargins(imageMarginLeft, imageMarginTop, 0, 0);
        }

        //Set imageThumbnail
        final String imagePath = toDoItem.getImagePath();
        if(!imagePath.isEmpty()){
            holder.mImageThumbnail.setVisibility(View.VISIBLE);

            Picasso.with(mContext).load("file://" + imagePath)
                    .fit()
                    .centerInside()
                    .into(holder.mImageThumbnail, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            holder.mImageThumbnail.setVisibility(View.GONE);
                        }
                    });

            holder.mImageThumbnail.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mItemEventListener != null) {
                        mItemEventListener.onImageThumbnailTouchListener(v, event, imagePath);
                    }
                    return false;
                }
            });

        } else{
            holder.mImageThumbnail.setVisibility(View.GONE);
        }

        //Set longClick to start drag
        holder.mContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mItemEventListener != null) {
                    mItemEventListener.onItemLongClick(holder);
                }
                return false;
            }
        });

        //Set ClickListener for the container
        holder.mContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(mItemEventListener != null) {
                    mItemEventListener.onItemTouch(holder, event, mIsOnGetItemDraggableRangeCalled);
                }

                if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mIsOnGetItemDraggableRangeCalled = false;
                }

                //can't return true or drag wont work, button selector wont work
                //But touch framework shouldn't delegate further events to this view if it returns false
                //but why does it still work?
                return false;
            }
        });

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        //final int swipeState = holder.getSwipeStateFlags();

        int bgResId;
        if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
            bgResId = R.drawable.bg_drag_item_active_state;
        } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
            bgResId = R.drawable.bg_drag_item_normal_state;
        } else if (toDoItem.isChecked()) {
            bgResId = R.drawable.bg_item_checked_state;
        } else {
            bgResId = R.drawable.bg_item_normal_state;
        }

        //make sure it stays visually checked or not on every binding
        if (toDoItem.isChecked()) {
            HolderCheckUncheckUtil.showCheckAndStrikeThru(holder, true);
        } else {
            HolderCheckUncheckUtil.showCheckAndStrikeThru(holder, false);
        }

        holder.mContainer.setBackgroundResource(bgResId);

        // set swiping properties
        // keep edit swipe pinned until dialog closes
        holder.setSwipeItemSlideAmount(
                toDoItem.isBeingEdited() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT : 0);
    }

    @Override
    public int getItemCount() {
        return mDataProvider.getCount();
    }

    @Override
    public long getItemId(int position) {
        //Log.d(TAG, "getItemId");
        return mDataProvider.getItem(position).getId();
    }

    //onCheckCanStartDrag() needs to return true before onGetItemDraggableRange() is called.
    @Override
    public boolean onCheckCanStartDrag(MyDragSwipeViewHolder holder, int position, int x, int y) {
        //Log.d(TAG, "onCheckCanStartDrag");
        return mDataProvider.getItem(position).canDrag();
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyDragSwipeViewHolder holder, int position) {
        //Log.d(TAG, "onGetItemDraggableRange");
        mIsOnGetItemDraggableRangeCalled = true;
        return null;
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        //Log.d(TAG, "onMoveItem");

        if (fromPosition == toPosition) {
            return;
        }
        mDataProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    //Enables which direction your view can swipe
    @Override
    public int onGetSwipeReactionType(MyDragSwipeViewHolder holder, int position, int x, int y) {
        //Log.d(TAG, "onGetSwipeReactionType");
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return RecyclerViewSwipeManager.REACTION_CAN_NOT_SWIPE_BOTH;
        } else{
            return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
        }
    }

    //Sets the background for when you swipe left or right
    @Override
    public void onSetSwipeBackground(MyDragSwipeViewHolder holder, int position, int swipeType) {
        //Log.d(TAG, "onSetSwipeBackground");

        int bgRes = 0;
        switch (swipeType) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }
        holder.itemView.setBackgroundResource(bgRes);
    }

    //Returns a reaction that gets sent to onPerformAfterSwipeReaction();
    //If you to return a reaction, swipe will still work, but the swipe animation will not work properly.
    @Override
    public int onSwipeItem(MyDragSwipeViewHolder holder, int position, int swipeResult) {
        //Log.d(TAG, "onSwipeItem");

        switch (swipeResult) {
            // swipe right
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(MyDragSwipeViewHolder holder, int position, int swipeResult, int swipeReaction) {
        //Log.d(TAG, "onPerformAfterSwipeReaction(result = " + swipeResult + ", reaction = " + swipeReaction + ")");

        //You can also use swipeResult as the switch variable too.
        switch (swipeReaction){
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM:
                if (mItemEventListener != null) {
                    mItemEventListener.onItemSwipeLeft(position);
                }
                break;
            case RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION:
                if (mItemEventListener != null) {
                    mItemEventListener.onItemSwipeRight(position);
                }
                break;
        }
    }

    public void setItemEventListener(ItemEventListener itemEventListener){
        mItemEventListener = itemEventListener;
    }
}