package cn.xanderye;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @author XanderYe
 * @date 2020/2/6
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setTitle("慈溪市口罩发放预约程序@XanderYe");
        primaryStage.setScene(new Scene(root, 380, 500));
        primaryStage.show();
    }
}
