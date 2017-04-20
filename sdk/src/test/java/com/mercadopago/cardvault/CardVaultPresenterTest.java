package com.mercadopago.cardvault;

import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.constants.Sites;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Site;
import com.mercadopago.mvp.OnResourcesRetrievedCallback;
import com.mercadopago.presenters.CardVaultPresenter;
import com.mercadopago.providers.CardVaultProvider;
import com.mercadopago.views.CardVaultView;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import junit.framework.Assert;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by vaserber on 4/20/17.
 */

public class CardVaultPresenterTest {

    @Test
    public void ifPublicKeyNotSetThenShowMissingPublicKeyError() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

        CardVaultPresenter presenter = new CardVaultPresenter();
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.initialize();

        assertEquals(MockedProvider.MISSING_PUBLIC_KEY, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInstallmentsEnabledNotSetThenDefaultValueIsTrue() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

//        List<Installment> installmentsList = com.mercadopago.utils.PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
//
//        provider.setResponse(installmentsList);

        CardVaultPresenter presenter = new CardVaultPresenter();
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.setPublicKey("mockedPublicKey");

        presenter.initialize();

        assertTrue(presenter.isInstallmentsEnabled());
    }

    @Test
    public void ifInstallmentsEnabledAndSiteNotSetThenShowMissingSiteError() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

//        List<Installment> installmentsList = com.mercadopago.utils.PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
//
//        provider.setResponse(installmentsList);

        CardVaultPresenter presenter = new CardVaultPresenter();
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.setPublicKey("mockedPublicKey");
        presenter.setAmount(new BigDecimal(100));

        presenter.initialize();

        assertEquals(MockedProvider.MISSING_SITE, mockedView.errorShown.getMessage());
    }

    @Test
    public void ifInstallmentsEnabledAndAmountNotSetThenShowMissingAmountError() {
        MockedView mockedView = new MockedView();
        MockedProvider provider = new MockedProvider();

//        List<Installment> installmentsList = com.mercadopago.utils.PaymentMethodSearchs.getPaymentMethodWithoutCustomOptionsMLA();
//
//        provider.setResponse(installmentsList);

        CardVaultPresenter presenter = new CardVaultPresenter();
        presenter.attachView(mockedView);
        presenter.attachResourcesProvider(provider);

        presenter.setPublicKey("mockedPublicKey");
        presenter.setSite(Sites.ARGENTINA);

        presenter.initialize();

        assertEquals(MockedProvider.MISSING_AMOUNT, mockedView.errorShown.getMessage());
    }

    private class MockedProvider implements CardVaultProvider {

        private static final String MULTIPLE_INSTALLMENTS = "multiple installments";
        private static final String MISSING_INSTALLMENTS = "missing installments";
        private static final String MISSING_PAYER_COSTS = "missing payer costs";
        private static final String MISSING_AMOUNT = "missing amount";
        private static final String MISSING_PUBLIC_KEY = "missing public key";
        private static final String MISSING_SITE = "missing site";

        private boolean shouldFail;
        private MercadoPagoError failedResponse;
        private List<Installment> successfulResponse;


        public void setResponse(MercadoPagoError exception) {
            shouldFail = true;
            failedResponse = exception;
        }

        @Override
        public String getMultipleInstallmentsForIssuerErrorMessage() {
            return MULTIPLE_INSTALLMENTS;
        }

        @Override
        public String getMissingInstallmentsForIssuerErrorMessage() {
            return MISSING_INSTALLMENTS;
        }

        @Override
        public String getMissingPayerCostsErrorMessage() {
            return MISSING_PAYER_COSTS;
        }

        @Override
        public String getMissingAmountErrorMessage() {
            return MISSING_AMOUNT;
        }

        @Override
        public String getMissingPublicKeyErrorMessage() {
            return MISSING_PUBLIC_KEY;
        }

        @Override
        public String getMissingSiteErrorMessage() {
            return MISSING_SITE;
        }

        @Override
        public void getInstallmentsAsync(String bin, Long issuerId, String paymentMethodId, BigDecimal amount, OnResourcesRetrievedCallback<List<Installment>> onResourcesRetrievedCallback) {
            if (shouldFail) {
                onResourcesRetrievedCallback.onFailure(failedResponse);
            } else {
                onResourcesRetrievedCallback.onSuccess(successfulResponse);
            }
        }
    }

    private class MockedView implements CardVaultView {

        private MercadoPagoError errorShown;
        private List<Installment> installmentsShown;
        private boolean issuerFlowStarted;
        private boolean installmentsFlowStarted;
        private boolean securityCodeFlowStarted;
        private boolean guessingFlowStarted;

        @Override
        public void askForInstallments() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForInstallmentsFromIssuers() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForInstallmentsFromNewCard() {
            installmentsFlowStarted = true;
        }

        @Override
        public void askForSecurityCodeWithoutInstallments() {
            securityCodeFlowStarted = true;
        }

        @Override
        public void askForCardInformation() {
            guessingFlowStarted = true;
        }

        @Override
        public void askForSecurityCodeFromInstallments() {
            securityCodeFlowStarted = true;
        }

        @Override
        public void askForSecurityCodeFromTokenRecovery() {
            securityCodeFlowStarted = true;
        }

        @Override
        public void startIssuersActivity() {
            issuerFlowStarted = true;
        }

        @Override
        public void showApiExceptionError(ApiException exception) {

        }

        @Override
        public void startErrorView(String message) {

        }

        @Override
        public void showProgressLayout() {

        }

        @Override
        public void showError(MercadoPagoError mercadoPagoError) {
            errorShown = mercadoPagoError;
        }

        @Override
        public void finishWithResult() {

        }

        @Override
        public void setFailureRecovery(FailureRecovery failureRecovery) {

        }

    }
}
