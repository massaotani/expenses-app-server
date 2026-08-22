package com.expensesapp.server.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.expensesapp.server.dto.CardRequest;
import com.expensesapp.server.model.AuthUser;
import com.expensesapp.server.model.Card;
import com.expensesapp.server.service.card.CardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<Card>> getMyCards(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(cardService.getMyCards(authUser));
    }

    @PostMapping
    public ResponseEntity<Card> createCard(@Valid @RequestBody CardRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(cardService.createCard(request, authUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Card> updateCard(@PathVariable UUID id, @Valid @RequestBody CardRequest request, @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(cardService.updateCard(id, request, authUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable UUID id, @AuthenticationPrincipal AuthUser authUser) {
        cardService.deleteCard(id, authUser);
        return ResponseEntity.noContent().build();
    }
}