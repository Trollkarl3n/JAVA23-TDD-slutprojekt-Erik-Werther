import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ATMTest {
    private ATM atm;
    private BankInterface bank;

    @BeforeEach
    void setUp() {
        bank = mock(BankInterface.class);
        atm = new ATM(bank);
    }

    @Test
    @DisplayName("Testar att användaren hämtas korrekt baserat på kort-ID")
    void testInsertCard() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        boolean result = atm.insertCard(userId);

        assertTrue(result, "Kortet borde sättas in och användaren hämtas");
        assertEquals(mockUser, atm.getCurrentUser(), "Användaren borde vara den hämtade");
    }

    @Test
    @DisplayName("Testar inmatning av felaktig PIN-kod")
    void testEnterIncorrectPin() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        boolean result = atm.enterPin("wrongPin");

        assertFalse(result, "Inmatningen av fel PIN-kod ska returnera false");
        assertEquals(1, mockUser.getFailedAttempts(), "Antalet misslyckade försök ska öka med 1");
    }

    @Test
    @DisplayName("Testar låsning av kort efter 3 misslyckade försök")
    void testCardLockAfterThreeAttempts() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("wrongPin");
        atm.enterPin("wrongPin");
        atm.enterPin("wrongPin"); // Tredje misslyckade försök

        assertTrue(mockUser.isLocked(), "Kortet ska vara låst efter tre misslyckade försök");
    }

    @Test
    @DisplayName("Testar korrekt saldo")
    void testCheckBalance() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234"); // Korrekt PIN

        double balance = atm.checkBalance();

        assertEquals(1000.0, balance, "Saldo ska vara 1000.0");
    }

    @Test
    @DisplayName("Testar insättning av pengar")
    void testDeposit() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234"); // Korrekt PIN

        atm.deposit(500.0);
        assertEquals(1500.0, mockUser.getBalance(), "Saldo ska vara 1500.0 efter insättning");
    }

    @Test
    @DisplayName("Testar uttag av pengar")
    void testWithdraw() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234"); // Korrekt PIN

        boolean result = atm.withdraw(200.0);
        assertTrue(result, "Uttag ska lyckas om saldo är tillräckligt");
        assertEquals(800.0, mockUser.getBalance(), "Saldo ska vara 800.0 efter uttag");
    }

    @Test
    @DisplayName("Testar uttag med otillräckligt saldo")
    void testWithdrawInsufficientBalance() {
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234"); // Korrekt PIN

        boolean result = atm.withdraw(1200.0);
        assertFalse(result, "Uttag ska misslyckas om saldo är otillräckligt");
        assertEquals(1000.0, mockUser.getBalance(), "Saldo ska fortfarande vara 1000.0 efter misslyckat uttag");
    }
}