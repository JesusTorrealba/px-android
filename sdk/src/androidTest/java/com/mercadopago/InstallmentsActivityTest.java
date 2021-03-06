package com.mercadopago;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Spanned;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.mercadopago.constants.Sites;
import com.mercadopago.controllers.CheckoutTimer;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.Card;
import com.mercadopago.model.Currency;
import com.mercadopago.model.DecorationPreference;
import com.mercadopago.model.Discount;
import com.mercadopago.model.Installment;
import com.mercadopago.model.Issuer;
import com.mercadopago.model.PayerCost;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.PaymentPreference;
import com.mercadopago.model.Site;
import com.mercadopago.model.Token;
import com.mercadopago.test.ActivityResult;
import com.mercadopago.test.FakeAPI;
import com.mercadopago.test.StaticMock;
import com.mercadopago.util.CurrenciesUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.utils.ActivityResultUtil;
import com.mercadopago.utils.ViewUtils;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.Intents.times;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.mercadopago.utils.ActivityResultUtil.getActivityResult;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by vaserber on 7/12/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InstallmentsActivityTest {

    @Rule
    public ActivityTestRule<InstallmentsActivity> mTestRule = new ActivityTestRule<>(InstallmentsActivity.class, true, false);
    public Intent validStartIntent;

    private String mMerchantPublicKey;
    private PaymentMethod mPaymentMethod;
    private Site mSite;
    private BigDecimal mAmount;
    private String mCurrencyId;

    private FakeAPI mFakeAPI;

    @Before
    public void createValidStartIntent() {
        mMerchantPublicKey = StaticMock.DUMMY_MERCHANT_PUBLIC_KEY;
        mPaymentMethod = StaticMock.getPaymentMethodOn();
        mSite = Sites.ARGENTINA;
        mAmount = new BigDecimal(1000);
        mCurrencyId = mSite.getCurrencyId();

        validStartIntent = new Intent();
        validStartIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        validStartIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
        validStartIntent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        validStartIntent.putExtra("amount", JsonUtil.getInstance().toJson(mAmount));
    }

    @Before
    public void startFakeAPI() {
        mFakeAPI = new FakeAPI();
        mFakeAPI.start();
    }

    @Before
    public void initIntentsRecording() {
        Intents.init();
    }

    @After
    public void stopFakeAPI() {
        mFakeAPI.stop();
    }

    @After
    public void releaseIntents() {
        Intents.release();
    }

    @Test
    public void getActivityParametersOnCreateIsValid() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        InstallmentsActivity activity = mTestRule.launchActivity(validStartIntent);

        assertEquals(activity.mPresenter.getPublicKey(), mMerchantPublicKey);
        assertEquals(activity.mPresenter.getPaymentMethod().getId(), mPaymentMethod.getId());
        assertEquals(activity.mPresenter.getSite().getId(), mSite.getId());
        assertEquals(activity.mPresenter.getAmount(), mAmount);
        assertNotNull(activity.mPresenter.getPayerCosts());
        assertEquals(activity.mPresenter.getPayerCosts().size(), payerCostList.size());
        assertNull(activity.mPresenter.getIssuer());
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void initializeWithoutPayerCostsButWithIssuerIsValid() {
        String installments = StaticMock.getInstallmentsJson();
        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        InstallmentsActivity activity = mTestRule.launchActivity(validStartIntent);

        assertEquals(activity.mPresenter.getPublicKey(), mMerchantPublicKey);
        assertEquals(activity.mPresenter.getPaymentMethod().getId(), mPaymentMethod.getId());
        assertEquals(activity.mPresenter.getSite().getId(), mSite.getId());
        assertEquals(activity.mPresenter.getAmount(), mAmount);
        assertEquals(activity.mPresenter.getIssuer().getId(), issuer.getId());
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void hideCardWhenNoTokenOrCard() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        validStartIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        mTestRule.launchActivity(validStartIntent);

        assertTrue(mTestRule.getActivity().mLowResActive);
        assertNotNull(mTestRule.getActivity().mLowResToolbar);
        assertNull(mTestRule.getActivity().mNormalToolbar);
        assertNull(mTestRule.getActivity().mCardContainer);
    }

    @Test
    public void hideCardWhenNoPaymentMethod() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        Token token = StaticMock.getToken();
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));
        validStartIntent.removeExtra("paymentMethod");

        mTestRule.launchActivity(validStartIntent);

        assertTrue(mTestRule.getActivity().mLowResActive);
        assertNotNull(mTestRule.getActivity().mLowResToolbar);
        assertNull(mTestRule.getActivity().mNormalToolbar);
        assertNull(mTestRule.getActivity().mCardContainer);
    }

    @Test
    public void showCardWhenTokenAndPaymentMethod() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        Token token = StaticMock.getToken();

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        validStartIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));

        mTestRule.launchActivity(validStartIntent);
        assertNotNull(mTestRule.getActivity().mCardContainer);
        assertNotNull(mTestRule.getActivity().mNormalToolbar);
        assertNotNull(mTestRule.getActivity().mFrontCardView);
        onView(withId(R.id.mpsdkCardFrontContainer)).check(matches(isDisplayed()));
    }

    @Test
    public void showToolbarWithTitleWhenNoTokenOrCard() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        mTestRule.launchActivity(validStartIntent);

        assertNotNull(mTestRule.getActivity().mLowResToolbar);
        onView(withId(R.id.mpsdkRegularToolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkTitle)).check(matches(withText(R.string.mpsdk_card_installments_title)));
    }

    @Test
    public void showCollapsingToolbarWhenToken() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        Token token = StaticMock.getToken();

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));

        mTestRule.launchActivity(validStartIntent);

        assertNull(mTestRule.getActivity().mLowResToolbar);
        assertNotNull(mTestRule.getActivity().mNormalToolbar);
        onView(withId(R.id.mpsdkRegularToolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkCollapsingToolbar)).check(matches(isDisplayed()));
        String expected = mTestRule.getActivity().getApplicationContext().getResources().getString(R.string.mpsdk_card_installments_title);
        assertEquals(mTestRule.getActivity().mNormalToolbar.getTitle().toString(), expected);
    }

    @Test
    public void initializeCardWhenCardSet() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        //Visa
        Card card = StaticMock.getCards().get(1);

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        validStartIntent.putExtra("card", JsonUtil.getInstance().toJson(card));

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkCardNumberTextView)).check(matches(withText(containsString(card.getLastFourDigits()))));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onView(withId(R.id.mpsdkCardLollipopImageView)).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.mpsdkCardLowApiImageView)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void initializeCardWhenToken() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        Token token = StaticMock.getToken();

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkCardNumberTextView)).check(matches(withText(containsString(token.getLastFourDigits()))));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            onView(withId(R.id.mpsdkCardLollipopImageView)).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.mpsdkCardLowApiImageView)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void ifPayerCostsSetAllowStartWithoutPaymentMethod() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        Issuer issuer = StaticMock.getIssuer();

        Intent intent = new Intent();
        intent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        intent.putExtra("merchantPublicKey", mMerchantPublicKey);
        intent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        intent.putExtra("amount", mAmount);
        intent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        mTestRule.launchActivity(intent);
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void ifPayerCostListSetAllowStartWithoutPublicKey() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        Issuer issuer = StaticMock.getIssuer();

        Intent intent = new Intent();
        intent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        intent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        intent.putExtra("amount", mAmount);
        intent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        intent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));

        mTestRule.launchActivity(intent);
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void ifPayerCostListSetAllowStartWithoutIssuer() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        Issuer issuer = StaticMock.getIssuer();

        Intent intent = new Intent();
        intent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        intent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        intent.putExtra("amount", mAmount);
        intent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        mTestRule.launchActivity(intent);
        assertFalse(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void ifPayerCostListNotSetFinishOnInvalidParameterPublicKey() {

        Issuer issuer = StaticMock.getIssuer();

        Intent invalidIntent = new Intent();
        invalidIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        invalidIntent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        invalidIntent.putExtra("amount", mAmount);
        invalidIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));

        mTestRule.launchActivity(invalidIntent);
        assertTrue(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void ifPayerCostsNotSetFinishOnInvalidParameterPaymentMethod() {

        Issuer issuer = StaticMock.getIssuer();

        Intent invalidIntent = new Intent();
        invalidIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        invalidIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        invalidIntent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        invalidIntent.putExtra("amount", mAmount);

        mTestRule.launchActivity(invalidIntent);
        assertTrue(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void ifPayerCostsNotSetFinishOnInvalidParameterIssuer() {
        String installments = StaticMock.getInstallmentsJson();
        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        InstallmentsActivity activity = mTestRule.launchActivity(validStartIntent);

        assertEquals(activity.mPresenter.getPublicKey(), mMerchantPublicKey);
        assertEquals(activity.mPresenter.getPaymentMethod().getId(), mPaymentMethod.getId());
        assertEquals(activity.mPresenter.getSite().getId(), mSite.getId());
        assertEquals(activity.mPresenter.getAmount(), mAmount);
        assertNull(activity.mPresenter.getIssuer());
        assertNull(activity.mPresenter.getPayerCosts());
        assertTrue(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void finishOnInvalidParameterAmount() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        Intent invalidIntent = new Intent();
        invalidIntent.putExtra("merchantPublicKey", mMerchantPublicKey);
        invalidIntent.putExtra("site", JsonUtil.getInstance().toJson(mSite));
        invalidIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));
        invalidIntent.putExtra("paymentMethod", JsonUtil.getInstance().toJson(mPaymentMethod));

        mTestRule.launchActivity(invalidIntent);
        assertTrue(mTestRule.getActivity().isFinishing());
    }

    @Test
    public void showErrorOnEmptyInstallmentsAsyncResponse() {
        List<Installment> installmentList = new ArrayList<>();
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        mTestRule.launchActivity(validStartIntent);
        intended(hasComponent(ErrorActivity.class.getName()));
    }

    @Test
    public void showErrorOnMultipleInstallmentsAsyncResponse() {
        List<Installment> installmentList = new ArrayList<Installment>();
        installmentList.add(new Installment());
        installmentList.add(new Installment());

        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        mTestRule.launchActivity(validStartIntent);
        intended(hasComponent(ErrorActivity.class.getName()));
    }

    @Test
    public void initializePayerCostsWhenListSent() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);

        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        mTestRule.launchActivity(validStartIntent);

        RecyclerView referencesLayout = (RecyclerView) mTestRule.getActivity().findViewById(R.id.mpsdkActivityInstallmentsView);
        assertEquals(referencesLayout.getChildCount(), payerCostList.size());
    }

    @Test
    public void selectPayerCostAndGetResult() {
        String installments = StaticMock.getInstallmentsJson();
        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);
        List<PayerCost> payerCosts = installmentList.get(0).getPayerCosts();
        PayerCost mockedPayerCostFirst = payerCosts.get(0);
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkActivityInstallmentsView)).perform(actionOnItemAtPosition(0, click()));

        ActivityResult result = getActivityResult(mTestRule.getActivity());
        PayerCost selectedPayerCost = JsonUtil.getInstance().fromJson(result.getExtras().getString("payerCost"), PayerCost.class);
        assertEquals(mockedPayerCostFirst.getRecommendedMessage(), selectedPayerCost.getRecommendedMessage());

        ActivityResultUtil.assertFinishCalledWithResult(mTestRule.getActivity(), Activity.RESULT_OK);
    }

    @Test
    public void ifApiFailureShowErrorActivity() {
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        mFakeAPI.addResponseToQueue("", 401, "");
        mTestRule.launchActivity(validStartIntent);
        intended(hasComponent(ErrorActivity.class.getName()));
    }

    @Test
    public void ifAfterApiFailureUserRetriesAndSucceedsShowPayerCosts() {
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        mFakeAPI.addResponseToQueue("", 401, "");
        String installmentsJson = StaticMock.getInstallmentsJson();
        mFakeAPI.addResponseToQueue(installmentsJson, 200, "");

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkErrorRetry)).perform(click());

        //Prepare assertion data
        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentsList = JsonUtil.getInstance().getGson().fromJson(installmentsJson, listType);


        RecyclerView referencesLayout = (RecyclerView) mTestRule.getActivity().findViewById(R.id.mpsdkActivityInstallmentsView);
        assertEquals(referencesLayout.getChildCount(), installmentsList.get(0).getPayerCosts().size());
    }

    @Test
    public void decorationPreferenceWithTokenPaintsBackground() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        Token token = StaticMock.getToken();
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));

        DecorationPreference decorationPreference = new DecorationPreference();
        decorationPreference.setBaseColor(ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_color_red_error));
        validStartIntent.putExtra("decorationPreference", JsonUtil.getInstance().toJson(decorationPreference));

        mTestRule.launchActivity(validStartIntent);

        int appBarColor = ViewUtils.getBackgroundColor(mTestRule.getActivity().mAppBar);
        assertEquals(appBarColor, decorationPreference.getLighterColor());
        int toolbarColor = ViewUtils.getBackgroundColor(mTestRule.getActivity().mNormalToolbar);
        assertEquals(toolbarColor, decorationPreference.getLighterColor());
    }

    @Test
    public void decorationPreferenceWithoutTokenPaintsToolbar() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        DecorationPreference decorationPreference = new DecorationPreference();
        decorationPreference.setBaseColor(ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_color_red_error));
        validStartIntent.putExtra("decorationPreference", JsonUtil.getInstance().toJson(decorationPreference));

        mTestRule.launchActivity(validStartIntent);

        int color = ViewUtils.getBackgroundColor(mTestRule.getActivity().mLowResToolbar);
        assertEquals(color, (int)decorationPreference.getBaseColor());


        MPTextView toolbarTitle = (MPTextView) mTestRule.getActivity().findViewById(R.id.mpsdkTitle);
        int fontColor = toolbarTitle.getCurrentTextColor();
        int expectedColor = ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_white);
        assertEquals(fontColor, expectedColor);
    }

    @Test
    public void whenDefaultPayerCostFoundSetItAsResult() {
        //Add API response
        String installmentsJson = StaticMock.getInstallmentsJson();
        mFakeAPI.addResponseToQueue(installmentsJson, 200, "");

        //Set default installment
        PaymentPreference paymentPreference = new PaymentPreference();
        paymentPreference.setDefaultInstallments(3);

        //Prepare start intent
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mTestRule.launchActivity(validStartIntent);

        //Prepare assertion data
        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentsList = JsonUtil.getInstance().getGson().fromJson(installmentsJson, listType);

        ActivityResult activityResult = getActivityResult(mTestRule.getActivity());
        String payerCostJson = JsonUtil.getInstance().toJson(installmentsList.get(0).getPayerCosts().get(1));

        //Assertions
        assertTrue(activityResult.getResultCode() == Activity.RESULT_OK);
        assertEquals(payerCostJson, activityResult.getExtras().getString("payerCost"));
    }

    @Test
    public void whenEmptyInstallmentsReceivedStartErrorActivity() {
        //Add API response
        List<Installment> installments = new ArrayList<>();
        mFakeAPI.addResponseToQueue(installments, 200, "");

        //Prepare start intent
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        mTestRule.launchActivity(validStartIntent);

        intended(hasComponent(ErrorActivity.class.getName()));
    }

    @Test
    public void whenEmptyPayerCostsReceivedStartErrorActivity() {
        //Add API response
        List<Installment> installments = new ArrayList<>();
        installments.add(new Installment());
        List<PayerCost> payerCosts = new ArrayList<>();
        installments.get(0).setPayerCosts(payerCosts);
        mFakeAPI.addResponseToQueue(installments, 200, "");

        //Prepare start intent
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        mTestRule.launchActivity(validStartIntent);

        intended(hasComponent(ErrorActivity.class.getName()));
    }

    @Test
    public void whenOnlyOnePayerCostAvailableFinishWithItAsResult() {
        //Prepare API response
        List<Installment> installments = new ArrayList<>();
        installments.add(new Installment());

        final PayerCost payerCost = StaticMock.getPayerCostWithInterests();
        List<PayerCost> payerCosts = new ArrayList<>();
        payerCosts.add(payerCost);

        installments.get(0).setPayerCosts(payerCosts);

        //Add API response
        mFakeAPI.addResponseToQueue(installments, 200, "");

        //Prepare start intent
        Issuer issuer = StaticMock.getIssuer();
        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));

        mTestRule.launchActivity(validStartIntent);

        //Prepare assertion data

        ActivityResult activityResult = getActivityResult(mTestRule.getActivity());
        String payerCostJson = JsonUtil.getInstance().toJson(installments.get(0).getPayerCosts().get(0));

        //Assertions
        assertTrue(activityResult.getResultCode() == Activity.RESULT_OK);
        assertEquals(payerCostJson, activityResult.getExtras().getString("payerCost"));
    }

    @Test
    public void decorationPreferenceWithDarkFontAndTokenPaintsBackground() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        Token token = StaticMock.getToken();
        validStartIntent.putExtra("token", JsonUtil.getInstance().toJson(token));

        DecorationPreference decorationPreference = new DecorationPreference();
        decorationPreference.setBaseColor(ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_color_red_error));
        decorationPreference.enableDarkFont();
        validStartIntent.putExtra("decorationPreference", JsonUtil.getInstance().toJson(decorationPreference));

        mTestRule.launchActivity(validStartIntent);

        int appBarColor = ViewUtils.getBackgroundColor(mTestRule.getActivity().mAppBar);
        assertEquals(appBarColor, decorationPreference.getLighterColor());
        int toolbarColor = ViewUtils.getBackgroundColor(mTestRule.getActivity().mNormalToolbar);
        assertEquals(toolbarColor, decorationPreference.getLighterColor());
    }
    
    @Test
    public void decorationPreferenceWithDarkFontAndWithoutTokenPaintsToolbarAndTitle() {
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        DecorationPreference decorationPreference = new DecorationPreference();
        decorationPreference.setBaseColor(ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_color_red_error));
        decorationPreference.enableDarkFont();
        validStartIntent.putExtra("decorationPreference", JsonUtil.getInstance().toJson(decorationPreference));

        mTestRule.launchActivity(validStartIntent);

        int color = ViewUtils.getBackgroundColor(mTestRule.getActivity().mLowResToolbar);
        assertEquals(color, (int)decorationPreference.getBaseColor());

        MPTextView toolbarTitle = (MPTextView) mTestRule.getActivity().findViewById(R.id.mpsdkTitle);
        int fontColor = toolbarTitle.getCurrentTextColor();
        int expectedColor = ContextCompat.getColor(InstrumentationRegistry.getContext(), R.color.mpsdk_dark_font_color);
        assertEquals(fontColor, expectedColor);
    }

    //Timer
    @Test
    public void showCountDownTimerWhenItIsInitialized(){
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        CheckoutTimer.getInstance().start(60);

        mTestRule.launchActivity(validStartIntent);

        Assert.assertTrue(mTestRule.getActivity().findViewById(R.id.mpsdkTimerTextView).getVisibility() == View.VISIBLE);
        Assert.assertTrue(CheckoutTimer.getInstance().isTimerEnabled());
        Looper.myLooper().quit();
    }

    @Test
    public void finishActivityWhenSetOnFinishCheckoutListener(){
        Looper.prepare();
        String payerCosts = StaticMock.getPayerCostsJson();
        Type listType = new TypeToken<List<PayerCost>>(){}.getType();
        List<PayerCost> payerCostList = JsonUtil.getInstance().getGson().fromJson(payerCosts, listType);
        validStartIntent.putExtra("payerCosts", JsonUtil.getInstance().toJson(payerCostList));

        CheckoutTimer.getInstance().start(10);
        CheckoutTimer.getInstance().setOnFinishListener(new CheckoutTimer.FinishListener() {
            @Override
            public void onFinish() {
                CheckoutTimer.getInstance().finishCheckout();
                Assert.assertTrue(mTestRule.getActivity().isFinishing());
                Looper.myLooper().quit();
            }
        });

        mTestRule.launchActivity(validStartIntent);
    }

    //Discount
    @Test
    public void showDirectDiscountWithPercentOffWhenStartActivityWithoutDiscount() {
        Spanned amountWithDiscount, totalAmount;
        Discount discount = StaticMock.getPercentOffDiscount();

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue(discount, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));
    }

    @Test
    public void showDirectDiscountWithAmountOffWhenStartActivityWithoutDiscount() {
        Spanned amountWithDiscount, totalAmount;

        Discount discount = StaticMock.getPercentOffDiscount();

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>() {
        }.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue(discount, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));
    }

    @Test
    public void showHasADiscountWhenStartActivityWithoutDiscountAndNotHasDirectDiscount() {
        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>() {
        }.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue("", 404, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkDiscountRow)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkHasDiscount)).check(matches(isDisplayed()));
    }

    @Test
    public void startDiscountActivityWhenNotHasDirectDiscountAndPressOnHasDiscountRow() {
        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>() {
        }.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue("", 404, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkDiscountRow)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkHasDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkHasDiscount)).perform(click());

        Intents.intended(hasComponent(DiscountsActivity.class.getName()), times(1));
    }

    @Test
    public void showDiscountDetailWhenStartActivityWithDiscount() {
        Spanned amountWithDiscount, totalAmount;

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Discount discount = StaticMock.getPercentOffDiscount();

        Type listType = new TypeToken<List<Installment>>() {
        }.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("discount", JsonUtil.getInstance().toJson(discount));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));
    }

    @Test
    public void showDiscountDetailWhenFinishDiscountActivity() {
        Spanned amountWithDiscount, totalAmount;
        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>() {
        }.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        Discount discount = StaticMock.getAmountOffDiscount();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        Intent discountResultIntent = new Intent();
        discountResultIntent.putExtra("discount", JsonUtil.getInstance().toJson(discount));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, discountResultIntent);
        intending(hasComponent(DiscountsActivity.class.getName())).respondWith(result);

        mFakeAPI.addResponseToQueue("", 404, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkDiscountRow)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkHasDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkHasDiscount)).perform(click());

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));
    }

    @Test
    public void showTotalAmountWhenDiscountsAreNotEnabled() {
        Discount discount = StaticMock.getPercentOffDiscount();

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));
        validStartIntent.putExtra("discountEnabled",false);

        mFakeAPI.addResponseToQueue(discount, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(getFormattedAmount(mAmount, mCurrencyId).toString())));
    }

    //Installments review
    @Test
    public void showInstallmentsReviewWithDiscountDetailWhenClickOnInstallmentRow() {
        Spanned amountWithDiscount, totalAmount;
        Discount discount = StaticMock.getPercentOffDiscount();
        PayerCost selectedPayerCost;

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue(discount, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        selectedPayerCost = installmentList.get(0).getPayerCosts().get(0);

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));

        onView(withId(R.id.mpsdkActivityInstallmentsView)).perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));

        onView(withId(R.id.mpsdkInstallmentsAmount)).check(matches(withText(formatInstallmentAmountText(selectedPayerCost, mCurrencyId).toString())));
        onView(withId(R.id.mpsdkReviewTotalAmount)).check(matches(withText(formatTotalAmountWithRateText(selectedPayerCost, mCurrencyId).toString())));
        onView(withId(R.id.mpsdkTeaPercent)).check(matches(withText(formatTEAPercentText(selectedPayerCost))));
        onView(withId(R.id.mpsdkCftpercent)).check(matches(withText(formatCFTPercentText(selectedPayerCost))));
    }

    @Test
    public void showTotalAmountInInstallmentsReviewWhenClickOnInstallmentRowWithoutDiscount() {
        PayerCost selectedPayerCost;

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));

        mFakeAPI.addResponseToQueue("", 404, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        selectedPayerCost = installmentList.get(0).getPayerCosts().get(0);

        onView(withId(R.id.mpsdkDiscountRow)).check(matches(isDisplayed()));

        onView(withId(R.id.mpsdkActivityInstallmentsView)).perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(getFormattedAmount(mAmount, mCurrencyId).toString())));
        onView(withId(R.id.mpsdkInstallmentsAmount)).check(matches(withText(formatInstallmentAmountText(selectedPayerCost, mCurrencyId).toString())));
        onView(withId(R.id.mpsdkReviewTotalAmount)).check(matches(withText(formatTotalAmountWithRateText(selectedPayerCost, mCurrencyId).toString())));
        onView(withId(R.id.mpsdkTeaPercent)).check(matches(withText(formatTEAPercentText(selectedPayerCost))));
        onView(withId(R.id.mpsdkCftpercent)).check(matches(withText(formatCFTPercentText(selectedPayerCost))));
    }

    @Test
    public void hideInstallmentsReviewWhenInstallmentsReviewIsNotEnabled() {
        Spanned amountWithDiscount, totalAmount;
        Discount discount = StaticMock.getPercentOffDiscount();

        PaymentPreference paymentPreference = new PaymentPreference();

        String installments = StaticMock.getInstallmentsJson();

        Type listType = new TypeToken<List<Installment>>(){}.getType();
        List<Installment> installmentList = JsonUtil.getInstance().getGson().fromJson(installments, listType);

        Issuer issuer = StaticMock.getIssuer();

        validStartIntent.putExtra("issuer", JsonUtil.getInstance().toJson(issuer));
        validStartIntent.putExtra("paymentPreference", JsonUtil.getInstance().toJson(paymentPreference));
        validStartIntent.putExtra("installmentsReviewEnabled", false);

        mFakeAPI.addResponseToQueue(discount, 200, "");
        mFakeAPI.addResponseToQueue(installmentList, 200, "");

        mTestRule.launchActivity(validStartIntent);

        amountWithDiscount = getFormattedAmount(mTestRule.getActivity().getPresenter().getDiscount().getAmountWithDiscount(mAmount), mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());
        totalAmount = getFormattedAmount(mAmount, mTestRule.getActivity().getPresenter().getDiscount().getCurrencyId());

        onView(withId(R.id.mpsdkHasDirectDiscount)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(isDisplayed()));
        onView(withId(R.id.mpsdkDiscountOff)).check(matches(withText(getDiscountOff(discount))));
        onView(withId(R.id.mpsdkDiscountAmount)).check(matches(withText(amountWithDiscount.toString())));
        onView(withId(R.id.mpsdkTotalAmount)).check(matches(withText(totalAmount.toString())));

        onView(withId(R.id.mpsdkActivityInstallmentsView)).perform(actionOnItemAtPosition(0, click()));

        Assert.assertTrue(mTestRule.getActivity().isFinishing());
    }

    private Spanned formatInstallmentAmountText(PayerCost payerCost, String currencyId) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(payerCost.getInstallments());
        stringBuilder.append(" ");
        stringBuilder.append(mTestRule.getActivity().getString(R.string.mpsdk_installments_by));
        stringBuilder.append(" ");
        stringBuilder.append(CurrenciesUtil.formatNumber(payerCost.getInstallmentAmount(), currencyId));

        Spanned spannedInstallmentsText = CurrenciesUtil.formatCurrencyInText(payerCost.getInstallmentAmount(),
                currencyId, stringBuilder.toString(), false, true);

        return spannedInstallmentsText;
    }

    private Spanned formatTotalAmountWithRateText(PayerCost payerCost, String currencyId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        stringBuilder.append(CurrenciesUtil.formatNumber(payerCost.getTotalAmount(), currencyId));
        stringBuilder.append(")");

        Spanned spannedFullAmountText = CurrenciesUtil.formatCurrencyInText(payerCost.getTotalAmount(),
                currencyId, stringBuilder.toString(), false, true);

        return spannedFullAmountText;
    }

    private String formatTEAPercentText(PayerCost payerCost) {
        String teaPercent = mTestRule.getActivity().getString(R.string.mpsdk_installments_tea) + " " + payerCost.getTEAPercent();

        return teaPercent;
    }

    private String formatCFTPercentText(PayerCost payerCost) {
        String cftPercent = mTestRule.getActivity().getString(R.string.mpsdk_installments_cft) + " " + payerCost.getCFTPercent();
        return cftPercent;
    }

    private String getDiscountOff(Discount discount) {
        if (discount.getAmountOff() != null && discount.getAmountOff().compareTo(BigDecimal.ZERO)>0) {
            Currency currency = CurrenciesUtil.getCurrency(discount.getCurrencyId());
            String amount = currency.getSymbol() + " " + discount.getAmountOff();
            return amount;
        } else {
            String discountOff = mTestRule.getActivity().getResources().getString(R.string.mpsdk_discount_percent_off,
                    String.valueOf(discount.getPercentOff()));
            return discountOff;
        }
    }

    private Spanned getFormattedAmount(BigDecimal amount, String currencyId) {
        String originalNumber = CurrenciesUtil.formatNumber(amount, currencyId);
        Spanned amountText = CurrenciesUtil.formatCurrencyInText(amount, currencyId, originalNumber, false, true);
        return amountText;
    }
}
