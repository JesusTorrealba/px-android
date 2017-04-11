package com.mercadopago.presenters;

import android.text.TextUtils;

import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.mvp.MvpPresenter;
import com.mercadopago.mvp.OnResourcesRetrievedCallback;
import com.mercadopago.providers.CustomerCardsProvider;
import com.mercadopago.views.CustomerCardsView;

import java.util.List;

/**
 * Created by mromar on 4/10/17.
 */

public class CustomerCardsPresenter extends MvpPresenter<CustomerCardsView, CustomerCardsProvider> {

    private Integer mSelectionImageDrawableResId;
    private String mCustomTitle;
    private String mSelectionConfirmPromptText;
    private String mCustomFooterMessage;
    private FailureRecovery mFailureRecovery;

    private List<Card> mCards;

    public void initialize() {
        if (mCards == null) {
            getCustomerAsync();
        } else {
            getView().fillData();
        }
    }

    private void getCustomerAsync() {
        getView().showProgress();

        getResourcesProvider().getCustomer(new OnResourcesRetrievedCallback<Customer>() {
            @Override
            public void onSuccess(Customer customer) {
                mCards = customer.getCards();
                getView().hideProgress();
                getView().fillData();
            }

            @Override
            public void onFailure(MercadoPagoError error) {
                getView().showError(error);
                getView().hideProgress();

                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        getCustomerAsync();
                    }
                });
            }
        });
    }

    public void resolveCardResponse(final Card card) {
        String lastDigitsLabel = getResourcesProvider().getLastDigitsLabel();

        if (isConfirmPromptEnabled()) {
            String dialogTitle = new StringBuilder().append(lastDigitsLabel).append(" ").append(card.getLastFourDigits()).toString();

            




            int resourceId = MercadoPagoUtil.getPaymentMethodIcon(this, card.getPaymentMethod().getId());
            if (resourceId == 0) {
                resourceId = android.R.drawable.ic_dialog_alert;
            }

            new AlertDialog.Builder(this)
                    .setIcon(resourceId)
                    .setTitle(dialogTitle)
                    .setMessage(mSelectionConfirmPromptText)
                    .setPositiveButton(getString(R.string.mpsdk_confirm_prompt_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getView().finishWithCardResult(card);
                        }

                    })
                    .setNegativeButton(getString(R.string.mpsdk_confirm_prompt_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    })
                    .show();
        } else {
            getView().finishWithCardResult(card);
        }
    }

    private boolean isConfirmPromptEnabled() {
        return !TextUtils.isEmpty(mSelectionConfirmPromptText);
    }

    public void recoverFromFailure() {
        getFailureRecovery().recover();
    }

    public List<Card> getCards() {
        return mCards;
    }

    public void setCards(List<Card> cards) {
        this.mCards = cards;
    }

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

    public void setFailureRecovery(FailureRecovery failureRecovery) {
        this.mFailureRecovery = failureRecovery;
    }

    public FailureRecovery getFailureRecovery() {
        return this.mFailureRecovery;
    }
}
