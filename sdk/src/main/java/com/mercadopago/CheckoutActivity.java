package com.mercadopago;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.callbacks.CallbackHolder;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.callbacks.PaymentCallback;
import com.mercadopago.callbacks.PaymentDataCallback;
import com.mercadopago.constants.PaymentMethods;
import com.mercadopago.constants.PaymentTypes;
import com.mercadopago.core.CustomServer;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.core.MercadoPagoServices;
import com.mercadopago.exceptions.CheckoutPreferenceException;
import com.mercadopago.exceptions.ExceptionHandler;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Campaign;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.model.FinancialInstitution;
import com.mercadopago.model.Identification;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.Payer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentBody;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentMethodSearch;
import com.mercadopago.model.PaymentRecovery;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.model.PaymentResultAction;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.model.TransactionDetails;
import com.mercadopago.mptracker.MPTracker;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ReviewScreenPreference;
import com.mercadopago.preferences.ServicePreference;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.CurrenciesUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;
import com.mercadopago.util.TextUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.text.TextUtils.isEmpty;

/**
 * Created by vaserber on 2/1/17.
 */

public class CheckoutActivity extends MercadoPagoBaseActivity {
    private static final String CHECKOUT_PREFERENCE_BUNDLE = "mCheckoutPreference";
    private static final String PAYMENT_METHOD_SEARCH_BUNDLE = "mPaymentMethodSearch";
    private static final String SAVED_CARDS_BUNDLE = "mSavedCards";
    private static final String MERCHANT_PUBLIC_KEY_BUNDLE = "mMerchantPublicKey";
    private static final String PAYMENT_DATA_BUNDLE = "mPaymentDataInput";
    private static final String DECORATION_PREFERENCE_BUNDLE = "mDecorationPreference";
    private static final String SERVICE_PREFERENCE_BUNDLE = "mServicePreference";
    private static final String REVIEW_SCREEN_PREFERENCE_BUNDLE = "mReviewScreenPreference";
    private static final String DISCOUNT_BUNDLE = "mDiscount";
    private static final String DISCOUNT_ENABLED_BUNDLE = "mDiscountEnabled";
    private static final String BINARY_MODE_BUNDLE = "mBinaryMode";
    private static final String MAX_SAVED_CARDS_BUNDLE = "mMaxSavedCards";
    private static final String CONGRATS_DISPLAY_BUNDLE = "mCongratsDisplay";
    private static final String RESULT_CODE_BUNDLE = "mRequestedResultCode";
    private static final String SELECTED_PAYMENT_METHOD_BUNDLE = "mSelectedPaymentMethod";
    private static final String SELECTED_ISSUER_BUNDLE = "mSelectedIssuer";
    private static final String SELECTED_PAYER_COST_BUNDLE = "mSelectedPayerCost";
    private static final String CREATED_TOKEN_BUNDLE = "mCreatedToken";
    private static final String SELECTED_DISCOUNT_BUNDLE = "mDiscount";

    //Parameters
    protected CheckoutPreference mCheckoutPreference;
    protected String mMerchantPublicKey;
    protected Integer mCongratsDisplay;

    //Local vars
    protected MercadoPagoServices mMercadoPagoServices;
    protected PaymentMethodSearch mPaymentMethodSearch;

    protected Activity mActivity;

    protected String mTransactionId;
    protected PaymentMethod mSelectedPaymentMethod;
    protected Issuer mSelectedIssuer;
    protected PayerCost mSelectedPayerCost;
    protected Token mCreatedToken;
    protected Payment mCreatedPayment;
    protected Payer mPayer;

    protected boolean mPaymentMethodEditionRequested;

    protected PaymentRecovery mPaymentRecovery;
    protected Discount mDiscount;
    protected String mCustomerId;
    protected Boolean mBinaryModeEnabled;

    protected List<Campaign> mCampaigns;
    protected List<Card> mSavedCards;
    protected DecorationPreference mDecorationPreference;
    protected ServicePreference mServicePreference;
    protected PaymentResultScreenPreference mPaymentResultScreenPreference;
    protected FlowPreference mFlowPreference;

    protected PaymentData mPaymentDataInput;
    protected PaymentResult mPaymentResultInput;
    protected FailureRecovery mFailureRecovery;
    protected ReviewScreenPreference mReviewScreenPreference;
    protected Integer mMaxSavedCards;
    protected Integer mRequestedResultCode;
    protected boolean mPaymentMethodEdited = false;

    protected Boolean mDiscountEnabled = true;
    protected Boolean mInstallmentsReviewScreenEnabled = true;
    protected Boolean mHasDirectDiscount = false;
    protected Boolean mHasCodeDiscount = false;
    protected Boolean mDirectDiscountEnabled = true;
    private TransactionDetails mTransactionDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getActivityParameters();
            if (mDecorationPreference != null) {
                mDecorationPreference.activateFont(this);
                if (mDecorationPreference.hasColors()) {
                    setTheme(R.style.Theme_MercadoPagoTheme_NoActionBar);
                }
            }
            setContentView(R.layout.mpsdk_activity_checkout);
            mActivity = this;

