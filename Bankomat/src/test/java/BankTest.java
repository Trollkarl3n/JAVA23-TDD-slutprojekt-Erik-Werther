import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankTest {

    @Test
    @DisplayName("Testar att bankens namn hämtas korrekt")
    void testBankName() {
        String bankName = Bank.getBankName();
        assertEquals("MockBank", bankName, "Banknamnet borde visas korrekt");
    }
}