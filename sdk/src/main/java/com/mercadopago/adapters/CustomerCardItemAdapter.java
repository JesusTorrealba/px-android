package com.mercadopago.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.mercadopago.uicontrollers.CustomViewController;
import com.mercadopago.uicontrollers.savedcards.CustomerCardViewController;

import java.util.ArrayList;
import java.util.List;

public class CustomerCardItemAdapter extends RecyclerView.Adapter<CustomerCardItemAdapter.ViewHolder> {

    public static final int ITEM_TYPE_CARD = 0;
    public static final int ITEM_TYPE_MESSAGE = 1;

    private List<CustomerCardViewController> mItems;
    private CustomerCardViewController mItem;

    public CustomerCardItemAdapter() {
        mItems = new ArrayList<>();
    }

    @Override
    public CustomerCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        mItem = mItems.get(position);
        mItem.inflateInParent(parent, false);

        return new ViewHolder(mItem);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;

        if (mItem.hasActionMessage()) {
            viewType = ITEM_TYPE_MESSAGE;
        } else {
            viewType = ITEM_TYPE_CARD;
        }

        return viewType;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CustomerCardViewController viewController = mItems.get(position);
        viewController.draw();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addItems(List<CustomerCardViewController> items) {
        mItems.addAll(items);
    }

    public void clear() {
        int size = this.mItems.size();
        this.mItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void notifyItemInserted() {
        notifyItemInserted(mItems.size() - 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CustomViewController mViewController;

        public ViewHolder(CustomViewController viewController) {
            super(viewController.getView());
            mViewController = viewController;
            mViewController.initializeControls();
        }
    }
}