package com.picpaysimulation.services;

import com.picpaysimulation.domain.user.User;
import com.picpaysimulation.domain.user.UserType;
import com.picpaysimulation.dtos.TransactionDTO;
import com.picpaysimulation.repositories.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private TransactionRepository repository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthorizationService authorizationService;

    @Autowired
    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @DisplayName("Should create Transaction successfully when everything is OK")
    void createTransactionCase1() throws Exception {
        User sender = new User(1L, "Davi", "Marinho", "99999999901", "davi@gmail.com", "asdasda", new BigDecimal(10), UserType.COMMON);
        User receiver = new User(2L, "Vinicius", "Marinho", "99999999902", "vinicius@gmail.com", "dsadsad", new BigDecimal(10), UserType.MERCHANT);

        when(userService.findUserbyId(1L)).thenReturn(sender);
        when(userService.findUserbyId(2L)).thenReturn(receiver);

        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(true);

        TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
        transactionService.createTransaction(request);

        verify(repository, times(1)).save(any());

        sender.setBalance(new BigDecimal(0));
        verify(userService, times(1)).saveUser(sender);

        receiver.setBalance(new BigDecimal(20));
        verify(userService, times(1)).saveUser(receiver);

        verify(notificationService, times(1)).sendNotification(sender, "Transação realizada com sucesso.");
        verify(notificationService, times(1)).sendNotification(receiver, "Transação recebida com sucesso.");
    }

    @Test
    @DisplayName("Should throw Exception when Transaction is unauthorized")
    void createTransactionCase2() throws Exception {
        User sender = new User(1L, "Davi", "Marinho", "99999999901", "davi@gmail.com", "asdasda", new BigDecimal(10), UserType.COMMON);
        User receiver = new User(2L, "Vinicius", "Marinho", "99999999902", "vinicius@gmail.com", "dsadsad", new BigDecimal(10), UserType.MERCHANT);

        when(userService.findUserbyId(1L)).thenReturn(sender);
        when(userService.findUserbyId(2L)).thenReturn(receiver);

        when(authorizationService.authorizeTransaction(any(), any())).thenReturn(false); // will throw an exception

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            TransactionDTO request = new TransactionDTO(new BigDecimal(10), 1L, 2L);
            transactionService.createTransaction(request);
        }); // this is a catcher

        Assertions.assertEquals("Transação não autorizada.", thrown.getMessage());
    }
}