            try {
                validateActivityParameters();
                onValidStart();
            } catch (IllegalStateException exception) {
                onInvalidStart(exception.getMessage());
            }
        }
    }

    protected void getActivityParameters() {
        mMerchantPublicKey = this.getIntent().getStringExtra("merchantPublicKey");
        mServicePreference = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("servicePreference"), ServicePreference.class);
        mCheckoutPreference = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("checkoutPreference"), CheckoutPreference.class);
        mPayer = mCheckoutPreference.getPayer();
        mPaymentResultScreenPreference = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("paymentResultScreenPreference"), PaymentResultScreenPreference.class);
        mPaymentDataInput = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("paymentData"), PaymentData.class);
        mCongratsDisplay = this.getIntent().getIntExtra("congratsDisplay", -1);
        mBinaryModeEnabled = this.getIntent().getBooleanExtra("binaryMode", false);
        mMaxSavedCards = this.getIntent().getIntExtra("maxSavedCards", 0);
        mDiscount = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("discount"), Discount.class);

        mDirectDiscountEnabled = this.getIntent().getBooleanExtra("directDiscountEnabled", true);

        mPaymentResultInput = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("paymentResult"), PaymentResult.class);
        mDecorationPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("decorationPreference"), DecorationPreference.class);
        mReviewScreenPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("reviewScreenPreference"), ReviewScreenPreference.class);
        mFlowPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("flowPreference"), FlowPreference.class);
        mRequestedResultCode = this.getIntent().getIntExtra("resultCode", 0);

        mDiscountEnabled = isDiscountEnabled();
        mInstallmentsReviewScreenEnabled = isInstallmentsReviewScreenEnabled();
    }

    protected void onValidStart() {

        if (CallbackHolder.getInstance().hasPaymentCallback() || mRequestedResultCode == MercadoPagoCheckout.PAYMENT_RESULT_CODE) {
            CallbackHolder.getInstance().setPaymentDataCallback(new PaymentDataCallback() {
                @Override
                public void onSuccess(PaymentData paymentData, boolean paymentMethodChanged) {
                    createPayment(paymentData);
                }

                @Override
                public void onCancel() {
//                    cancelCheckout();
                }

                @Override
                public void onFailure(MercadoPagoError error) {
                    cancelFailedCheckout(error);
                }
            });
        }

        mMercadoPagoServices = new MercadoPagoServices.Builder()
                .setContext(this)
                .setPublicKey(mMerchantPublicKey)
                .setPrivateKey(mPayer.getAccessToken())
                .setServicePreference(mServicePreference)
                .build();

        showProgressBar();

        if (mCheckoutPreference.getId() != null) {
            getCheckoutPreference();
        } else {
            try {
                validatePreference();
                initializeCheckout();

            } catch (CheckoutPreferenceException e) {
                String errorMessage = ExceptionHandler.getErrorMessage(mActivity, e);
                ErrorUtil.startErrorActivity(mActivity, errorMessage, false);
            }
        }
    }

    protected void onInvalidStart(String message) {
        ErrorUtil.startErrorActivity(this, getString(R.string.mpsdk_standard_error_message), message, false);
    }

    private void getCheckoutPreference() {
        showProgressBar();

        mMercadoPagoServices.getPreference(mCheckoutPreference.getId(), new Callback<CheckoutPreference>() {
            @Override
            public void success(CheckoutPreference checkoutPreference) {
                mCheckoutPreference = checkoutPreference;
                mPayer = mCheckoutPreference.getPayer();
                try {
                    validatePreference();
                    initializeCheckout();

                } catch (CheckoutPreferenceException e) {
                    String errorMessage = ExceptionHandler.getErrorMessage(mActivity, e);
                    ErrorUtil.startErrorActivity(mActivity, errorMessage, false);
                }
            }

            @Override
            public void failure(ApiException apiException) {
                ApiUtil.showApiExceptionError(mActivity, apiException);
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        getCheckoutPreference();
                    }
                });
            }
        });
    }

    private void validatePreference() throws CheckoutPreferenceException {
        mCheckoutPreference.validate();
    }

    private void initializeCheckout() {
        getDiscountAsync();
    }

    protected void validateActivityParameters() throws IllegalStateException {
        if (isEmpty(mMerchantPublicKey)) {
            throw new IllegalStateException("public key not set");
        } else if (mCheckoutPreference == null) {
            throw new IllegalStateException("preference not set");
        }
    }

    protected void getPaymentMethodSearch() {
        showProgressBar();
        mMercadoPagoServices.getPaymentMethodSearch(mCheckoutPreference.getAmount(), mCheckoutPreference.getExcludedPaymentTypes(), mCheckoutPreference.getExcludedPaymentMethods(), mPayer, mCheckoutPreference.getSite(), new Callback<PaymentMethodSearch>() {
            @Override
            public void success(PaymentMethodSearch paymentMethodSearch) {
                mPaymentMethodSearch = paymentMethodSearch;
                if (!mPaymentMethodSearch.hasSavedCards() && isMerchantServerInfoAvailable()) {
                    getCustomerAsync();
                } else {
                    startFlow();
                }
            }

            @Override
            public void failure(ApiException apiException) {
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        getPaymentMethodSearch();
                    }
                });
                ApiUtil.showApiExceptionError(mActivity, apiException);

            }
        });
    }

    private boolean isMerchantServerInfoAvailable() {
        return mServicePreference != null && mServicePreference.hasGetCustomerURL();
    }

    private void getCustomerAsync() {
        showProgressBar();

        CustomServer.getCustomer(this, mServicePreference.getGetCustomerURL(), mServicePreference.getGetCustomerURI(), mServicePreference.getGetCustomerAdditionalInfo(), new Callback<Customer>() {
            @Override
            public void success(Customer customer) {
                if (customer != null) {
                    mCustomerId = customer.getId();
                    mSavedCards = mCheckoutPreference.getPaymentPreference() == null ? customer.getCards() : mCheckoutPreference.getPaymentPreference().getValidCards(customer.getCards());
                }
                startFlow();
            }

            @Override
            public void failure(ApiException apiException) {
                startFlow();
            }
        });
    }

    private void getDiscountAsync() {

        if (mDiscountEnabled && mDiscount == null && mServicePreference != null && mServicePreference.hasGetDiscountURL()) {
            getMerchantDirectDiscount();
        } else if (mDiscountEnabled && mDiscount == null) {
            mMercadoPagoServices.getCampaigns(new Callback<List<Campaign>>() {
                @Override
                public void success(List<Campaign> campaigns) {
                    mCampaigns = campaigns;
                    analyzeCampaigns(campaigns);
                }

                @Override
                public void failure(ApiException apiException) {
                    mDiscountEnabled = false;
                    getPaymentMethodSearch();
                }
            });
        } else {
            getPaymentMethodSearch();
        }
    }

    private void getDirectDiscount() {
        mMercadoPagoServices.getDirectDiscount(mCheckoutPreference.getAmount().toString(), mPayer.getEmail(), new Callback<Discount>() {
            @Override
            public void success(Discount discount) {
                mDiscount = discount;
                getPaymentMethodSearch();
            }

            @Override
            public void failure(ApiException apiException) {
                mDirectDiscountEnabled = false;
                if (mHasCodeDiscount) {
                    getPaymentMethodSearch();
                } else {
                    mDiscountEnabled = false;
                    getPaymentMethodSearch();
                }
            }
        });
    }

    private void getMerchantDirectDiscount() {
        Map<String, Object> discountInfoMap = new HashMap<>();
        discountInfoMap.putAll(mServicePreference.getGetDiscountAdditionalInfo());

        CustomServer.getDirectDiscount(mCheckoutPreference.getAmount().toString(), mPayer.getEmail(), this, mServicePreference.getGetMerchantDiscountBaseURL(), mServicePreference.getGetMerchantDiscountURI(), mServicePreference.getGetDiscountAdditionalInfo(), new Callback<Discount>() {
            @Override
            public void success(Discount discount) {
                mDiscount = discount;
                getPaymentMethodSearch();
            }

            @Override
            public void failure(ApiException apiException) {
                mDirectDiscountEnabled = false;
                getPaymentMethodSearch();
            }
        });
    }

    private void showProgressBar() {
        LayoutUtil.showProgressLayout(this);
    }

    private void startFlow() {
        if (mPaymentDataInput != null) {
            mSelectedIssuer = mPaymentDataInput.getIssuer();
            mSelectedPayerCost = mPaymentDataInput.getPayerCost();
            mCreatedToken = mPaymentDataInput.getToken();
            mSelectedPaymentMethod = mPaymentDataInput.getPaymentMethod();
            showReviewAndConfirm();
        } else if (mPaymentResultInput != null) {
            checkStartPaymentResultActivity(mPaymentResultInput);
        } else {
            startPaymentVaultActivity();
            overridePendingTransition(R.anim.mpsdk_fade_in_seamless, R.anim.mpsdk_fade_out_seamless);
        }

    }

    protected void startPaymentVaultActivity() {
        boolean showBankDeals = true;
        if (mFlowPreference != null && !mFlowPreference.isBankDealsEnabled()) {
            showBankDeals = false;
        }

        new MercadoPagoComponents.Activities.PaymentVaultActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPayerAccessToken(mPayer.getAccessToken())
                .setPayerEmail(mPayer.getEmail())
                .setSite(mCheckoutPreference.getSite())
                .setAmount(mCheckoutPreference.getAmount())
                .setPaymentMethodSearch(mPaymentMethodSearch)
                .setDiscount(mDiscount)
                .setInstallmentsEnabled(true)
                .setDiscountEnabled(mDiscountEnabled)
                .setDirectDiscountEnabled(mDirectDiscountEnabled)
                .setInstallmentsReviewEnabled(mInstallmentsReviewScreenEnabled)
                .setPaymentPreference(mCheckoutPreference.getPaymentPreference())
                .setDecorationPreference(mDecorationPreference)
                .setCards(mSavedCards)
                .setMaxSavedCards(mMaxSavedCards)
                .setShowBankDeals(showBankDeals)
                .startActivity();
    }

    private void analyzeCampaigns(List<Campaign> campaigns) {
        if (campaigns.size() == 0) {
            mDiscountEnabled = false;
            getPaymentMethodSearch();
        } else {
            for (Campaign campaign : campaigns) {
                if (campaign.isDirectDiscountCampaign()) {
                    mHasDirectDiscount = true;
                }

                if (campaign.isCodeDiscountCampaign()) {
                    mHasCodeDiscount = true;
                }
            }
        }

        if (mHasDirectDiscount) {
            getDirectDiscount();
        } else if (mHasCodeDiscount) {
            mDirectDiscountEnabled = false;
            getPaymentMethodSearch();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CHECKOUT_PREFERENCE_BUNDLE, JsonUtil.getInstance().toJson(mCheckoutPreference));
        outState.putString(PAYMENT_METHOD_SEARCH_BUNDLE, JsonUtil.getInstance().toJson(mPaymentMethodSearch));
        outState.putString(SAVED_CARDS_BUNDLE, JsonUtil.getInstance().toJson(mSavedCards));
        outState.putString(MERCHANT_PUBLIC_KEY_BUNDLE, mMerchantPublicKey);
        outState.putString(PAYMENT_DATA_BUNDLE, JsonUtil.getInstance().toJson(mPaymentDataInput));
        outState.putString(DECORATION_PREFERENCE_BUNDLE, JsonUtil.getInstance().toJson(mDecorationPreference));
        outState.putString(SERVICE_PREFERENCE_BUNDLE, JsonUtil.getInstance().toJson(mServicePreference));
        outState.putString(REVIEW_SCREEN_PREFERENCE_BUNDLE, JsonUtil.getInstance().toJson(mReviewScreenPreference));
        outState.putString(DISCOUNT_BUNDLE, JsonUtil.getInstance().toJson(mDiscount));
        outState.putBoolean(DISCOUNT_ENABLED_BUNDLE, mDiscountEnabled);
        outState.putBoolean(BINARY_MODE_BUNDLE, mBinaryModeEnabled);
        outState.putInt(MAX_SAVED_CARDS_BUNDLE, mMaxSavedCards);
        outState.putInt(RESULT_CODE_BUNDLE, mRequestedResultCode);
        outState.putInt(CONGRATS_DISPLAY_BUNDLE, mCongratsDisplay);

        outState.putString(SELECTED_PAYMENT_METHOD_BUNDLE, JsonUtil.getInstance().toJson(mSelectedPaymentMethod));
        outState.putString(SELECTED_ISSUER_BUNDLE, JsonUtil.getInstance().toJson(mSelectedIssuer));
        outState.putString(SELECTED_PAYER_COST_BUNDLE, JsonUtil.getInstance().toJson(mSelectedPayerCost));
        outState.putString(CREATED_TOKEN_BUNDLE, JsonUtil.getInstance().toJson(mCreatedToken));
        outState.putString(SELECTED_DISCOUNT_BUNDLE, JsonUtil.getInstance().toJson(mDiscount));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCheckoutPreference = JsonUtil.getInstance().fromJson(savedInstanceState.getString(CHECKOUT_PREFERENCE_BUNDLE), CheckoutPreference.class);
            mPaymentMethodSearch = JsonUtil.getInstance().fromJson(savedInstanceState.getString(PAYMENT_METHOD_SEARCH_BUNDLE), PaymentMethodSearch.class);
            try {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Card>>() {
                }.getType();
                mSavedCards = gson.fromJson(savedInstanceState.getString(SAVED_CARDS_BUNDLE), listType);
            } catch (Exception ex) {
                mSavedCards = null;
            }
            mMerchantPublicKey = savedInstanceState.getString(MERCHANT_PUBLIC_KEY_BUNDLE);
            mPaymentDataInput = JsonUtil.getInstance().fromJson(savedInstanceState.getString(PAYMENT_DATA_BUNDLE), PaymentData.class);
            mCongratsDisplay = savedInstanceState.getInt(CONGRATS_DISPLAY_BUNDLE, -1);
            mBinaryModeEnabled = savedInstanceState.getBoolean(BINARY_MODE_BUNDLE, false);
            mMaxSavedCards = savedInstanceState.getInt(MAX_SAVED_CARDS_BUNDLE, 0);
            mDiscount = JsonUtil.getInstance().fromJson(savedInstanceState.getString(DISCOUNT_BUNDLE), Discount.class);
            mDiscountEnabled = savedInstanceState.getBoolean(DISCOUNT_ENABLED_BUNDLE, true);
            mRequestedResultCode = savedInstanceState.getInt(RESULT_CODE_BUNDLE, 0);

            mServicePreference = JsonUtil.getInstance().fromJson(savedInstanceState.getString(SERVICE_PREFERENCE_BUNDLE), ServicePreference.class);
            mDecorationPreference = JsonUtil.getInstance().fromJson(savedInstanceState.getString(DECORATION_PREFERENCE_BUNDLE), DecorationPreference.class);
            mReviewScreenPreference = JsonUtil.getInstance().fromJson(savedInstanceState.getString(REVIEW_SCREEN_PREFERENCE_BUNDLE), ReviewScreenPreference.class);

            mSelectedPaymentMethod = JsonUtil.getInstance().fromJson(savedInstanceState.getString(SELECTED_PAYMENT_METHOD_BUNDLE), PaymentMethod.class);
            mSelectedIssuer = JsonUtil.getInstance().fromJson(savedInstanceState.getString(SELECTED_ISSUER_BUNDLE), Issuer.class);
            mSelectedPayerCost = JsonUtil.getInstance().fromJson(savedInstanceState.getString(SELECTED_PAYER_COST_BUNDLE), PayerCost.class);
            mCreatedToken = JsonUtil.getInstance().fromJson(savedInstanceState.getString(CREATED_TOKEN_BUNDLE), Token.class);
            mDiscount = JsonUtil.getInstance().fromJson(savedInstanceState.getString(SELECTED_DISCOUNT_BUNDLE), Discount.class);

        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == MercadoPagoComponents.Activities.PAYMENT_VAULT_REQUEST_CODE) {
            resolvePaymentVaultRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.PAYMENT_RESULT_REQUEST_CODE) {
            resolvePaymentResultRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.CARD_VAULT_REQUEST_CODE) {
            resolveCardVaultRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.REVIEW_AND_CONFIRM_REQUEST_CODE) {
            resolveReviewAndConfirmRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.ADDITIONAL_VAULT_REQUEST_CODE) {
            resolveAdditionalVaultRequest(resultCode, data);
        } else {
            resolveErrorRequest(resultCode, data);
        }
    }

    private void resolveAdditionalVaultRequest(int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            Identification identification = JsonUtil.getInstance().fromJson(data.getStringExtra("identification"), Identification.class);
            FinancialInstitution financialInstitution = JsonUtil.getInstance().fromJson(data.getStringExtra("financialInstitution"), FinancialInstitution.class);
            String entityType = data.getStringExtra("entityType");

            if(identification!= null){
                mPayer.setIdentification(identification);
            }
            if(entityType!= null && !entityType.equals("")){
                mPayer.setEntityType(entityType);
            }
            if(financialInstitution!= null){
                mTransactionDetails = new TransactionDetails();
                mTransactionDetails.setFinancialInstitution(financialInstitution.getId().toString());
            }


            if (isReviewAndConfirmEnabled()) {
                showReviewAndConfirm();

            } else {
                resolvePaymentDataCallback();
            }
        } else if (resultCode == RESULT_CANCELED && isUniquePaymentMethod()) {
            cancelCheckout();
        } else if (resultCode == RESULT_CANCELED) {
            mPaymentMethodEdited = true;
            animateBackToPaymentMethodSelection();
            startPaymentVaultActivity();
        } else {
            animateBackToPaymentMethodSelection();
            startPaymentVaultActivity();
        }

    }

    private void resolveReviewAndConfirmRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            resolvePaymentDataCallback();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CHANGE_PAYMENT_METHOD) {
            changePaymentMethod();
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_CANCEL_PAYMENT) {
            if (data != null && data.getIntExtra("resultCode", 0) != 0) {
                Integer customResultCode = data.getIntExtra("resultCode", 0);
                PaymentData paymentData = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentData"), PaymentData.class);
                cancelCheckout(customResultCode, paymentData);
            } else {
                cancelCheckout();
            }
        } else if (resultCode == ReviewAndConfirmActivity.RESULT_SILENT_CANCEL_PAYMENT) {
            setResult(RESULT_CANCELED);
            this.finish();
        } else if (resultCode == RESULT_CANCELED && isUniquePaymentMethod()) {
            cancelCheckout();
        } else if (resultCode == RESULT_CANCELED) {
            mPaymentMethodEdited = true;
//            mPaymentMethodEditionRequested = true;
            animateBackToPaymentMethodSelection();
            startPaymentVaultActivity();
        } else {
            animateBackToPaymentMethodSelection();
            startPaymentVaultActivity();
        }
    }

    private void resolvePaymentDataCallback() {
        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(mSelectedPaymentMethod);
        paymentData.setIssuer(mSelectedIssuer);
        paymentData.setToken(mCreatedToken);
        paymentData.setPayerCost(mSelectedPayerCost);
        paymentData.setDiscount(mDiscount);
        //TODO setear los datos que faltan de mPayer y transactionDetails
        paymentData.setPayer(mPayer);
        paymentData.setTransactionDetails(mTransactionDetails);
        boolean hasToFinishActivity = false;
        if (MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE.equals(mRequestedResultCode)
                || (CallbackHolder.getInstance().hasPaymentDataCallback()
                && !CallbackHolder.getInstance().hasPaymentCallback()
                && !MercadoPagoCheckout.PAYMENT_RESULT_CODE.equals(mRequestedResultCode))) {
            hasToFinishActivity = true;
        }

        //Deprecate
        if (CallbackHolder.getInstance().hasPaymentDataCallback()) {
            CallbackHolder.getInstance().getPaymentDataCallback().onSuccess(paymentData, mPaymentMethodEdited);
        }

        //New
        if (hasToFinishActivity) {
            Intent intent = new Intent();
            intent.putExtra("paymentMethodChanged", mPaymentMethodEdited);
            intent.putExtra("paymentData", JsonUtil.getInstance().toJson(paymentData));
            setResult(mRequestedResultCode, intent);
            this.finish();
        }
    }

    protected void resolveCardVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mSelectedIssuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
            mSelectedPayerCost = JsonUtil.getInstance().fromJson(data.getStringExtra("payerCost"), PayerCost.class);
            mCreatedToken = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);
            mSelectedPaymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);

            if (mPaymentRecovery != null && mPaymentRecovery.isTokenRecoverable()) {
                createPayment(createPaymentData());
            } else {
                checkFlowWithPaymentMethodSelected();
            }
        } else {
            if (data != null && data.getStringExtra("mercadoPagoError") != null) {
                MPTracker.getInstance().trackEvent("CARD_VAULT", "CANCELED", "3", mMerchantPublicKey, mCheckoutPreference.getSite().getId(), BuildConfig.VERSION_NAME, this);
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                this.finish();
            } else {
                startPaymentVaultActivity();
            }
        }
    }

    private void resolvePaymentVaultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mDiscount = JsonUtil.getInstance().fromJson(data.getStringExtra("discount"), Discount.class);
            mSelectedIssuer = JsonUtil.getInstance().fromJson(data.getStringExtra("issuer"), Issuer.class);
            mSelectedPayerCost = JsonUtil.getInstance().fromJson(data.getStringExtra("payerCost"), PayerCost.class);
            mCreatedToken = JsonUtil.getInstance().fromJson(data.getStringExtra("token"), Token.class);
            mSelectedPaymentMethod = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentMethod"), PaymentMethod.class);

            checkFlowWithPaymentMethodSelected();

        } else {
            if (!mPaymentMethodEditionRequested) {
                cancelCheckout();
            } else {
                showReviewAndConfirm();
                animateBackFromPaymentEdition();
            }
        }
    }


    private void checkFlowWithPaymentMethodSelected() {

        if (isAdditionalStepRequired()) {
            startAdditionalStepVault();

        } else if (isReviewAndConfirmEnabled()) {
            showReviewAndConfirm();

        } else {
            resolvePaymentDataCallback();
        }
    }

    private void startAdditionalStepVault() {
        Site site = mCheckoutPreference.getSite();

        new MercadoPagoComponents.Activities.AdditionalStepVaultActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPaymentMethod(mSelectedPaymentMethod)
                .setDecorationPreference(mDecorationPreference)
                .setSite(site)
                .startActivity();

        animatePaymentMethodSelection();

    }

    private boolean isAdditionalStepRequired() {
        return mSelectedPaymentMethod.isAdditionalInfoNeeded();
    }


    private void showReviewAndConfirm() {
        MPTracker.getInstance().trackScreen("REVIEW_AND_CONFIRM", "3", mMerchantPublicKey, mCheckoutPreference.getSite().getId(), BuildConfig.VERSION_NAME, this);

        mPaymentMethodEditionRequested = false;

        MercadoPagoComponents.Activities.ReviewAndConfirmBuilder builder = new MercadoPagoComponents.Activities.ReviewAndConfirmBuilder()
                .setActivity(this)
                .setReviewScreenPreference(mReviewScreenPreference)
                .setPaymentMethod(mSelectedPaymentMethod)
                .setPayerCost(mSelectedPayerCost)
                .setAmount(mCheckoutPreference.getAmount())
                .setSite(mCheckoutPreference.getSite())
                .setDecorationPreference(mDecorationPreference)
                .setEditionEnabled(!isUniquePaymentMethod())
                .setDiscountEnabled(mDiscountEnabled)
                .setItems(mCheckoutPreference.getItems())
                .setTermsAndConditionsEnabled(!isUserLogged());

        if (mDiscountEnabled && isDiscountValid()) {
            builder.setDiscount(mDiscount);
        }

        if (MercadoPagoUtil.isCard(mSelectedPaymentMethod.getPaymentTypeId())) {
            builder.setToken(mCreatedToken);
        } else if (!PaymentTypes.ACCOUNT_MONEY.equals(mSelectedPaymentMethod.getPaymentTypeId())) {
            String searchItemComment = mPaymentMethodSearch.getSearchItemByPaymentMethod(mSelectedPaymentMethod).getComment();
            builder.setExtraPaymentMethodInfo(searchItemComment);
        }
        builder.startActivity();
    }

    private boolean isUserLogged() {
        return mPayer != null && !TextUtil.isEmpty(mPayer.getAccessToken());
    }

    private void resolvePaymentResultRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED && data != null) {
            String nextAction = data.getStringExtra("nextAction");
            if (!isEmpty(nextAction)) {
                if (nextAction.equals(PaymentResultAction.SELECT_OTHER_PAYMENT_METHOD)) {
                    startPaymentVaultActivity();
                }
                if (nextAction.equals(PaymentResultAction.RECOVER_PAYMENT)) {
                    createPaymentRecovery();
                    startCardVaultActivity();
                }
            }
        } else if (resultCode == PaymentResultActivity.RESULT_SILENT_OK) {
            setResult(RESULT_OK);
            finish();
        } else {
            if (data != null && data.hasExtra("resultCode")) {
                Integer finalResultCode = data.getIntExtra("resultCode", MercadoPagoCheckout.PAYMENT_RESULT_CODE);
                finishWithPaymentResult(finalResultCode);
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void createPaymentRecovery() {
        try {
            mPaymentRecovery = new PaymentRecovery(mCreatedToken, mCreatedPayment, mSelectedPaymentMethod, mSelectedPayerCost, mSelectedIssuer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void startCardVaultActivity() {
        PaymentPreference paymentPreference = mCheckoutPreference.getPaymentPreference();

        if (paymentPreference == null) {
            paymentPreference = new PaymentPreference();
        }

        paymentPreference.setDefaultPaymentTypeId(mSelectedPaymentMethod.getPaymentTypeId());

        boolean showBankDeals = true;
        if (mFlowPreference != null && !mFlowPreference.isBankDealsEnabled()) {
            showBankDeals = false;
        }

        new MercadoPagoComponents.Activities.CardVaultActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mMerchantPublicKey)
                .setPayerAccessToken(mPayer.getAccessToken())
                .setPaymentPreference(paymentPreference)
                .setDecorationPreference(mDecorationPreference)
                .setAmount(mCheckoutPreference.getAmount())
                .setSite(mCheckoutPreference.getSite())
                .setInstallmentsEnabled(true)
                .setAcceptedPaymentMethods(mPaymentMethodSearch.getPaymentMethods())
                .setInstallmentsReviewEnabled(mInstallmentsReviewScreenEnabled)
                .setDiscount(mDiscount)
                .setDiscountEnabled(mDiscountEnabled)
                .setDirectDiscountEnabled(mDirectDiscountEnabled)
                .setPaymentRecovery(mPaymentRecovery)
                .setShowBankDeals(showBankDeals)
                .startActivity();

        animatePaymentMethodSelection();
    }

    private void animatePaymentMethodSelection() {
        overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
    }

    private void resolveErrorRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            recoverFromFailure();
        } else if (noUserInteractionReached() || !isReviewAndConfirmEnabled()) {
            setResult(RESULT_CANCELED, data);
            this.finish();
        } else {
            showReviewAndConfirm();
        }
    }

    private boolean isReviewAndConfirmEnabled() {
        return mFlowPreference == null || mFlowPreference.isReviewAndConfirmScreenEnabled();
    }

    private boolean isDiscountEnabled() {
        return mFlowPreference == null || mFlowPreference.isDiscountEnabled();
    }

    private boolean isInstallmentsReviewScreenEnabled() {
        return mFlowPreference == null || mFlowPreference.isInstallmentsReviewScreenEnabled();
    }

    private boolean noUserInteractionReached() {
        return mSelectedPaymentMethod == null;
    }

    private void finishWithPaymentResult(Integer resultCode) {
        if (CallbackHolder.getInstance().hasPaymentCallback()) {
            //Deprecate
            PaymentCallback paymentCallback = CallbackHolder.getInstance().getPaymentCallback();
            paymentCallback.onSuccess(mCreatedPayment);

        } else if (CallbackHolder.getInstance().hasPaymentDataCallback()
                && !mRequestedResultCode.equals(MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE)
                && !mRequestedResultCode.equals(MercadoPagoCheckout.PAYMENT_RESULT_CODE)) {
            PaymentDataCallback paymentDataCallback = CallbackHolder.getInstance().getPaymentDataCallback();
            paymentDataCallback.onSuccess(null, mPaymentMethodEdited);

        }

        //New
        Intent intent = new Intent();
        intent.putExtra("payment", JsonUtil.getInstance().toJson(mCreatedPayment));
        setResult(resultCode, intent);
        this.finish();
    }

    private void changePaymentMethod() {
        if (!isUniquePaymentMethod()) {
            mPaymentMethodEdited = true;
            mPaymentMethodEditionRequested = true;
            startPaymentVaultActivity();
            overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
        }
    }

    private void animateBackFromPaymentEdition() {
        overridePendingTransition(R.anim.mpsdk_slide_left_to_right_in, R.anim.mpsdk_slide_left_to_right_out);
    }

    private void animateBackToPaymentMethodSelection() {
        overridePendingTransition(R.anim.mpsdk_slide_left_to_right_in, R.anim.mpsdk_slide_left_to_right_out);
    }

    private boolean isUniquePaymentMethod() {
        return isOnlyUniquePaymentMethodAvailable() || isOnlyAccountMoneyEnabled();
    }

    public boolean isOnlyUniquePaymentMethodAvailable() {
        return mPaymentMethodSearch != null && mPaymentMethodSearch.hasSearchItems() && mPaymentMethodSearch.getGroups().size() == 1 && mPaymentMethodSearch.getGroups().get(0).isPaymentMethod() && !mPaymentMethodSearch.hasCustomSearchItems();
    }

    private boolean isOnlyAccountMoneyEnabled() {
        return mPaymentMethodSearch.hasCustomSearchItems()
                && mPaymentMethodSearch.getCustomSearchItems().size() == 1
                && mPaymentMethodSearch.getCustomSearchItems().get(0).getId().equals(PaymentMethods.ACCOUNT_MONEY)
                && (mPaymentMethodSearch.getGroups() == null || mPaymentMethodSearch.getGroups().isEmpty());
    }

    protected void createPayment(final PaymentData paymentData) {

        if (mServicePreference != null && mServicePreference.hasCreatePaymentURL()) {

            Map<String, Object> paymentInfoMap = new HashMap<>();
            paymentInfoMap.putAll(mServicePreference.getCreatePaymentAdditionalInfo());

            String paymentDataJson = JsonUtil.getInstance().toJson(paymentData);

            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> paymentDataMap = new Gson().fromJson(paymentDataJson, type);

            paymentInfoMap.putAll(paymentDataMap);

            CustomServer.createPayment(this, getTransactionID(paymentData), mServicePreference.getCreatePaymentURL(), mServicePreference.getCreatePaymentURI(), paymentInfoMap, new Callback<Payment>() {
                @Override
                public void success(Payment payment) {
                    mCreatedPayment = payment;
                    PaymentResult paymentResult = createPaymentResult(payment, paymentData);
                    checkStartPaymentResultActivity(paymentResult);
                    cleanTransactionId();
                }

                @Override
                public void failure(ApiException apiException) {
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            createPayment(paymentData);
                        }
                    });
                    resolvePaymentFailure(apiException);
                }
            });
        } else {
            PaymentBody paymentBody = createPaymentBody(paymentData);
            mMercadoPagoServices.createPayment(paymentBody, new Callback<Payment>() {
                @Override
                public void success(Payment payment) {
                    mCreatedPayment = payment;
                    PaymentResult paymentResult = createPaymentResult(payment, paymentData);
                    checkStartPaymentResultActivity(paymentResult);
                    cleanTransactionId();
                }

                @Override
                public void failure(ApiException apiException) {
                    setFailureRecovery(new FailureRecovery() {
                        @Override
                        public void recover() {
                            createPayment(paymentData);
                        }
                    });
                    resolvePaymentFailure(apiException);
                }
            });
        }
    }

    private PaymentResult createPaymentResult(Payment payment, PaymentData paymentData) {
        return new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentId(payment.getId())
                .setPaymentStatus(payment.getStatus())
                .setPaymentStatusDetail(payment.getStatusDetail())
                .setPayerEmail(mPayer.getEmail())
                .setStatementDescription(payment.getStatementDescriptor())
                .build();
    }

    private PaymentData createPaymentData() {
        PaymentData paymentData = new PaymentData();
        paymentData.setPaymentMethod(mSelectedPaymentMethod);
        paymentData.setPayerCost(mSelectedPayerCost);
        paymentData.setIssuer(mSelectedIssuer);
        paymentData.setDiscount(mDiscount);
        paymentData.setToken(mCreatedToken);
        //TODO setear los datos que faltan de mPayer y transactionDetails
        paymentData.setPayer(mPayer);
        paymentData.setTransactionDetails(mTransactionDetails);
        return paymentData;
    }

    private PaymentBody createPaymentBody(PaymentData paymentData) {
        PaymentBody paymentIntent = new PaymentBody();
        paymentIntent.setPrefId(mCheckoutPreference.getId());
        paymentIntent.setPublicKey(mMerchantPublicKey);
        paymentIntent.setPaymentMethodId(paymentData.getPaymentMethod().getId());
        paymentIntent.setBinaryMode(mBinaryModeEnabled);
        //FIXME usar mPayer
//        Payer payer = mCheckoutPreference.getPayer();
        Payer payer = mPayer;
        if (!TextUtils.isEmpty(mCustomerId) && MercadoPagoUtil.isCard(paymentData.getPaymentMethod().getPaymentTypeId())) {
            payer.setId(mCustomerId);
        }

        paymentIntent.setPayer(payer);

        if (paymentData.getToken() != null) {
            paymentIntent.setTokenId(paymentData.getToken().getId());
        }
        if (paymentData.getPayerCost() != null) {
            paymentIntent.setInstallments(paymentData.getPayerCost().getInstallments());
        }
        if (paymentData.getIssuer() != null) {
            paymentIntent.setIssuerId(paymentData.getIssuer().getId());
        }
        //TODO setear a PaymentBody los datos que faltan de mPayer y transactionDetails
        if(paymentData.getTransactionDetails() != null){
            paymentIntent.setTransactionDetails(paymentData.getTransactionDetails());
        }

        mTransactionId = getTransactionID(paymentData);

        if (mDiscountEnabled && isDiscountValid()) {
            paymentIntent.setCampaignId(mDiscount.getId().intValue());
            paymentIntent.setCouponAmount(mDiscount.getCouponAmount().floatValue());

            if (!isEmpty(paymentData.getDiscount().getCouponCode())) {
                paymentIntent.setCouponCode(paymentData.getDiscount().getCouponCode());
            }
        }

        paymentIntent.setTransactionId(mTransactionId);
        return paymentIntent;
    }

    private String getTransactionID(PaymentData paymentData) {
        String transactionId;
        if (!existsTransactionId() || !MercadoPagoUtil.isCard(paymentData.getPaymentMethod().getPaymentTypeId())) {
            transactionId = createNewTransactionId();
        } else {
            transactionId = mTransactionId;
        }
        return transactionId;
    }

    private Boolean isDiscountValid() {
        return mDiscount != null && isCampaignIdValid() && isCouponAmountValid() && isDiscountCurrencyIdValid();
    }

    private Boolean isCampaignIdValid() {
        return mDiscount.getId() != null;
    }

    private Boolean isCouponAmountValid() {
        return mDiscount.getCouponAmount() != null && mDiscount.getCouponAmount().compareTo(BigDecimal.ZERO) >= 0;
    }

    private Boolean isDiscountCurrencyIdValid() {
        return mDiscount != null && mDiscount.getCurrencyId() != null && CurrenciesUtil.isValidCurrency(mDiscount.getCurrencyId());
    }

    private void checkStartPaymentResultActivity(PaymentResult paymentResult) {
        if (hasToSkipPaymentResultActivity(paymentResult)) {
            finishWithPaymentResult(MercadoPagoCheckout.PAYMENT_RESULT_CODE);
        } else {
            startPaymentResultActivity(paymentResult);
        }
    }

    private boolean hasToSkipPaymentResultActivity(PaymentResult paymentResult) {
        return mCongratsDisplay == 0 && (paymentResult != null) && (!isEmpty(paymentResult.getPaymentStatus())) &&
                (paymentResult.getPaymentStatus().equals(Payment.StatusCodes.STATUS_APPROVED));
    }

    private void startPaymentResultActivity(PaymentResult paymentResult) {

        BigDecimal amount = mCreatedPayment == null ? mCheckoutPreference.getAmount() : mCreatedPayment.getTransactionDetails().getTotalPaidAmount();

        new MercadoPagoComponents.Activities.PaymentResultActivityBuilder()
                .setMerchantPublicKey(mMerchantPublicKey)
                .setActivity(mActivity)
                .setPaymentResult(paymentResult)
                .setDiscountEnabled(mDiscountEnabled)
                .setCongratsDisplay(mCongratsDisplay)
                .setSite(mCheckoutPreference.getSite())
                .setAmount(mCheckoutPreference.getAmount())
                .setPaymentResultScreenPreference(mPaymentResultScreenPreference)
                .setAmount(amount)
                .startActivity();
    }

    private String createNewTransactionId() {
        return String.valueOf(mMerchantPublicKey + Calendar.getInstance().getTimeInMillis()) + String.valueOf(Math.round(Math.random()));
    }

    private boolean existsTransactionId() {
        return mTransactionId != null;
    }

    private void cleanTransactionId() {
        mTransactionId = null;
    }

    private void resolvePaymentFailure(ApiException apiException) {
        if (apiException.getStatus() != null) {
            String serverErrorFirstDigit = String.valueOf(ApiUtil.StatusCodes.INTERNAL_SERVER_ERROR).substring(0, 1);

            if (apiException.getStatus() == ApiUtil.StatusCodes.PROCESSING) {
                startPaymentInProcessActivity();
                cleanTransactionId();
            } else if (String.valueOf(apiException.getStatus()).startsWith(serverErrorFirstDigit)) {

                ApiUtil.showApiExceptionError(this, apiException);
                setFailureRecovery(new FailureRecovery() {
                    @Override
                    public void recover() {
                        createPayment(createPaymentData());
                    }
                });
            } else if (apiException.getStatus() == ApiUtil.StatusCodes.BAD_REQUEST) {

                MercadoPagoError error = new MercadoPagoError(apiException);
                ErrorUtil.startErrorActivity(this, error);
            } else {
                ApiUtil.showApiExceptionError(this, apiException);
            }
        } else {
            ApiUtil.showApiExceptionError(this, apiException);
        }
    }

    private void startPaymentInProcessActivity() {
        mCreatedPayment = new Payment();
        mCreatedPayment.setStatus(Payment.StatusCodes.STATUS_IN_PROCESS);
        mCreatedPayment.setStatusDetail(Payment.StatusCodes.STATUS_DETAIL_PENDING_CONTINGENCY);
        PaymentResult paymentResult = createPaymentResult(mCreatedPayment, createPaymentData());
        startPaymentResultActivity(paymentResult);
    }

    private void cancelCheckout() {

        //TODO Deprecate
        if (CallbackHolder.getInstance().hasPaymentDataCallback()) {
            CallbackHolder.getInstance().getPaymentDataCallback().onCancel();
        } else {
            CallbackHolder.getInstance().getPaymentCallback().onCancel();
        }
        finishCheckoutWithCancel();
    }

    private void finishCheckoutWithCancel() {
        //New
        setResult(RESULT_CANCELED);
        this.finish();
    }

    private void cancelCheckout(Integer resultCode, PaymentData paymentData) {
        Intent intent = new Intent();
        intent.putExtra("paymentMethodChanged", mPaymentMethodEdited);
        intent.putExtra("paymentData", JsonUtil.getInstance().toJson(paymentData));
        setResult(resultCode, intent);
        this.finish();
    }


    private void cancelFailedCheckout(MercadoPagoError error) {
        //Deprecate
        CallbackHolder.getInstance().getPaymentCallback().onFailure(error);

        //New
        Intent intent = new Intent();
        intent.putExtra("mercadoPagoError", JsonUtil.getInstance().toJson(error));
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    public void setFailureRecovery(FailureRecovery failureRecovery) {
        this.mFailureRecovery = failureRecovery;
    }

    protected void recoverFromFailure() {
        if (mFailureRecovery != null) {
            mFailureRecovery.recover();
        }
    }
}
