package com.mercadopago.providers;

import android.content.Context;

import com.mercadopago.R;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.core.CustomServer;
import com.mercadopago.core.MercadoPagoServices;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Customer;
import com.mercadopago.mvp.OnResourcesRetrievedCallback;

/**
 * Created by mromar on 4/11/17.
 */

public class CustomerCardsProviderImpl implements CustomerCardsProvider {

    private final Context context;
    private final MercadoPagoServices mercadoPago;
    private final String merchantBaseUrl;
    private final String merchantGetCustomerUri;

    public CustomerCardsProviderImpl(Context context, String publicKey, String privateKey, String merchantBaseUrl, String merchantGetCustomerUri) {
        this.context = context;
        this.merchantBaseUrl = merchantBaseUrl;
        this.merchantGetCustomerUri = merchantGetCustomerUri;

        this.mercadoPago = new MercadoPagoServices.Builder()
                .setContext(context)
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey)
                .build();
    }

    @Override
    public void getCustomer(final OnResourcesRetrievedCallback<Customer> onResourcesRetrievedCallback) {
        CustomServer.getCustomer(context, merchantBaseUrl, merchantGetCustomerUri, new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                onResourcesRetrievedCallback.onSuccess(customer);
            }

            @Override
            public void failure(ApiException apiException) {
                onResourcesRetrievedCallback.onFailure(new MercadoPagoError(apiException));
            }
        });
    }

    @Override
    public String getLastDigitsLabel() {
        return context.getString(R.string.mpsdk_last_digits_label);
    }
}
