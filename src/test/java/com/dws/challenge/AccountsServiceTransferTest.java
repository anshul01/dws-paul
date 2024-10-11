package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountsServiceTransferTest {

    @Autowired
    private AccountsService accountsService;

    @MockBean
    private NotificationService notificationService;

/*    @BeforeEach
    void setUp() {
        notificationService = Mockito.mock(NotificationService.class);
        accountsService = new AccountsService(accountsService.getAccountsRepository(), notificationService);
    }*/

    @Test
    void transferMoney() {
        Account accountFrom = new Account("Id-123", new BigDecimal("1000"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));
        accountsService.createAccount(accountFrom);
        accountsService.createAccount(accountTo);
        accountsService.transferMoney("Id-123", "Id-456", new BigDecimal("200"));
        assertThat(accountsService.getAccount("Id-123").getBalance()).isEqualByComparingTo("800");
        assertThat(accountsService.getAccount("Id-456").getBalance()).isEqualByComparingTo("700");

        verify(notificationService).notifyAboutTransfer(accountFrom, "Debited amount: 200 from account: Id-123");
        verify(notificationService).notifyAboutTransfer(accountTo, "Credited amount: 200 to account: Id-456");
    }

    @Test
    void transferMoney_insufficientFunds() {
        Account accountFrom = new Account("Id-123", new BigDecimal("100"));
        Account accountTo = new Account("Id-456", new BigDecimal("500"));
        accountsService.createAccount(accountFrom);
        accountsService.createAccount(accountTo);

        assertThrows(IllegalArgumentException.class, () -> {
            accountsService.transferMoney("Id-123", "Id-456", new BigDecimal("200"));
        });

        assertThat(accountsService.getAccount("Id-123").getBalance()).isEqualByComparingTo("100");
        assertThat(accountsService.getAccount("Id-456").getBalance()).isEqualByComparingTo("500");
    }
}