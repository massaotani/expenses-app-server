package com.expensesapp.server.service.card;

import java.util.List;
import java.util.UUID;

import com.expensesapp.server.dto.CardRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Card;

public interface CardService {

    List<Card> getMyCards(AuthUser authUser);

    Card createCard(CardRequest request, AuthUser authUser);

    Card updateCard(UUID id, CardRequest request, AuthUser authUser);

    void deleteCard(UUID id, AuthUser authUser);
}
