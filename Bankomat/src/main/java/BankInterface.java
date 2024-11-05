public interface BankInterface {
    public User getUserById(String id);
    public boolean isCardLocked(String userId);
}
