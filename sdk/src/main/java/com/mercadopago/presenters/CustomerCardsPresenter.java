package com.mercadopago.presenters;

import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.providers.CustomerCardsProvider;
import com.mercadopago.views.CustomerCardsView;

/**
 * Created by mromar on 4/10/17.
 */

public class CustomerCardsPresenter extends MvpPresenter<CustomerCardsView, CustomerCardsProvider> {

    private Integer mSelectionImageDrawableResId;
    private String mCustomTitle;
    private String mSelectionConfirmPromptText;
    private String mCustomFooterMessage;



    public Integer getSelectionImageDrawableResId() {
        return mSelectionImageDrawableResId;
    }

    public void setSelectionImageDrawableResId(Integer selectionImageDrawableResId) {
        this.mSelectionImageDrawableResId = selectionImageDrawableResId;
    }

    public String getCustomTitle() {
        return mCustomTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.mCustomTitle = customTitle;
    }

    public String getSelectionConfirmPromptText() {
        return mSelectionConfirmPromptText;
    }

    public void setSelectionConfirmPromptText(String selectionConfirmPromptText) {
        this.mSelectionConfirmPromptText = selectionConfirmPromptText;
    }

    public String getCustomFooterMessage() {
        return mCustomFooterMessage;
    }

    public void setCustomFooterMessage(String customFooterMessage) {
        this.mCustomFooterMessage = customFooterMessage;
    }

}
