package com.mercadopago.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.model.Card;
import com.mercadopago.uicontrollers.savedcards.CustomerCardViewController;
import com.mercadopago.uicontrollers.savedcards.SavedCardView;

import java.util.ArrayList;
import java.util.List;

public class CustomerCardItemAdapter extends RecyclerView.Adapter<CustomerCardItemAdapter.ViewHolder> {

    private Context mContext;
    private List<CustomerCardViewController> mData;
    private OnSelectedCallback<Card> mSelectionCallback;
    private int mSelectionImageResId;

    public CustomerCardItemAdapter() {
        mData = new ArrayList<>();
    }

    public CustomerCardItemAdapter(Context context, List<Card> data, OnSelectedCallback<Card> callback) {
        mContext = context;
        mData = data;
        mSelectionCallback = callback;
    }

    public CustomerCardItemAdapter(Context context, List<Card> cards, OnSelectedCallback<Card> onSelectedCallback, int selectionImageResId) {
        mContext = context;
        mData = cards;
        mSelectionCallback = onSelectedCallback;
        mSelectionImageResId = selectionImageResId;
    }

    @Override
    public CustomerCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {

        Card card = mData.get(position);

        SavedCardView savedCardView = new MercadoPagoUI.Views.SavedCardViewBuilder()
                .setContext(mContext)
                .setCard(card)
                .setSelectionImage(mSelectionImageResId)
                .build();

        savedCardView.inflateInParent(parent, false);

        return new ViewHolder(savedCardView, card);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mSavedCardView.draw();
        if (position != mData.size() - 1) {
            holder.mSavedCardView.showSeparator();
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addItems(List<CustomerCardViewController> items) {
        mData.addAll(items);
    }

    public void notifyItemInserted() {
        notifyItemInserted(mData.size() - 1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Card mCard;
        private SavedCardView mSavedCardView;

        public ViewHolder(SavedCardView paymentMethodCardRow, Card card) {
            super(paymentMethodCardRow.getView());
            mCard = card;
            mSavedCardView = paymentMethodCardRow;

            mSavedCardView.initializeControls();
            mSavedCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectionCallback.onSelected(mCard);
                }
            });
        }
    }
}