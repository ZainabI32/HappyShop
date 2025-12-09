package ci553.happyshop.client.login;

public class LoginModel {
    // Temporary hardcoded login
    public boolean authenticate(String username, String password) {
        return "admin".equals(username) && "password123".equals(password);
    }
}
