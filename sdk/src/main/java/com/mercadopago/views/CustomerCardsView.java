package com.mercadopago.views;

import com.mercadopago.adapters.CustomerCardItemAdapter;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Card;
import com.mercadopago.mvp.MvpView;

import java.util.List;

/**
 * Created by mromar on 4/11/17.
 */

public interface CustomerCardsView extends MvpView {

    void showCards(List<Card> cards, String actionMessage, OnSelectedCallback<CustomerCardItemAdapter.CustomerCardItem> onSelectedCallback);

    void showAlertDialog(Card card);

    void showProgress();

    void hideProgress();

    void showError(MercadoPagoError error);

    void finishWithCardResult(Card card);

}
