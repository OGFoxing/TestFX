package kz.ogfox.testfx;

import com.aquafx_project.AquaFx;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("TestFX");
        AquaFx.styleStage(primaryStage, StageStyle.DECORATED);
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();

        AquaFx.style();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
