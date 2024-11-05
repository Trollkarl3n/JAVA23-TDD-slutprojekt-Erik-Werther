public interface BankInterface {
    User getUserById(String id);
    boolean isCardLocked(String userId);
}
