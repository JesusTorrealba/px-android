package com.mercadopago.uicontrollers.savedcards;

import com.mercadopago.model.Card;

import static com.mercadopago.util.TextUtil.isEmpty;

/**
 * Created by mromar on 4/20/17.
 */

public class CustomerCardItem {

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
