package com.mcochin.todolist.fragments;

import android.content.Intent;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.mcochin.todolist.ImageActivity;
import com.mcochin.todolist.MainActivity;
import com.mcochin.todolist.R;
import com.mcochin.todolist.adapters.MyDragSwipeItemAdapter;
import com.mcochin.todolist.custom.MyActionButton;
import com.mcochin.todolist.data.DataProvider;
import com.mcochin.todolist.dialogs.AddEditItemDialog;
import com.mcochin.todolist.pojos.ToDoItem;
import com.mcochin.todolist.utils.HolderCheckUncheckUtil;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

/**
 * Created by Marco on 5/21/2015.
 */
public class ListFragment extends Fragment implements
        MyDragSwipeItemAdapter.ItemEventListener, View.OnClickListener,
        DataProvider.OnLoadListListener, AddEditItemDialog.OnAddItemCLickListener,
        AddEditItemDialog.OnEditItemEventListener {

    private static final String TAG = "ListFragment";
    private static final String BUNDLE_IS_SNACKBAR_SHOWN = "mIsSnackbarShown";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerViewTouchActionGuardManager mRecyclerViewTouchActionGuardManager;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;
    private RecyclerViewSwipeManager mRecyclerViewSwipeManager;
    private MyDragSwipeItemAdapter mMyCustomAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private DataProvider mDataProvider;
    private MyActionButton mFloatingActionButton;
    private View mEmptyListMsg;

    private boolean mIsSnackbarShown;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        mEmptyListMsg = view.findViewById(R.id.empty_dialog_msg);
        mFloatingActionButton = (MyActionButton)view.findViewById(R.id.floating_action_button);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mDataProvider = getDataProvider();
        mLayoutManager = new LinearLayoutManager(getActivity());

        // touch guard manager  (this class is required to suppress scrolling while swipe-dismiss animation is running)
        mRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        mRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        mRecyclerViewTouchActionGuardManager.setEnabled(true);

        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(
                (NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3));

        // swipe manager
        mRecyclerViewSwipeManager = new RecyclerViewSwipeManager();

        //initialize my custom adapter
        mMyCustomAdapter = new MyDragSwipeItemAdapter(getActivity(), mDataProvider);

        // wrap for dragging
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mMyCustomAdapter);
        // wrap for swiping
        mWrappedAdapter = mRecyclerViewSwipeManager.createWrappedAdapter(mWrappedAdapter);

        GeneralItemAnimator animator = new SwipeDismissItemAnimator();

        // Change animations are enabled by default since support-v7-recyclerview v22.
        // Disable the change animation in order to make turning back animation of swiped item works properly.
        animator.setSupportsChangeAnimations(false);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);

        // additional decorations
        //noinspection StatementWithEmptyBody
        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z1)));
        }
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(ContextCompat.getDrawable(getActivity(), R.drawable.list_divider), true));

        // NOTE:
        // The initialization order is very important! This order determines the priority of touch event handling.
        //
        // priority: TouchActionGuard > Swipe > DragAndDrop
        mRecyclerViewTouchActionGuardManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewSwipeManager.attachRecyclerView(mRecyclerView);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        mMyCustomAdapter.setItemEventListener(this);
        mFloatingActionButton.setOnClickListener(this);
        mDataProvider.setOnLoadListListener(this);
        //Since list is loaded before listener is set, call it to sync
        onLoadList();

        if(savedInstanceState != null) {
            AddEditItemDialog addEditItemDialog;

            //save listeners on rotation
            if ((addEditItemDialog = (AddEditItemDialog) getActivity()
                    .getSupportFragmentManager().findFragmentByTag(AddEditItemDialog.TAG)) != null) {
                if (addEditItemDialog.getArguments().getInt(AddEditItemDialog.BUNDLE_ADD_OR_EDIT) == AddEditItemDialog.ADD) {
                    addEditItemDialog.setOnAddItemListener(this);
                } else if (addEditItemDialog.getArguments().getInt(AddEditItemDialog.BUNDLE_ADD_OR_EDIT) == AddEditItemDialog.EDIT) {
                    addEditItemDialog.setOnEditItemListener(this);
                }
            }

            if(SnackbarManager.getCurrentSnackbar() != null &&
                    savedInstanceState.getBoolean(BUNDLE_IS_SNACKBAR_SHOWN)) {
                showSnackBar();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Snackbar snackbar = SnackbarManager.getCurrentSnackbar();
        if(snackbar != null) {
            if(snackbar.isShown()){
                mIsSnackbarShown = true;
                outState.putBoolean(BUNDLE_IS_SNACKBAR_SHOWN, mIsSnackbarShown);
            }
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        //Log.d(TAG, "onPause");
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        //Log.d(TAG, "onDestroyView");
        //Clean-up code
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerViewSwipeManager != null) {
            mRecyclerViewSwipeManager.release();
            mRecyclerViewSwipeManager = null;
        }

        if (mRecyclerViewTouchActionGuardManager != null) {
            mRecyclerViewTouchActionGuardManager.release();
            mRecyclerViewTouchActionGuardManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mMyCustomAdapter = null;
        mLayoutManager = null;

        //setting snackbar state to dismiss or it will be out of sync when you show it
        Snackbar snackbar = SnackbarManager.getCurrentSnackbar();
        if(snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    //open add item dialog if fab is clicked
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.floating_action_button) {
            AddEditItemDialog addItemDialog = AddEditItemDialog.newAddInstance();
            addItemDialog.setOnAddItemListener(this);
            addItemDialog.show(getActivity().getSupportFragmentManager(), AddEditItemDialog.TAG);
        }
    }

    @Override
    public void onAddItemClick(String addText, String imagePath, String date, String time) {
        //Configure and add item to data provider
        ToDoItem toDoItem = new ToDoItem(addText);
        toDoItem.setImagePath(imagePath);
        toDoItem.setDate(date);
        toDoItem.setTime(time);
        mDataProvider.addItem(toDoItem);

        //refresh adapter
        int position = mDataProvider.getCount() - 1;
        mMyCustomAdapter.notifyItemInserted(position);
        mRecyclerView.scrollToPosition(position);

        mEmptyListMsg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onEditItemClick(int position, String editText, String imagePath, String date, String time) {
        ToDoItem toDoItem = mDataProvider.getItem(position);
        toDoItem.setText(editText);
        toDoItem.setImagePath(imagePath);
        toDoItem.setDate(date);
        toDoItem.setTime(time);
    }

    @Override
    public void onEditItemDialogDismiss(int position) {
        if(mMyCustomAdapter != null) {
            ToDoItem toDoItem = mDataProvider.getItem(position);
            toDoItem.setBeingEdited(false);
            mMyCustomAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onItemLongClick(MyDragSwipeItemAdapter.MyDragSwipeViewHolder holder) {
        int position = mRecyclerView.getChildLayoutPosition(holder.itemView);
        ToDoItem toDoItem = mDataProvider.getItem(position);

        //find all visible viewHolders that's not the current one being dragged
        // and change the bg to bg_drag_item_normal_state
        changeAllOtherViewHolderBgsToDragNormalState(position);

        //change the bg of the one being dragged to bg_drag_item_active_state
        holder.mContainer.setBackgroundResource(R.drawable.bg_drag_item_active_state);
        toDoItem.setCanDrag(true);
    }

    @Override
    public void onImageThumbnailTouchListener(View v, MotionEvent event, String imagePath) {
        boolean isCanceled = false;

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Animation scaleDown = AnimationUtils.loadAnimation(getActivity(), R.anim.button_scale_down);
                v.startAnimation(scaleDown);
                break;
            case MotionEvent.ACTION_CANCEL:
                isCanceled = true;
            case MotionEvent.ACTION_UP:
                Animation scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.button_scale_up);
                v.startAnimation(scaleUp);

                if(!isCanceled) {
                    //Launch imageActivity
                    Intent openImageActivity = new Intent(getActivity(), ImageActivity.class);
                    openImageActivity.putExtra(ImageActivity.KEY_IMAGE_PATH, imagePath);
                    startActivity(openImageActivity);
                }
                break;
        }
    }

    @Override
    public void onItemTouch(MyDragSwipeItemAdapter.MyDragSwipeViewHolder holder, MotionEvent event, boolean isOnGetItemDraggableRangeCalled) {
        int position = mRecyclerView.getChildLayoutPosition(holder.itemView);
        ToDoItem toDoItem = mDataProvider.getItem(position);

        switch (event.getAction()) {
            //This also gets called when activating drag and releasing, but remain stationary.
            //If you move when drag is activated, this won't get called.
            case MotionEvent.ACTION_UP:
                //Log.d(TAG, "ACTION_UP");
                //If drag is activated,
                //find all visible viewHolders and change the bg to the bg before drag had activated
                if (toDoItem.canDrag()) {
                    changeAllViewHolderBgsToOriginalState();
                }

                //If drag is not activated,
                //change selectors after being checked/unchecked
                else if (!toDoItem.isChecked()) {
                    holder.mContainer.setBackgroundResource(R.drawable.bg_item_checked_state);
                    HolderCheckUncheckUtil.showCheckAndStrikeThru(holder, true);
                    toDoItem.setChecked(true);
                } else {
                    holder.mContainer.setBackgroundResource(R.drawable.bg_item_normal_state);
                    HolderCheckUncheckUtil.showCheckAndStrikeThru(holder, false);
                    toDoItem.setChecked(false);
                }

                //set drag to false so you need to perform onLongClick to drag again
                toDoItem.setCanDrag(false);
                break;

            //Gets called if you start the dragging motion
            case MotionEvent.ACTION_CANCEL:
                //Log.d(TAG, "ACTION_CANCEL");
                //For a successful drag these events occur in this order
                //OnLongClick (always called)-> onGetItemDraggableRange(sometimes called) -> ACTION_CANCEL(always called)
                //If onGetItemDraggableRange is not called, then drag was canceled by dragging to close to edge of holder.
                if(!isOnGetItemDraggableRangeCalled){
                    changeAllViewHolderBgsToOriginalState();
                }
                toDoItem.setCanDrag(false);
                break;
        }
    }

    //Remove Item
    @Override
    public void onItemSwipeLeft(int position) {
        mDataProvider.removeItem(position);
        mMyCustomAdapter.notifyItemRemoved(position);

        if(mDataProvider.getCount() == 0){
            mEmptyListMsg.setVisibility(View.VISIBLE);
        }

        showSnackBar();
    }

    //Edit Item
    @Override
    public void onItemSwipeRight(int position) {
        ToDoItem toDoItem = mDataProvider.getItem(position);
        toDoItem.setBeingEdited(true);

        AddEditItemDialog editItemDialog =
                AddEditItemDialog.newEditInstance(position, toDoItem.getText(), toDoItem.getImagePath(),
                        toDoItem.getDate(), toDoItem.getTime());

        editItemDialog.setOnEditItemListener(this);
        editItemDialog.show(getActivity().getSupportFragmentManager(), AddEditItemDialog.TAG);
    }

    @Override
    public void onLoadList() {
        if(mDataProvider.getCount() != 0){
            mEmptyListMsg.setVisibility(View.INVISIBLE);
        } else {
            mEmptyListMsg.setVisibility(View.VISIBLE);
        }
    }

    private void showSnackBar(){
        SnackbarManager.show(
                Snackbar.with(getActivity())
                        .text(R.string.snack_bar_text_item_removed)
                        .actionLabel(R.string.snack_bar_action_undo)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                int position = mDataProvider.undoLastRemoveItem();
                                mMyCustomAdapter.notifyDataSetChanged();
                                mRecyclerView.scrollToPosition(position);
                                mEmptyListMsg.setVisibility(View.INVISIBLE);
                            }
                        })
                        .colorResource(R.color.snackbar_bg_color)
                        .textColorResource(R.color.snackbar_text_color)
                        .actionColorResource(R.color.snackbar_action_color)
                        .duration(5000)
                        .type(SnackbarType.SINGLE_LINE)
                        .swipeToDismiss(true)
                        .eventListener(new EventListener() {
                            @Override
                            public void onShow(Snackbar snackbar) {
                                final float snackbarHeight = snackbar.getHeight();
                                mFloatingActionButton.translateUp(snackbarHeight, 150);
                            }

                            @Override
                            public void onShowByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onShown(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismiss(Snackbar snackbar) {
                                final float snackbarHeight = snackbar.getHeight();
                                mFloatingActionButton.translateDown(snackbarHeight, 500);
                            }

                            @Override
                            public void onDismissByReplace(Snackbar snackbar) {

                            }

                            @Override
                            public void onDismissed(Snackbar snackbar) {
                            }
                        })
        );
    }

    //return holder bgs to checked / unchecked state after drag has finished
    public void changeAllViewHolderBgsToOriginalState(){
        for (int i = mLayoutManager.findFirstVisibleItemPosition(); i <= mLayoutManager.findLastVisibleItemPosition(); i++) {
            MyDragSwipeItemAdapter.MyDragSwipeViewHolder holder =
                    (MyDragSwipeItemAdapter.MyDragSwipeViewHolder) mRecyclerView.findViewHolderForLayoutPosition(i);

            if (mDataProvider.getItem(i).isChecked()) {
                holder.mContainer.setBackgroundResource(R.drawable.bg_item_checked_state);
            } else {
                holder.mContainer.setBackgroundResource(R.drawable.bg_item_normal_state);
            }
        }
    }

    //find all visible viewHolders that's not the current one being dragged (int position)
    // and change the bg to bg_drag_item_normal_state
    public void changeAllOtherViewHolderBgsToDragNormalState(int position){
        for(int i = mLayoutManager.findFirstVisibleItemPosition(); i <= mLayoutManager.findLastVisibleItemPosition(); i++) {
            if (i != position){
                MyDragSwipeItemAdapter.MyDragSwipeViewHolder holder =
                        (MyDragSwipeItemAdapter.MyDragSwipeViewHolder) mRecyclerView.findViewHolderForLayoutPosition(i);

                holder.mContainer.setBackgroundResource(R.drawable.bg_drag_item_normal_state);
            }
        }
    }

    public void notifyDataSetChanged(){
        mMyCustomAdapter.notifyDataSetChanged();
    }

    public DataProvider getDataProvider() {
        return ((MainActivity) getActivity()).getDataProvider();
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
}
