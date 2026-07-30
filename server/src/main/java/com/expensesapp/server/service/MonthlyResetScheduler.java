package com.expensesapp.server.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expensesapp.server.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyResetScheduler {

    private final UserRepository userRepository;
    // Cron syntax: Second, Minute, Hour, Day of Month, Month, Day of Week
    // "0 0 0 1 * *" fires exactly at 12:00:00 AM on day 1 of every month
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetMonthlyCounters() {
        log.info("Starting automated monthly expense tracking counters reset job...");
        userRepository.resetAllMonthlyExpenses();
        log.info("Successfully reset all user monthly expense tracking balances to 0.00.");
    }
}