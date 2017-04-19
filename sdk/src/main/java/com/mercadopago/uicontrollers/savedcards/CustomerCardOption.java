package com.mercadopago.uicontrollers.savedcards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mercadopago.R;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.Card;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.util.MercadoPagoUtil;

/**
 * Created by mromar on 4/19/17.
 */

public class CustomerCardOption implements CustomerCardViewController {

        private static final int COMMENT_MAX_LENGTH = 75;
        private static final String TO_TINT_IMAGES_PREFIX = "grey_";

        protected Card mItem;
        protected Context mContext;
        protected View mView;
        protected MPTextView mDescription;
        protected MPTextView mComment;
        protected ImageView mIcon;
        protected DecorationPreference mDecorationPreference;
        protected View.OnClickListener mListener;

        public CustomerCardOption(Context context, Card item, DecorationPreference decorationPreference) {
            mContext = context;
            mItem = item;
            mDecorationPreference = decorationPreference;
        }

        public View inflateInParent(ViewGroup parent, boolean attachToRoot) {
            mView = LayoutInflater.from(mContext)
                    .inflate(R.layout.mpsdk_row_pm_search_item, parent, attachToRoot);
            if (mListener != null) {
                mView.setOnClickListener(mListener);
            }
            return mView;
        }

        @Override
        public View getView() {
            return mView;
        }

        public void initializeControls() {
            mDescription = (MPTextView) mView.findViewById(R.id.mpsdkDescription);
            mComment = (MPTextView) mView.findViewById(R.id.mpsdkComment);
            mIcon = (ImageView) mView.findViewById(R.id.mpsdkImage);
        }

        public void draw() {
            mDescription.setText("Terminada en");
            mComment.setText(mItem.getLastFourDigits());
//            if (mItem.hasDescription()) {
//                mDescription.setVisibility(View.VISIBLE);
//                mDescription.setText(mItem.getDescription());
//            } else {
//                mDescription.setVisibility(View.GONE);
//            }
//            if (mItem.hasComment() && mItem.getComment().length() < COMMENT_MAX_LENGTH) {
//                mComment.setText(mItem.getComment());
//            }
//
            int resourceId = 0;
//
//            Boolean needsTint = itemNeedsTint(mItem);
            String imageId;
//            if(needsTint) {
//                imageId = TO_TINT_IMAGES_PREFIX + mItem.getPaymentMethod().getId();//mItem.getId();
            imageId = mItem.getPaymentMethod().getId();
//            } else {
//                imageId = mItem.getId();
//            }
//
//            if (mItem.isIconRecommended()) {
                resourceId = MercadoPagoUtil.getPaymentMethodSearchItemIcon(mContext, imageId);
//            }
//
            if (resourceId != 0) {
                mIcon.setImageResource(resourceId);
            } else {
                mIcon.setVisibility(View.GONE);
            }
//
//            if(needsTint) {
//                mIcon.setColorFilter(mDecorationPreference.getBaseColor(), PorterDuff.Mode.MULTIPLY);
//            }
        }

//        private boolean itemNeedsTint(PaymentMethodSearchItem paymentMethodSearchItem) {
//
//            return mDecorationPreference != null && mDecorationPreference.hasColors()
//                    && (paymentMethodSearchItem.isGroup()
//                    || paymentMethodSearchItem.isPaymentType());
//        }

        @Override
        public void setOnClickListener(View.OnClickListener listener) {
            mListener = listener;
            if (mView != null) {
                mView.setOnClickListener(listener);
            }
        }
}
