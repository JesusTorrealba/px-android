package com.mercadopago.examples.v3.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mercadopago.callbacks.PaymentDataCallback;
import com.mercadopago.callbacks.PaymentResultCallback;
import com.mercadopago.constants.PaymentTypes;
import com.mercadopago.constants.Sites;
import com.mercadopago.core.MercadoPagoCheckout;
import com.mercadopago.examples.reviewables.CellphoneReview;
import com.mercadopago.examples.reviewables.CongratsReview;
import com.mercadopago.examples.utils.ExamplesUtils;
import com.mercadopago.exceptions.MercadoPagoError;
import com.mercadopago.model.Item;
import com.mercadopago.model.Payment;
import com.mercadopago.model.PaymentData;
import com.mercadopago.model.PaymentResult;
import com.mercadopago.preferences.CheckoutPreference;
import com.mercadopago.preferences.FlowPreference;
import com.mercadopago.preferences.PaymentResultScreenPreference;
import com.mercadopago.preferences.ReviewScreenPreference;
import com.mercadopago.examples.R;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mreverter on 3/7/17.
 */

public class CallbacksResponseTestActivity extends AppCompatActivity {
    private Activity mActivity;
    private ProgressBar mProgressBar;
    private View mRegularLayout;

    private String mPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callbacks_example);
        mActivity = this;
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRegularLayout = findViewById(R.id.regularLayout);
        mPublicKey = ExamplesUtils.DUMMY_MERCHANT_PUBLIC_KEY;
    }

    public void onContinueClicked(View view) {
        startMercadoPagoCheckout();
    }

    private void startMercadoPagoCheckout() {
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("company_id", "movistar");
        additionalInfo.put("phone_number", "111111");

        String languageToLoad  = "pt"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);

        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .disableDiscount()
                .disableBankDeals()
                .disableInstallmentsReviewScreen()
                .build();

        new MercadoPagoCheckout.Builder()
                .setActivity(this)
                .setPublicKey(mPublicKey)
                .setCheckoutPreference(getCheckoutPreference())
                .setFlowPreference(flowPreference)
                .start(new PaymentDataCallback() {
                    @Override
                    public void onSuccess(PaymentData paymentData, boolean paymentMethodChanged) {

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Success! " + paymentData.getPaymentMethod().getId() + " selected. ");
                        if(paymentMethodChanged) {
                            stringBuilder.append("And it has changed!");
                        }
                        Toast.makeText(mActivity, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                        startRyC(paymentData);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(mActivity, "Cancel callback", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(MercadoPagoError error) {
                        Log.d("log", "failure");
                    }
                });
    }

    private void startRyC(PaymentData paymentData) {

        CellphoneReview cellphoneReview = new CellphoneReview(this, "15111111");

        ReviewScreenPreference reviewScreenPreference = new ReviewScreenPreference.Builder()
                .setTitle("Confirma tu recarga")
                .setConfirmText("Recargar")
                .setCancelText("Ir a Actividad")
                .setProductDetail("Recarga de celular")
                .addReviewable(cellphoneReview)
                .build();

        FlowPreference flowPreference = new FlowPreference.Builder()
                .disableReviewAndConfirmScreen()
                .disableBankDeals()
                .disableDiscount()
                .disableInstallmentsReviewScreen()
                .build();

        new MercadoPagoCheckout.Builder()
                .setActivity(this)
                .setReviewScreenPreference(reviewScreenPreference)
                .setPublicKey(mPublicKey)
                .setCheckoutPreference(getCheckoutPreference())
                .setFlowPreference(flowPreference)
                .setPaymentData(paymentData)
                .start(new PaymentDataCallback() {
                    @Override
                    public void onSuccess(PaymentData paymentData, boolean paymentMethodChanged) {

                        Toast.makeText(mActivity, getSuccessMessage(paymentData, paymentMethodChanged), Toast.LENGTH_SHORT).show();

                        if (paymentMethodChanged) {
                            startRyC(paymentData);
                        } else {
                            startWithPaymentResult(paymentData);
                        }
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(mActivity, "Cancel callback", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(MercadoPagoError error) {
                        Toast.makeText(mActivity, "Error callback: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private CheckoutPreference getCheckoutPreference() {
        return new CheckoutPreference.Builder()
                .addItem(new Item("Item", BigDecimal.TEN.multiply(BigDecimal.TEN)))
                .setSite(Sites.ARGENTINA)
                .addExcludedPaymentType(PaymentTypes.ATM)
                .addExcludedPaymentType(PaymentTypes.BANK_TRANSFER)
                .addExcludedPaymentType(PaymentTypes.DEBIT_CARD)
                .addExcludedPaymentType(PaymentTypes.DEBIT_CARD)
                .addExcludedPaymentType(PaymentTypes.TICKET)
                .enableAccountMoney()
                .setPayerAccessToken("APP_USR-6077407713835188-120612-9c010367e2aba8808865b227526f4ccc__LB_LD__-232134231")
                .build();
    }

    private String getSuccessMessage(PaymentData paymentData, boolean paymentMethodChanged) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Success! " + paymentData.getPaymentMethod().getId() + " selected. ");
        if(paymentMethodChanged) {
            stringBuilder.append("And it has changed!");
        }
        return stringBuilder.toString();
    }

    private void startWithPaymentResult(PaymentData paymentData) {

        CongratsReview congratsReview = new CongratsReview(this, "Hola!");

        PaymentResult paymentResult = new PaymentResult.Builder()
                .setPaymentData(paymentData)
                .setPaymentStatus(Payment.StatusCodes.STATUS_APPROVED)
//                .setPaymentStatus(Payment.StatusCodes.STATUS_PENDING)
//                .setPaymentStatus(Payment.StatusCodes.STATUS_REJECTED)
//                .setPaymentStatusDetail(Payment.StatusCodes.STATUS_DETAIL_CC_REJECTED_BAD_FILLED_CARD_NUMBER)
                .build();

        PaymentResultScreenPreference paymentResultScreenPreference = new PaymentResultScreenPreference.Builder()
                .setApprovedTitle("Recargaste!")
                .setApprovedSecondaryExitButton("Intentar nuevamente", new PaymentResultCallback() {
                    @Override
                    public void onResult(PaymentResult paymentResult) {
                        Toast.makeText(mActivity, "Secondary exit", Toast.LENGTH_SHORT).show();
                    }
                })
                .addCongratsReviewable(congratsReview)
                .setExitButtonTitle("Ir a Actividad")
                .build();


        new MercadoPagoCheckout.Builder()
                .setActivity(this)
                .setPublicKey(mPublicKey)
                .setCheckoutPreference(getCheckoutPreference())
                .setPaymentResult(paymentResult)
                .setPaymentResultScreenPreference(paymentResultScreenPreference)
                .start(new PaymentDataCallback() {
                    @Override
                    public void onSuccess(PaymentData paymentData, boolean paymentMethodChanged) {
                        Toast.makeText(mActivity, getSuccessMessage(paymentData, paymentMethodChanged), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(mActivity, "Cancel callback", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(MercadoPagoError error) {
                        Toast.makeText(mActivity, "Error callback: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        LayoutUtil.showRegularLayout(this);
//
//        if (requestCode == MercadoPagoCheckout.CHECKOUT_REQUEST_CODE) {
//            if (resultCode == MercadoPagoCheckout.PAYMENT_DATA_RESULT_CODE) {
//                PaymentData paymentData = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentData"), PaymentData.class);
//                if (mAlreadyStartedRyC) {
//                    Toast.makeText(mActivity, "Restart en Resultados", Toast.LENGTH_SHORT).show();
//                    startWithPaymentResult(paymentData);
//                } else {
//                    mAlreadyStartedRyC = true;
//                    Toast.makeText(mActivity, "Restart en RyC", Toast.LENGTH_SHORT).show();
//                    startRyC(paymentData);
//                }
//
//            } else if (resultCode == RESULT_CANCELED) {
//
//                if (data != null && data.getStringExtra("mercadoPagoError") != null) {
//                    MercadoPagoError mercadoPagoError = JsonUtil.getInstance().fromJson(data.getStringExtra("mercadoPagoError"), MercadoPagoError.class);
//                    Log.d("log", "RESULT_CANCELED - " + mercadoPagoError.getMessage());
//                } else {
//                    Log.d("log", "RESULT_CANCELED");
//                }
//
//            } else if (resultCode == CellphoneReview.CELLPHONE_CHANGE) {
//                //mPaymentData = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentData"), PaymentData.class);
//
//            } else if (resultCode == CongratsReview.CUSTOM_REVIEW) {
//                Payment payment = JsonUtil.getInstance().fromJson(data.getStringExtra("payment"), Payment.class);
//                PaymentResult paymentResult = JsonUtil.getInstance().fromJson(data.getStringExtra("paymentResult"), PaymentResult.class);
//            } else if (resultCode == RESULT_CUSTOM_EXIT) {
//                Toast.makeText(mActivity, "Hola custom exit!", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        showRegularLayout();
    }

    private void showRegularLayout() {
        mProgressBar.setVisibility(View.GONE);
        mRegularLayout.setVisibility(View.VISIBLE);
    }
}
