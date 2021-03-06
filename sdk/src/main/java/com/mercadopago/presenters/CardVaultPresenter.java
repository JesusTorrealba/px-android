package com.mercadopago.presenters;

import android.content.Context;

import com.mercadopago.callbacks.Callback;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.controllers.PaymentMethodGuessingController;
import com.mercadopago.core.MercadoPago;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Card;
import com.mercadopago.model.CardInfo;
import com.mercadopago.model.CardToken;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.views.CardVaultActivityView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by vaserber on 10/12/16.
 */

public class CardVaultPresenter {

    protected Context mContext;
    protected CardVaultActivityView mView;
    protected FailureRecovery mFailureRecovery;
    protected String mBin;
    protected MercadoPago mMercadoPago;

    //Activity parameters
    protected PaymentRecovery mPaymentRecovery;
    protected PaymentPreference mPaymentPreference;
    protected List<PaymentMethod> mPaymentMethodList;
    protected Site mSite;
    protected Boolean mInstallmentsEnabled;
    protected Boolean mInstallmentsReviewEnabled;
    protected String mPublicKey;
    protected BigDecimal mAmount;
    protected String mMerchantBaseUrl;
    protected String mMerchantDiscountUrl;
    protected String mMerchantGetDiscountUri;
    protected Map<String, String> mDiscountAdditionalInfo;

    //Activity result
    protected PaymentMethod mPaymentMethod;
    protected PayerCost mPayerCost;
    protected Issuer mIssuer;

    //Card Info
    protected CardInfo mCardInfo;
    protected Token mToken;
    protected CardToken mCardToken;
    protected Card mCard;

    //Discount
    protected Boolean mDiscountEnabled;
    protected Boolean mDirectDiscountEnabled;
    protected Discount mDiscount;
    protected String mPayerEmail;

    public CardVaultPresenter(Context context) {
        this.mContext = context;
    }

    public void setView(CardVaultActivityView view) {
        this.mView = view;
    }

    public void setPaymentRecovery(PaymentRecovery paymentRecovery) {
        this.mPaymentRecovery = paymentRecovery;
    }

    public void setPaymentPreference(PaymentPreference paymentPreference) {
        this.mPaymentPreference = paymentPreference;
    }

    public void setPaymentMethodList(List<PaymentMethod> paymentMethodList) {
        this.mPaymentMethodList = paymentMethodList;
    }

    public void setSite(Site site) {
        this.mSite = site;
    }

    public void setInstallmentsEnabled(Boolean installmentsEnabled) {
        this.mInstallmentsEnabled = installmentsEnabled;
    }

    public Boolean getInstallmentsEnabled() {
        return mInstallmentsEnabled;
    }

    public void setCard(Card card) {
        this.mCard = card;
    }

    public void setPublicKey(String publicKey) {
        this.mPublicKey = publicKey;
    }

    public void setAmount(BigDecimal amount) {
        this.mAmount = amount;
    }

    private void setFailureRecovery(FailureRecovery failureRecovery) {
        this.mFailureRecovery = failureRecovery;
    }

    public Issuer getIssuer() {
        return mIssuer;
    }

    public void setIssuer(Issuer mIssuer) {
        this.mIssuer = mIssuer;
    }

    public Token getToken() {
        return mToken;
    }

    public void setToken(Token mToken) {
        this.mToken = mToken;
    }

    public PaymentMethod getPaymentMethod() {
        return mPaymentMethod;
    }

    public void setPaymentMethod(PaymentMethod mPaymentMethod) {
        this.mPaymentMethod = mPaymentMethod;
    }

    public PayerCost getPayerCost() {
        return mPayerCost;
    }

