package ci553.happyshop.client.login;

public class LoginController {

    private final LoginModel loginModel;
    private Runnable onLoginSuccess;

    public LoginController(LoginModel loginModel) {
        this.loginModel = loginModel;
    }

    public void setOnLoginSuccess(Runnable r) {
        this.onLoginSuccess = r;
    }

    public boolean handleLogin(String user, String pass) {
        boolean ok = loginModel.authenticate(user, pass);
        if (ok && onLoginSuccess != null) {
            onLoginSuccess.run();
        }
        return ok;
    }
}
