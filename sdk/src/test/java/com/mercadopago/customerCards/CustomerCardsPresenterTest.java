package com.mercadopago.customerCards;

import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.mocks.Cards;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.mvp.OnResourcesRetrievedCallback;
import com.mercadopago.presenters.CustomerCardsPresenter;
import com.mercadopago.providers.CustomerCardsProvider;
import com.mercadopago.views.CustomerCardsView;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by mromar on 4/17/17.
 */

public class CustomerCardsPresenterTest {

    @Test
    public void getCardsIfCardsIsNull() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        List<Card> cards = Cards.getCardsMLA();
        provider.setResponse(cards);

        CustomerCardsPresenter presenter = new CustomerCardsPresenter();
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.initialize();

        assertEquals(provider.successfulResponse, cards);
    }

    private class MockedProvider implements CustomerCardsProvider {

        private boolean shouldFail;
        private boolean shouldDiscountFail;
        private List<Card> successfulResponse;

        public void setResponse(List<Card> cards) {
            shouldFail = false;
            successfulResponse = cards;
        }

        @Override
        public void getCustomer(OnResourcesRetrievedCallback<Customer> onResourcesRetrievedCallback) {

        }

        @Override
        public String getLastDigitsLabel() {
            return null;
        }

        @Override
        public String getConfirmPromptYes() {
            return null;
        }

        @Override
        public String getConfirmPromptNo() {
            return null;
        }

        @Override
        public int getIconDialogAlert() {
            return 0;
        }
    }

    private class MockedView implements CustomerCardsView {

        @Override
        public void fillData() {

        }

        @Override
        public void showAlertDialog(Card card) {

        }

        @Override
        public void showProgress() {

        }

        @Override
        public void hideProgress() {

        }

        @Override
        public void showError(MercadoPagoError error) {

        }

        @Override
        public void finishWithCardResult(Card card) {

        }
    }
}
