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
        //mock för BankInterface
        bank = mock(BankInterface.class);
        atm = new ATM(bank);
    }

    @Test
    @DisplayName("Testar att användaren hämtas korrekt baserat på kort-ID")
    void testInsertCard() {
        // Testar insättning av kort och att rätt användare hämtas från banken
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);       // Simulerar att rätt användare returneras
        when(bank.isCardLocked(userId)).thenReturn(false);         // Simulerar att kortet inte är låst

        boolean result = atm.insertCard(userId);

        // Bekräftar att kortet sätts in och att användaren identifieras korrekt
        assertTrue(result, "Kortet borde sättas in och användaren hämtas");
        assertEquals(mockUser, atm.getCurrentUser(), "Användaren borde vara den hämtade");
    }

    @Test
    @DisplayName("Testar inmatning av felaktig PIN-kod")
    void testEnterIncorrectPin() {
        // Testar om en felaktig PIN ökar antalet felaktiga försök
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        boolean result = atm.enterPin("wrongPin");    // Simulerar en felaktig PIN-inmatning

        // Verifierar att fel PIN-kod inte tillåter inloggning och att misslyckade försök ökas
        assertFalse(result, "Inmatningen av fel PIN-kod ska returnera false");
        assertEquals(1, mockUser.getFailedAttempts(), "Antalet misslyckade försök ska öka med 1");
    }

    @Test
    @DisplayName("Testar inmatning av korrekt PIN-kod")
    void testEnterCorrectPin() {
        // Skapar en mockad användare och sätter förväntad PIN-kod
        String userId = "user123";
        String correctPin = "1234";
        User mockUser = new User(userId, correctPin, 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);    // Simulerar att rätt användare returneras
        when(bank.isCardLocked(userId)).thenReturn(false);      // Simulerar att kortet inte är låst

        // Försöker sätta in kortet och ange korrekt PIN-kod
        atm.insertCard(userId);
        boolean result = atm.enterPin(correctPin);

        // Verifierar att PIN-koden accepterats och att inloggningen lyckats
        assertTrue(result, "Inmatningen av korrekt PIN-kod ska returnera true");
        assertEquals(0, mockUser.getFailedAttempts(), "Antalet misslyckade försök ska vara noll efter korrekt inmatning");
        assertEquals(mockUser, atm.getCurrentUser(), "Den aktuella användaren ska vara den inloggade användaren");
    }

    @Test
    @DisplayName("Testar låsning av kort efter 3 misslyckade försök")
    void testCardLockAfterThreeAttempts() {
        // Testar om kortet låser efter tre felaktiga försök
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("wrongPin");
        atm.enterPin("wrongPin");
        atm.enterPin("wrongPin");    // Tredje misslyckade försök, kortet ska låsas

        // Verifierar att kortet är låst efter tre felaktiga försök
        assertTrue(mockUser.isLocked(), "Kortet ska vara låst efter tre misslyckade försök");
    }

    @Test
    @DisplayName("Testar hantering av ett kort som redan är låst")
    void testLockedCard() {
        // Skapar en mockad användare och ställer in misslyckade försök till 3 innan kortet låses
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        mockUser.incrementFailedAttempts(); // Första misslyckade försöket
        mockUser.incrementFailedAttempts(); // Andra misslyckade försöket
        mockUser.incrementFailedAttempts(); // Tredje misslyckade försöket
        mockUser.lockCard(); // Låser kortet efter 3 misslyckade försök

        // Mockar bankens beteende för att returnera den låsta användaren
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(true);  // Returnerar att kortet är låst

        // Försöker sätta in kortet
        boolean result = atm.insertCard(userId);

        // Verifierar att kortet inte tillåts användas
        assertFalse(result, "Inmatning av ett låst kort ska misslyckas");
        assertNull(atm.getCurrentUser(), "Ingen användare ska vara inloggad när kortet är låst");

        // Försöker ändå ange PIN och kontrollerar att det inte påverkar status
        boolean pinResult = atm.enterPin("1234");
        assertFalse(pinResult, "Inmatning av PIN-kod ska inte tillåtas om kortet är låst");
        assertEquals(3, mockUser.getFailedAttempts(), "Antalet misslyckade försök ska vara oförändrat på 3 för ett låst kort");
    }

    @Test
    @DisplayName("Testar korrekt saldo")
    void testCheckBalance() {
        // Testar om rätt saldo returneras efter inloggning
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234");  // Rätt PIN-kod för att logga in

        double balance = atm.checkBalance();

        // Verifierar att saldot är korrekt efter inloggning
        assertEquals(1000.0, balance, "Saldo ska vara 1000.0");
    }

    @Test
    @DisplayName("Testar insättning av pengar")
    void testDeposit() {
        // Testar insättning av pengar på användarens konto
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234");  // Rätt PIN-kod för att logga in

        atm.deposit(500.0);  // Försöker sätta in 500.0
        assertEquals(1500.0, mockUser.getBalance(), "Saldo ska vara 1500.0 efter insättning");
    }

    @Test
    @DisplayName("Testar uttag av pengar")
    void testWithdraw() {
        // Testar uttag av pengar när användaren har tillräckligt saldo
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234");  // Rätt PIN-kod för att logga in

        boolean result = atm.withdraw(200.0);

        // Verifierar att uttaget lyckas och att saldot minskar korrekt
        assertTrue(result, "Uttag ska lyckas om saldo är tillräckligt");
        assertEquals(800.0, mockUser.getBalance(), "Saldo ska vara 800.0 efter uttag");
    }

    @Test
    @DisplayName("Testar uttag med otillräckligt saldo")
    void testWithdrawInsufficientBalance() {
        // Testar uttag av ett belopp som överstiger kontosaldot
        String userId = "user123";
        User mockUser = new User(userId, "1234", 1000.0);
        when(bank.getUserById(userId)).thenReturn(mockUser);
        when(bank.isCardLocked(userId)).thenReturn(false);

        atm.insertCard(userId);
        atm.enterPin("1234");  // Rätt PIN-kod för att logga in

        boolean result = atm.withdraw(1200.0);  // Försöker ta ut mer än vad som finns på kontot

        // Verifierar att uttaget misslyckas och att saldot är oförändrat
        assertFalse(result, "Uttag ska misslyckas om saldo är otillräckligt");
        assertEquals(1000.0, mockUser.getBalance(), "Saldo ska fortfarande vara 1000.0 efter misslyckat uttag");
    }
}