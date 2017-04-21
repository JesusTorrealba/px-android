package com.mercadopago.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mercadopago.R;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.util.MercadoPagoUtil;

import java.util.ArrayList;
import java.util.List;

import static com.mercadopago.util.TextUtil.isEmpty;

public class CustomerCardItemAdapter extends RecyclerView.Adapter<CustomerCardItemAdapter.ViewHolder> {

    private static final int ITEM_TYPE_CARD = 0;
    private static final int ITEM_TYPE_MESSAGE = 1;

    private List<CustomerCardItem> mItems;
    private Context mContext;

    public CustomerCardItemAdapter(Context context, List<Card> cards, String actionMessage) {
        mItems = createCustomerCardItemList(cards, actionMessage);
        mContext = context;
    }

    private List<CustomerCardItem> createCustomerCardItemList(List<Card> cards, String actionMessage) {
        List<CustomerCardItem> customerCardItems = new ArrayList<>();

        for (Card card : cards) {
            CustomerCardItem customerCardItem = new CustomerCardItem();
            customerCardItem.setCard(card);
            customerCardItems.add(customerCardItem);
        }

        if (!isEmpty(actionMessage)) {
            CustomerCardItem customerCardItem = new CustomerCardItem();
            customerCardItem.setActionMessage(actionMessage);
            customerCardItems.add(customerCardItem);
        }

        return customerCardItems;
    }

    @Override
    public CustomerCardItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == ITEM_TYPE_CARD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpsdk_row_pm_search_item, parent, true);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mpsdk_custom_text_row, parent, true);
        }

        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        int viewType;

        CustomerCardItem item = mItems.get(position);
        if (item.hasActionMessage()) {
            viewType = ITEM_TYPE_MESSAGE;
        } else {
            viewType = ITEM_TYPE_CARD;
        }

        return viewType;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        CustomerCardItem customerCardItem = mItems.get(position);

        if (customerCardItem.hasActionMessage()) {
            viewHolder.mDescription.setText(customerCardItem.getActionMessage());
        } else {
            setCardDescription(viewHolder, customerCardItem);
            setIcon(viewHolder, customerCardItem);
        }
    }

    private void setCardDescription(ViewHolder viewHolder, CustomerCardItem customerCardItem) {
        String description;

        if (!isEmpty(customerCardItem.getCard().getLastFourDigits())) {
            description = mContext.getString(R.string.mpsdk_last_digits_label) + "\n" + customerCardItem.getCard().getLastFourDigits();

            viewHolder.mDescription.setText(description);
        } else {
            viewHolder.mDescription.setVisibility(View.GONE);
        }
    }

    private void setIcon(ViewHolder viewHolder, CustomerCardItem customerCardItem) {
        String imageId;
        int resourceId = 0;

        imageId = customerCardItem.getCard().getPaymentMethod().getId();
        resourceId = MercadoPagoUtil.getPaymentMethodSearchItemIcon(mContext, imageId);

        if (resourceId != 0) {
            viewHolder.mIcon.setImageResource(resourceId);
        } else {
            viewHolder.mIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
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

        private MPTextView mDescription;
        private ImageView mIcon;

        public ViewHolder(View view) {
            super(view);
            mDescription = (MPTextView) view.findViewById(R.id.mpsdkDescription);
            mIcon = (ImageView) view.findViewById(R.id.mpsdkImage);
        }

    }

    public static class CustomerCardItem {

        private String actionMessage;
        private Card card;

        public boolean hasActionMessage() {
            return isEmpty(actionMessage);
        }

        public String getActionMessage() {
            return actionMessage;
        }

        public void setActionMessage(String actionMessage) {
            this.actionMessage = actionMessage;
        }

        public Card getCard() {
            return card;
        }

        public void setCard(Card card) {
            this.card = card;
        }
    }
}