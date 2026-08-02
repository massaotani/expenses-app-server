package com.expensesapp.server.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.expensesapp.server.dto.CardRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Card;
import com.expensesapp.server.model.User;
import com.expensesapp.server.repository.CardRepository;
import com.expensesapp.server.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Card> addCard(
            @Valid @RequestBody CardRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        
        User user = userRepository.findById(authUser.getUserProfile().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Card card = Card.builder()
                .name(request.getName())
                .cardType(request.getCardType())
                .currentBalance(request.getCurrentBalance())
                .user(user)
                .build();

        return ResponseEntity.ok(cardRepository.save(card));
    }

    @GetMapping
    public ResponseEntity<List<Card>> getMyCards(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(cardRepository.findByUser_Id(authUser.getUserProfile().getId()));
    }
}