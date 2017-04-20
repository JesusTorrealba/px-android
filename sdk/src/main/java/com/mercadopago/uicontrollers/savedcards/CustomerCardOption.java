package com.mercadopago.uicontrollers.savedcards;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mercadopago.R;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.util.MercadoPagoUtil;

/**
 * Created by mromar on 4/19/17.
 */

public class CustomerCardOption implements CustomerCardViewController {

    protected CustomerCardItem mItem;
    protected Context mContext;
    protected View mView;
    protected MPTextView mDescription;
    protected MPTextView mComment;
    protected ImageView mIcon;
    protected DecorationPreference mDecorationPreference;
    protected View.OnClickListener mListener;

    public CustomerCardOption(Context context, CustomerCardItem item, DecorationPreference decorationPreference) {
        mContext = context;
        mItem = item;
        mDecorationPreference = decorationPreference;
    }

    public View inflateInParent(ViewGroup parent, boolean attachToRoot) {

        if (viewType == 0) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.mpsdk_row_pm_search_item, parent, attachToRoot);
        } else if (viewType == 1) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.mpsdk_custom_text_row, parent, attachToRoot);
        }

        if (mListener != null) {
            mView.setOnClickListener(mListener);
        }

        return mView;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public boolean hasActionMessage() {
        return mItem.hasActionMessage();
    }

    public void initializeControls() {
        mDescription = (MPTextView) mView.findViewById(R.id.mpsdkDescription);
        mComment = (MPTextView) mView.findViewById(R.id.mpsdkComment);
        mIcon = (ImageView) mView.findViewById(R.id.mpsdkImage);
    }

    public void draw() {
        String description, imageId;
        int resourceId = 0;

        if (mItem != null && mItem.getCard().getLastFourDigits() != null) {
            description = mContext.getString(R.string.mpsdk_last_digits_label) + "\n" + mItem.getCard().getLastFourDigits();
            mDescription.setText(description);
        } else {
            mDescription.setVisibility(View.GONE);
        }

        imageId = mItem.getCard().getPaymentMethod().getId();
        resourceId = MercadoPagoUtil.getPaymentMethodSearchItemIcon(mContext, imageId);

        if (resourceId != 0) {
            mIcon.setImageResource(resourceId);
        } else {
            mIcon.setVisibility(View.GONE);
        }

        if (itemNeedsTint()) {
            mIcon.setColorFilter(mDecorationPreference.getBaseColor(), PorterDuff.Mode.MULTIPLY);
        }
    }

    private boolean itemNeedsTint() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
        if (mView != null) {
            mView.setOnClickListener(listener);
        }
    }
}
