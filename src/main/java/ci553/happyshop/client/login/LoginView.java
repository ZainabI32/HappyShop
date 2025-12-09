package ci553.happyshop.client.login;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginView {

    public void start(Stage stage, LoginController controller) {

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();

        Button loginButton = new Button("Login");
        Label message = new Label();

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(loginButton, 1, 2);
        grid.add(message, 1, 3);

        loginButton.setOnAction(e -> {
            boolean success = controller.handleLogin(
                    userField.getText(), passField.getText()
            );

            if (!success) {
                message.setText("Invalid login.");
            } else {
                stage.close();
            }
        });

        Scene scene = new Scene(grid, 350, 180);
        stage.setScene(scene);
        stage.setTitle("HappyShop Login");
        stage.show();
    }
}