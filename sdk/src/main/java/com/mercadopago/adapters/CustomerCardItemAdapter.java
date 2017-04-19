package com.mercadopago.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.mercadopago.uicontrollers.CustomViewController;
import com.mercadopago.uicontrollers.savedcards.CustomerCardViewController;

import java.util.ArrayList;
import java.util.List;

public class CustomerCardItemAdapter extends RecyclerView.Adapter<CustomerCardItemAdapter.ViewHolder> {

    private List<CustomerCardViewController> mItems;

    public CustomerCardItemAdapter() {
        mItems = new ArrayList<>();
    }

    @Override
    public CustomerCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {

        CustomViewController item = mItems.get(position);

        item.inflateInParent(parent, false);

        return new ViewHolder(item);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
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