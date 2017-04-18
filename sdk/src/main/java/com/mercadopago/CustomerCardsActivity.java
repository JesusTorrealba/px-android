package com.mercadopago;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Card;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.presenters.CustomerCardsPresenter;
import com.mercadopago.providers.CustomerCardsProviderImpl;
import com.mercadopago.uicontrollers.savedcards.SavedCardsListView;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.views.CustomerCardsView;

import java.lang.reflect.Type;
import java.util.List;

public class CustomerCardsActivity extends MercadoPagoBaseActivity implements CustomerCardsView {

    // Local vars
    protected String mMerchantBaseUrl;
    protected String mMerchantGetCustomerUri;

    protected String mMerchantAccessToken;
    protected boolean mActivityActive;
    protected ViewGroup mSavedCardsContainer;
    protected DecorationPreference mDecorationPreference;

    //Controls
    protected CustomerCardsPresenter mPresenter;
    protected TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPresenter();
        getActivityParameters();

        mPresenter.attachView(this);
        mPresenter.attachResourcesProvider(new CustomerCardsProviderImpl(this, mMerchantAccessToken, mMerchantBaseUrl, mMerchantGetCustomerUri));

        mActivityActive = true;

        setContentView();
        initializeControls();
        initialize();
    }

    protected void initialize() {
        mPresenter.initialize();
    }

    protected void createPresenter() {
        mPresenter = new CustomerCardsPresenter();
    }

    protected void getActivityParameters() {
        List<Card> cards;

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Card>>() {
            }.getType();

            cards = gson.fromJson(this.getIntent().getStringExtra("cards"), listType);
        } catch (Exception ex) {
            cards = null;
        }

        mMerchantBaseUrl = this.getIntent().getStringExtra("merchantBaseUrl");
        mMerchantGetCustomerUri = this.getIntent().getStringExtra("merchantGetCustomerUri");
        mMerchantAccessToken = this.getIntent().getStringExtra("merchantAccessToken");

        mPresenter.setCustomTitle(this.getIntent().getStringExtra("title"));
        mPresenter.setSelectionConfirmPromptText(this.getIntent().getStringExtra("selectionConfirmPromptText"));
        mPresenter.setSelectionImageDrawableResId(this.getIntent().getIntExtra("selectionImageResId", 0));
        mPresenter.setCustomActionMessage(this.getIntent().getStringExtra("customActionMessage"));
        mPresenter.setCards(cards);
    }

    protected void setContentView() {
        setContentView(R.layout.mpsdk_activity_customer_cards);
    }

    protected void initializeControls() {
        initializeToolbar();
        mSavedCardsContainer = (ViewGroup) findViewById(R.id.mpsdkRegularLayout);
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.mpsdkToolbar);
        setSupportActionBar(toolbar);

        mTitle = (TextView) findViewById(R.id.mpsdkToolbarTitle);
        if (!TextUtils.isEmpty(mPresenter.getCustomTitle())) {
            mTitle.setText(mPresenter.getCustomTitle());
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (isCustomColorSet()) {
            decorate(toolbar);
        }
    }

    private boolean isCustomColorSet() {
        return mDecorationPreference != null && mDecorationPreference.hasColors();
    }

    private void decorate(Toolbar toolbar) {
        if (toolbar != null) {
            if (mDecorationPreference.hasColors()) {
                toolbar.setBackgroundColor(mDecorationPreference.getBaseColor());
            }
            decorateUpArrow(toolbar);
        }
    }

    protected void decorateUpArrow(Toolbar toolbar) {
        if (mDecorationPreference.isDarkFontEnabled()) {
            mTitle.setTextColor(mDecorationPreference.getDarkFontColor(this));
            int darkFont = mDecorationPreference.getDarkFontColor(this);
            Drawable upArrow = toolbar.getNavigationIcon();
            if (upArrow != null && getSupportActionBar() != null) {
                upArrow.setColorFilter(darkFont, PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPresenter.recoverFromFailure();
            } else {
                setResult(RESULT_CANCELED, data);
                finish();
            }
        }
    }

    @Override
    public void showCards(List<Card> cards, OnSelectedCallback<Card> onSelectedCallback) {
        SavedCardsListView savedCardsView = new MercadoPagoComponents.Views.SavedCardsListViewBuilder()
                .setContext(this)
                .setCards(cards)
                .setOnSelectedCallback(onSelectedCallback)
                .setCustomActionMessage(mPresenter.getCustomActionMessage())
                .setSelectionImage(mPresenter.getSelectionImageDrawableResId())
                .build();

        savedCardsView.drawInParent(mSavedCardsContainer);
    }

    @Override
    public void showAlertDialog(final Card card) {
        String lastDigitsLabel = mPresenter.getResourcesProvider().getLastDigitsLabel();
        String dialogTitle = new StringBuilder().append(lastDigitsLabel).append(" ").append(card.getLastFourDigits()).toString();

        new AlertDialog.Builder(this)
                .setIcon(mPresenter.getResourceId(this, card))
                .setTitle(dialogTitle)
                .setMessage(mPresenter.getSelectionConfirmPromptText())
                .setPositiveButton(mPresenter.getResourcesProvider().getConfirmPromptYes(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishWithCardResult(card);
                    }

                })
                .setNegativeButton(mPresenter.getResourcesProvider().getConfirmPromptNo(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
                .show();
    }

    @Override
    public void hideProgress() {
        LayoutUtil.showRegularLayout(this);
    }

    @Override
    public void showProgress() {
        LayoutUtil.showProgressLayout(this);
    }

    @Override
    public void showError(MercadoPagoError error) {
        if (error.isApiException()) {
            showApiException(error.getApiException());
        } else {
            ErrorUtil.startErrorActivity(this, error);
        }
    }

    @Override
    public void finishWithCardResult(Card card) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        returnIntent.putExtra("card", JsonUtil.getInstance().toJson(card));
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void showApiException(ApiException apiException) {
        if (mActivityActive) {
            ApiUtil.showApiExceptionError(this, apiException);
        }
    }

    public void onOtherPaymentMethodClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

}
