package com.mercadopago.mocks;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mercadopago.model.Card;
import com.mercadopago.utils.ResourcesUtil;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by mromar on 4/17/17.
 */

public class Cards {

    private Cards() {}

    public static List<Card> getCardsMLA() {
        List<Card> cards;
        String json = ResourcesUtil.getStringResource("cards_MLA.json");

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Card>>(){}.getType();
        cards = gson.fromJson(json, listType);

        return cards;
    }
}
