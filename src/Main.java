import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args)  throws IOException, InterruptedException {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception{

        //Создаем загрузчик интерфейса
        FXMLLoader loader=new FXMLLoader();

        //устанавливаем загрузчику путь к документу и класс контроллер
        loader.setLocation(InterfaceController.class.getResource("MainInterface.fxml"));
        //Загружаем документ с интерфейсом
        Pane rootLayout = (Pane) loader.load();
        //Получаем контроллер интерфейса
        InterfaceController controllerWelcomeTableForm = loader.getController();
        //Инициализируем интерфейс
        controllerWelcomeTableForm.Init(primaryStage);

        //создаем новую сцену с панелью авторизации
        Scene scene = new Scene(rootLayout);

        primaryStage.setOnHidden(e -> {

            Platform.exit();
        });


        //Задаем окну сцену с панелью, которая будет отображена
        primaryStage.setScene(scene);
        //задаем окну заголовок(необязательно)
        primaryStage.setTitle("Авторизация");
        //Отображение окна
        primaryStage.show();


    }
}
