import java.util.HashMap;
import java.util.Map;

public class Bank implements BankInterface {
    private Map<String, User> users = new HashMap<>();

    public Bank() {
        users.put("user123", new User("user123", "1234", 1000.0));
    }

    @Override
    public User getUserById(String id) {
        return users.get(id);
    }

    @Override
    public boolean isCardLocked(String userId) {
        User user = users.get(userId);
        return user != null && user.isLocked();
    }

    public static String getBankName() {
        return "MockBank";
    }
}
