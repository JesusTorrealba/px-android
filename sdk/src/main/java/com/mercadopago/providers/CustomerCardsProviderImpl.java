package com.mercadopago.providers;

import android.content.Context;

import com.mercadopago.core.MercadoPagoServices;

import java.util.Map;

/**
 * Created by mromar on 4/11/17.
 */

public class CustomerCardsProviderImpl implements CustomerCardsProvider {

    private final Context context;
    private final MercadoPagoServices mercadoPago;
    private final String merchantBaseUrl;
    private final String merchantGetCustomerUri;
    private final Map<String, String> merchantGetCustomerAdditionalInfo;


    public CustomerCardsProviderImpl(Context context, String publicKey, String privateKey, String merchantBaseUrl, String merchantGetCustomerUri, Map<String, String> merchantGetCustomerAdditionalInfo) {
        this.context = context;
        this.merchantBaseUrl = merchantBaseUrl;
        this.merchantGetCustomerUri = merchantGetCustomerUri;
        this.merchantGetCustomerAdditionalInfo = merchantGetCustomerAdditionalInfo;

        this.mercadoPago = new MercadoPagoServices.Builder()
                .setContext(context)
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey)
                .build();
    }

    
}
