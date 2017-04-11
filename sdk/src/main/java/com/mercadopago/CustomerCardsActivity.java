package com.mercadopago;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.callbacks.Callback;
import com.mercadopago.callbacks.FailureRecovery;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.core.MercadoPagoUI;
import com.mercadopago.core.MerchantServer;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.ApiException;
import com.mercadopago.model.Card;
import com.mercadopago.model.Customer;
import com.mercadopago.model.Discount;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.presenters.CustomerCardsPresenter;
import com.mercadopago.presenters.PaymentVaultPresenter;
import com.mercadopago.providers.CustomerCardsProviderImpl;
import com.mercadopago.providers.PaymentVaultProviderImpl;
import com.mercadopago.uicontrollers.FontCache;
import com.mercadopago.uicontrollers.savedcards.SavedCardsListView;
import com.mercadopago.util.ApiUtil;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.util.LayoutUtil;
import com.mercadopago.util.MercadoPagoUtil;
import com.mercadopago.views.CustomerCardsView;

import java.lang.reflect.Type;
import java.util.List;

public class CustomerCardsActivity extends MercadoPagoBaseActivity implements CustomerCardsView {

    // Local vars
    protected String mMerchantBaseUrl;
    protected String mMerchantGetCustomerUri;

    protected String mPublicKey;
    protected String mPrivateKey;
    protected boolean mActivityActive;
    protected List<Card> mCards;
    protected ViewGroup mSavedCardsContainer;
    protected DecorationPreference mDecorationPreference;

    //Controls
    protected CustomerCardsPresenter mPresenter;
    protected String mCustomTitle;
    protected TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createPresenter();
        getActivityParameters();

        mPresenter.attachView(this);
        mPresenter.attachResourcesProvider(new CustomerCardsProviderImpl(this, mPublicKey, mPrivateKey, mMerchantBaseUrl, mMerchantGetCustomerUri));

        //TODO, va?
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
        mPrivateKey = this.getIntent().getStringExtra("privateKey");

        mPresenter.setCustomTitle(this.getIntent().getStringExtra("title"));
        mPresenter.setSelectionConfirmPromptText(this.getIntent().getStringExtra("selectionConfirmPromptText"));
        mPresenter.setSelectionImageDrawableResId(this.getIntent().getIntExtra("selectionImageResId", 0));
        mPresenter.setCustomFooterMessage(this.getIntent().getStringExtra("footerText"));
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
        if (!TextUtils.isEmpty(mCustomTitle)) {
            mTitle.setText(mCustomTitle);
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
    public void fillData() {
        SavedCardsListView savedCardsView = new MercadoPagoComponents.Views.SavedCardsListViewBuilder()
                .setContext(this)
                .setCards(mPresenter.getCards())
                .setFooter(mPresenter.getCustomFooterMessage())
                .setOnSelectedCallback(getOnSelectedCallback())
                .build();

        savedCardsView.drawInParent(mSavedCardsContainer);
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

    private OnSelectedCallback<Card> getOnSelectedCallback() {
        return new OnSelectedCallback<Card>() {
            @Override
            public void onSelected(Card card) {
                if (card != null) {
                    mPresenter.resolveCardResponse(card);
                }
            }
        };
    }

    public void onOtherPaymentMethodClicked(View view) {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
