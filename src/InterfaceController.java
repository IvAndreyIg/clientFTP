import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class InterfaceController {
    //fxml



    public TextField transferPortField;
    public TextField portField;
    public TextField ipField;
    public TextField loginField;
    public PasswordField passField;

    public ChoiceBox commandChoiceBox;
    public TextArea logArea;
    public TableView tableFiles;
    public Pane workPane;
    public Pane connectionPane;

    public File selectedDirectory;
    public File selectedFile;


    public String fileName="";


    public Client client;

    public String selectedMode;

    public Map<Integer,String> modes=new HashMap<>();



    public Stage parentStage;

    final DirectoryChooser directoryChooser = new DirectoryChooser();
    final FileChooser fileChooser = new FileChooser();
    public Text fileField;
    public Text directoryField;
    public Button dirButton;

    public boolean connected=false;

    public TableColumn<? extends Object, ? extends Object> columnName;
    public Pane filePane;
    public Pane dirPane;
    public Text filePathField;
    public Button SignInButton;


    //заместо конструктора
    public void Init(Stage primaryStage) {


        SignInButton.setFocusTraversable(true);

        modes.put(0,"GET");
        modes.put(1,"DELETE");
        modes.put(2,"SEND");
        modes.put(3,"LIST");
        modes.put(4,"TEST");

        dirPane.setVisible(true);

        filePane.setVisible(false);


        parentStage=primaryStage;


        commandChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                changeMethod( newValue);

            }
        });


        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));


        tableFiles.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            //можно проверку и получше написать
            if (newSelection != null&&!selectedMode.equals("LIST")) {


                fileName =((FileName)newSelection).getName();

                fileField.setText(fileName);


                if(selectedDirectory!=null){

                    directoryField.setText(selectedDirectory.getAbsolutePath()+"\\"+fileName);
                }


            }
        });

        selectedMode=modes.get(0);

        commandChoiceBox.setValue("GET");

        workPane.setDisable(true);


    }


    public void clearFilesFields(){

        fileField.setText("");
        directoryField.setText("");

        filePathField.setText("");


        selectedDirectory=null;
        selectedFile=null;


        fileName="";

    }


    public  void changeMethod(Number newValue){


        clearFilesFields();



        selectedMode=modes.get(newValue);

        switch (selectedMode){

            case "GET":{

                fileField.setText(fileName);

                dirPane.setVisible(true);

                filePane.setVisible(false);

            };break;
            case "DELETE":{

                fileField.setText(fileName);

                dirPane.setVisible(false);

                filePane.setVisible(false);

            };break;
            case "SEND":{

                dirPane.setVisible(false);

                filePane.setVisible(true);

            };break;
            case "LIST":{


                fileField.setText("list.txt");

                dirPane.setVisible(true);

                filePane.setVisible(false);

            };break;
        }


    }


    public void sendAuthData(){







    }



    public void process(ActionEvent actionEvent) {




        switch (selectedMode){

            case "GET":{

                if(selectedDirectory!=null){

                    try {
                        client.sendCommandObject(selectedMode,selectedDirectory.getAbsolutePath()+"\\"+fileName,"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    logInLogger("\n------\n directory not selected \n------");

                }

            };break;
            case "DELETE":{

                if(!fileName.equals("")){

                    try {
                        client.sendCommandObject(selectedMode,"C:"+"\\"+fileName,"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    logInLogger("\n------\n file not selected \n------");

                }

            };break;
            case "SEND":{

                if(selectedFile!=null){

                    try {
                        client.sendCommandObject(selectedMode,selectedFile.getAbsolutePath(),"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    logInLogger("\n------\n path not selected \n------");

                }

            };break;
            case "LIST":{

                if(selectedDirectory!=null){

                    try {
                        client.sendCommandObject(selectedMode,selectedDirectory.getAbsolutePath(),"");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    logInLogger("\n------\n directory not selected \n------");

                }

            };break;
            case "TEST":{

                try {
                    client.sendCommandObject(selectedMode,"","");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            };break;
        }






    }


    public void logInLogger(String data){




        logArea.appendText("\n"+Tools.getDateTime());
        logArea.appendText(data);



    }



    public int getTransferPort() {
        return Integer.parseInt(transferPortField.getText());
    }
    public String getTransferAddress() {
        return ipField.getText();
    }




    public void refreshFilesList(ActionEvent actionEvent) {
        try {
            client.sendCommandObject("LIST","","refresh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setFilesList(List<FileName> filesList){


        tableFiles.getItems().clear();

        filesList.forEach(fileName->{

            tableFiles.getItems().add(fileName);



        });



    }


    public boolean connect() {

        Socket socket = new Socket();
        try {

            //подключаемся к серверу

            //192.168.100.15
            socket.connect(new InetSocketAddress(ipField.getText(), Integer.parseInt(portField.getText())),2000);
            //Для тестов
           // socket.connect(new InetSocketAddress("127.0.0.1", Integer.parseInt(portField.getText())),2000);


            client = new Client(socket,this);



            new Thread(client).start();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    //public TextField loginField;
    //    public PasswordField passField;

    public void signIn() {




        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run(){

                client.stepCkeck=1;

                client.sendAuthDataObject(loginField.getText(),passField.getText());
                System.out.println("Task #1 is running");

                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(client.stepCkeck==1){
                    signIn();
                }
            }
        });

        thread1.start();





    }

    public void logout(){




        tableFiles.getItems().clear();

        //ConnectButton.setText("Connect");
        connected= client.closeConnection();

        workPane.setDisable(true);

        Platform.runLater(() -> {

            SignInButton.setDisable(false);
            SignInButton.setText("Sign in");
        });


    }


    public void unConfirmSignIn() {


        workPane.setDisable(true);


        Platform.runLater(() -> {


            SignInButton.setDisable(false);
            SignInButton.setText("Sign in");



        });
    }

    public void confirmSignIn(){


        System.out.println("YES");


        workPane.setDisable(false);


        Platform.runLater(() -> {


            SignInButton.setDisable(false);
                SignInButton.setText("Logout");



        });
    }








    public void signInButClick(ActionEvent actionEvent) {

        SignInButton.setDisable(true);
        SignInButton.setText("Connection...");

        if (!connected){



//            Platform.runLater(() -> {
//
//
//            });
            connected=connect();
            if(connected){






                //

                //

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                signIn();
            }
            else {
                logInLogger("\n-----\n"+"connection error:\n\tconnect timed out"+"\n-----\n");

                Platform.runLater(() -> {

                    SignInButton.setDisable(false);
                    SignInButton.setText("Sign in");
                });
            }
        }
        else {


            if(client!=null){

                logInLogger("\n-----\n"+"\tlogouted by click\n-----\n");
                logout();

            }



        }

    }

    //выбираем путь к файлу для метода SEND
    public void choiceFile(ActionEvent actionEvent) {

        fileChooser.setTitle("Select file for send");
        selectedFile = fileChooser.showOpenDialog(parentStage);


        filePathField.setText(selectedFile.getAbsolutePath());




    }
    //Выбираем путь к директориии сохранения файлов для методов GET,LIST
    public void choiceDirectory(ActionEvent actionEvent) {
        directoryChooser.setTitle("Select directory for save");
        directoryChooser.setInitialDirectory(new File("/"));
        selectedDirectory = directoryChooser.showDialog(parentStage);



        directoryField.setText(selectedDirectory.getAbsolutePath()+"\\"+fileName);
        // System.out.println(selectedDirectory.getAbsolutePath());
        //utton button = new Button("Select Directory");


    }


    //очищаем логгер
    public void clearLoggerButClick(ActionEvent actionEvent) {


        logArea.clear();
    }



}
