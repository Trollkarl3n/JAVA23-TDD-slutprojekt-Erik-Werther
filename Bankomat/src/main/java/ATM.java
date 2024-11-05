public class ATM {
    private BankInterface bank;
    private User currentUser;

    public ATM(BankInterface bank) {
        this.bank = bank;
    }

    public boolean insertCard(String userId) {
        if (bank.isCardLocked(userId)) {
            System.out.println("Kortet är låst.");
            return false;
        }

        currentUser = bank.getUserById(userId);
        return currentUser != null;
    }

    public boolean enterPin(String pin) {
        if (currentUser != null && currentUser.getPin().equals(pin)) {
            currentUser.resetFailedAttempts();
            return true;
        } else {
            if (currentUser != null) {
                currentUser.incrementFailedAttempts();
                if (currentUser.getFailedAttempts() >= 3) {
                    currentUser.lockCard();
                    System.out.println("Kortet har låsts efter 3 misslyckade försök.");
                } else {
                    System.out.println("Fel PIN. Du har " + (3 - currentUser.getFailedAttempts()) + " försök kvar.");
                }
            }
            return false;
        }
    }

    public double checkBalance() {
        return currentUser != null ? currentUser.getBalance() : 0;
    }

    public void deposit(double amount) {
        if (currentUser != null) {
            currentUser.deposit(amount);
        }
    }

    public boolean withdraw(double amount) {
        if (currentUser != null && currentUser.getBalance() >= amount) {
            currentUser.withdraw(amount);
            return true;
        } else {
            System.out.println("Otillräckligt saldo.");
            return false;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}