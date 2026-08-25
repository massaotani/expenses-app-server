package com.expensesapp.server.service.card;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.CardRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Card;
import com.expensesapp.server.model.Expense;
import com.expensesapp.server.model.User;
import com.expensesapp.server.repository.CardRepository;
import com.expensesapp.server.repository.ExpenseRepository;
import com.expensesapp.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Card> getMyCards(AuthUser authUser) {
        validateUserProfile(authUser);
        return cardRepository.findByUser_Id(authUser.getUserProfile().getId());
    }

    @Override
    @Transactional
    public Card createCard(CardRequest request, AuthUser authUser) {
        validateUserProfile(authUser);

        System.out.println("--> Card creation started for auth user: " + authUser.getEmail());

        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        Card card = Card.builder()
                .name(request.getName())
                .cardType(request.getCardType())
                .user(user)
                .build();

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public Card updateCard(UUID id, CardRequest request, AuthUser authUser) {
        validateUserProfile(authUser);

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        if (!card.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }

        card.setName(request.getName());
        card.setCardType(request.getCardType());

        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(UUID id, AuthUser authUser) {
        validateUserProfile(authUser);

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found"));

        if (!card.getUser().getId().equals(authUser.getUserProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized action");
        }

        List<Expense> expenses = expenseRepository.findByCard_Id(id);
        for (Expense expense : expenses) {
            expense.setCard(null);
        }
        expenseRepository.saveAll(expenses);

        cardRepository.delete(card);
    }

    private void validateUserProfile(AuthUser authUser) {
        if (authUser == null || authUser.getUserProfile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User account is missing a profile link.");
        }
    }
}
