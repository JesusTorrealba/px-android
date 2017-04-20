package com.mercadopago.uicontrollers.savedcards;

import android.view.View;

import com.mercadopago.uicontrollers.CustomViewController;

/**
 * Created by mromar on 4/19/17.
 */

public interface CustomerCardViewController extends CustomViewController {

    void draw();
    void setOnClickListener(View.OnClickListener listener);
    boolean hasActionMessage();
}
