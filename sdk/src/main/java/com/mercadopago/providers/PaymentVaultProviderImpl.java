package com.mercadopago.providers;

import android.content.Context;

import com.mercadopago.R;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.core.MerchantServer;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.mvp.OnResourcesRetrievedCallback;
import com.mercadopago.util.TextUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by mreverter on 1/30/17.
 */

public class PaymentVaultProviderImpl implements PaymentVaultProvider {
    private final Context context;
    private final MercadoPago mercadoPago;
    private final String merchantBaseUrl;
    private final String merchantDiscountUrl;
    private final String merchantGetCustomerUri;
    private final String merchantGetDiscountUri;
    private final Map<String, String> mDiscountAdditionalInfo;
    private final String merchantAccessToken;

    public PaymentVaultProviderImpl(Context context, String publicKey, String merchantBaseUrl, String merchantDiscountUrl, String merchantGetCustomerUri, String merchantGetDiscountUri,
                                    String merchantAccessToken, Map<String, String> discountAdditionalInfo) {
        this.context = context;
        this.merchantBaseUrl = merchantBaseUrl;
        this.merchantDiscountUrl = merchantDiscountUrl;
        this.merchantGetCustomerUri = merchantGetCustomerUri;
        this.merchantGetDiscountUri = merchantGetDiscountUri;
        this.mDiscountAdditionalInfo = discountAdditionalInfo;
        this.merchantAccessToken = merchantAccessToken;

        this.mercadoPago = new MercadoPago.Builder()
                .setContext(context)
                .setPublicKey(publicKey)
                .build();
    }

    @Override
    public void getDirectDiscount(String amount, String payerEmail, final OnResourcesRetrievedCallback<Discount> onResourcesRetrievedCallback) {
        if (isMerchantServerDiscountsAvailable()) {
            getMerchantDirectDiscount(amount, payerEmail, onResourcesRetrievedCallback);
        } else {
            getMPDirectDiscount(amount, payerEmail, onResourcesRetrievedCallback);
        }
    }

    private void getMPDirectDiscount(String amount, String payerEmail, final OnResourcesRetrievedCallback<Discount> onResourcesRetrievedCallback) {
        mercadoPago.getDirectDiscount(amount, payerEmail, new Callback<Discount>() {
            @Override
            public void success(Discount discount) {
                onResourcesRetrievedCallback.onSuccess(discount);
            }

            @Override
            public void failure(ApiException apiException) {
                onResourcesRetrievedCallback.onFailure(new MPException(apiException));
            }
        });
    }

    private void getMerchantDirectDiscount(String amount, String payerEmail, final OnResourcesRetrievedCallback<Discount> onResourcesRetrievedCallback) {
        String merchantDiscountUrl = getMerchantServerDiscountUrl();

        MerchantServer.getDirectDiscount(amount, payerEmail, context, merchantDiscountUrl, merchantGetDiscountUri, mDiscountAdditionalInfo, new Callback<Discount>() {
            @Override
            public void success(Discount discount) {
                onResourcesRetrievedCallback.onSuccess(discount);
            }

            @Override
            public void failure(ApiException apiException) {
                onResourcesRetrievedCallback.onFailure(new MPException(apiException));
            }
        });
    }

    @Override
    public void getPaymentMethodSearch(BigDecimal amount, final PaymentPreference paymentPreference, Payer payer, Boolean accountMoneyEnabled, final OnResourcesRetrievedCallback<PaymentMethodSearch> onResourcesRetrievedCallback) {

        List<String> excludedPaymentTypes = paymentPreference == null ? null : paymentPreference.getExcludedPaymentTypes();
        List<String> excludedPaymentMethodIds = paymentPreference == null ? null : paymentPreference.getExcludedPaymentMethodIds();
        mercadoPago.getPaymentMethodSearch(amount, excludedPaymentTypes, excludedPaymentMethodIds, payer, accountMoneyEnabled, new Callback<PaymentMethodSearch>() {

            @Override
            public void success(final PaymentMethodSearch paymentMethodSearch) {
                if (!paymentMethodSearch.hasSavedCards() && isMerchantServerCustomerAvailable()) {
                    addCustomerCardsFromMerchantServer(paymentMethodSearch, paymentPreference, onResourcesRetrievedCallback);
                } else {
                    onResourcesRetrievedCallback.onSuccess(paymentMethodSearch);
                }
            }

            @Override
            public void failure(ApiException apiException) {
                onResourcesRetrievedCallback.onFailure(new MPException(apiException));
            }
        });
    }

    private void addCustomerCardsFromMerchantServer(final PaymentMethodSearch paymentMethodSearch, final PaymentPreference paymentPreference, final OnResourcesRetrievedCallback<PaymentMethodSearch> onResourcesRetrievedCallback) {
        MerchantServer.getCustomer(context, merchantBaseUrl, merchantGetCustomerUri, merchantAccessToken, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                List<Card> savedCards = paymentPreference == null ? customer.getCards() : paymentPreference.getValidCards(customer.getCards());
                paymentMethodSearch.addCards(savedCards, context.getString(R.string.mpsdk_last_digits_label));
                onResourcesRetrievedCallback.onSuccess(paymentMethodSearch);
            }

            @Override
            public void failure(ApiException apiException) {
                //Avoid failure caused by merchant's server
                onResourcesRetrievedCallback.onSuccess(paymentMethodSearch);
            }
        });
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.mpsdk_title_activity_payment_vault);
    }

    @Override
    public String getInvalidSiteConfigurationErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_currency);
    }

    @Override
    public String getInvalidAmountErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_amount);
    }

    @Override
    public String getAllPaymentTypesExcludedErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_excluded_all_payment_type);
    }

    @Override
    public String getInvalidDefaultInstallmentsErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_default_installments);
    }

    @Override
    public String getInvalidMaxInstallmentsErrorMessage() {
        return context.getString(R.string.mpsdk_error_message_invalid_max_installments);
    }

    @Override
    public String getStandardErrorMessage() {
        return context.getString(R.string.mpsdk_standard_error_message);
    }

    @Override
    public String getEmptyPaymentMethodsErrorMessage() {
        return context.getString(R.string.mpsdk_no_payment_methods_found);
    }

    private boolean isMerchantServerCustomerAvailable() {
        return !TextUtil.isEmpty(merchantBaseUrl) && !TextUtil.isEmpty(merchantGetCustomerUri);
    }

    private boolean isMerchantServerDiscountsAvailable() {
        return !TextUtil.isEmpty(getMerchantServerDiscountUrl()) && !TextUtil.isEmpty(merchantGetDiscountUri);
    }

    private String getMerchantServerDiscountUrl() {
        String merchantBaseUrl;

        if (TextUtil.isEmpty(merchantDiscountUrl)) {
            merchantBaseUrl = this.merchantBaseUrl;
        } else {
            merchantBaseUrl = this.merchantDiscountUrl;
        }

        return merchantBaseUrl;
    }
}
