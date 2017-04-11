package com.mercadopago.views;

import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Card;
import com.mercadopago.mvp.MvpView;

/**
 * Created by mromar on 4/11/17.
 */

public interface CustomerCardsView extends MvpView {

    void fillData();

    void showProgress();

    void hideProgress();

    void showError(MercadoPagoError error);

    void setFailureRecovery(FailureRecovery failureRecovery);

    void finishWithCardResult(Card card);

}
