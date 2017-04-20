package com.mercadopago.views;

import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Card;
import com.mercadopago.mvp.MvpView;
import com.mercadopago.uicontrollers.savedcards.CustomerCardItem;

import java.util.List;

/**
 * Created by mromar on 4/11/17.
 */

public interface CustomerCardsView extends MvpView {

    void showCards(List<Card> cards, OnSelectedCallback<CustomerCardItem> onSelectedCallback);

    void showAlertDialog(Card card);

    void showProgress();

    void hideProgress();

    void showError(MercadoPagoError error);

    void finishWithCardResult(Card card);

}