    public void setPayerCost(PayerCost mPayerCost) {
        this.mPayerCost = mPayerCost;
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public PaymentRecovery getPaymentRecovery() {
        return mPaymentRecovery;
    }

    public PaymentPreference getPaymentPreference() {
        return mPaymentPreference;
    }

    public List<PaymentMethod> getPaymentMethodList() {
        return mPaymentMethodList;
    }

    public Site getSite() {
        return mSite;
    }

    public Card getCard() {
        return mCard;
    }

    public String getPublicKey() {
        return mPublicKey;
    }

    public CardToken getCardToken() {
        return mCardToken;
    }

    public void setCardToken(CardToken mCardToken) {
        this.mCardToken = mCardToken;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.mCardInfo = cardInfo;
        if (mCardInfo == null) {
            mBin = "";
        } else {
            mBin = mCardInfo.getFirstSixDigits();
        }
    }

    public void setPayerEmail(String payerEmail) {
        this.mPayerEmail = payerEmail;
    }

    public String getPayerEmail() {
        return this.mPayerEmail;
    }

    public void setDiscount(Discount discount) {
        this.mDiscount = discount;
    }

    public Discount getDiscount() {
        return mDiscount;
    }

    public void setDiscountEnabled(Boolean discountEnabled) {
        this.mDiscountEnabled = discountEnabled;
    }

    public Boolean getDiscountEnabled() {
        return this.mDiscountEnabled;
    }

    public void setDiscountAdditionalInfo(Map<String, String> discountAdditionalInfo) {
        this.mDiscountAdditionalInfo = discountAdditionalInfo;
    }

    public Map<String, String> getDiscountAdditionalInfo() {
        return this.mDiscountAdditionalInfo;
    }

    public void setInstallmentsReviewEnabled(Boolean installmentReviewEnabled) {
        this.mInstallmentsReviewEnabled = installmentReviewEnabled;
    }

    public Boolean getInstallmentsReviewEnabled() {
        return this.mInstallmentsReviewEnabled;
    }

    public CardInfo getCardInfo() {
        return mCardInfo;
    }

    public Integer getCardNumberLength() {
        return PaymentMethodGuessingController.getCardNumberLength(mPaymentMethod, mBin);
    }

    public void setMerchantBaseUrl(String merchantBaseUrl) {
        this.mMerchantBaseUrl = merchantBaseUrl;
    }

    public String getMerchantBaseUrl() {
        return this.mMerchantBaseUrl;
    }

    public void setMerchantDiscountBaseUrl(String merchantDiscountUrl) {
        this.mMerchantDiscountUrl = merchantDiscountUrl;
    }

    public String getMerchantDiscountBaseUrl() {
        return this.mMerchantDiscountUrl;
    }

    public void setMerchantGetDiscountUri(String merchantGetDiscountUri) {
        this.mMerchantGetDiscountUri = merchantGetDiscountUri;
    }

    public String getMerchantGetDiscountUri() {
        return mMerchantGetDiscountUri;
    }

    public void setDirectDiscountEnabled(Boolean directDiscountEnabled) {
        this.mDirectDiscountEnabled = directDiscountEnabled;
    }

    public Boolean getDirectDiscountEnabled() {
        return this.mDirectDiscountEnabled;
    }

    public void checkStartInstallmentsActivity() {
        if (installmentsRequired()) {
            mView.startInstallmentsActivity();
            mView.overrideTransitionHold();
        } else {
            createToken();
        }
    }

    public void checkStartIssuersActivity() {
        if (mIssuer == null) {
            mView.startIssuersActivity();
        } else {
            checkStartInstallmentsActivity();
        }
    }

    public boolean installmentsRequired() {
        return mInstallmentsEnabled;
    }

    public void validateActivityParameters() {
        if (mPublicKey == null) {
            mView.onInvalidStart("public key not set");
        } else if (mInstallmentsEnabled && (mSite == null || mAmount == null)) {
            mView.onInvalidStart("missing site or amount");
        } else {
            mView.onValidStart();
        }
    }

    public void initializeMercadoPago() {
        if (mPublicKey == null) return;
        mMercadoPago = new MercadoPago.Builder()
                .setContext(mContext)
                .setKey(mPublicKey, MercadoPago.KEY_TYPE_PUBLIC)
                .build();
    }

    public void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }

    public void createToken() {
        mMercadoPago.createToken(mCardToken, new Callback<Token>() {
            @Override
            public void success(Token token) {
                mToken = token;
                mView.finishWithResult();
            }

            @Override
            public void failure(ApiException apiException) {
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        createToken();
                    }
                });
                mView.showApiExceptionError(apiException);
            }
        });
    }
}
