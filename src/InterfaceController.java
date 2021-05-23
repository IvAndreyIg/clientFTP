import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class InterfaceController {
    //fxml

    public TextField transferPortField;
    public TextField portField;
    public TextField ipField;
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
    public Button ConnectButton;
    public TableColumn<? extends Object, ? extends Object> columnName;
    public Pane filePane;
    public Pane dirPane;
    public Text filePathField;


    public void Init(Stage primaryStage) {

        modes.put(0,"GET");
        modes.put(1,"DELETE");
        modes.put(2,"SEND");
        modes.put(3,"LIST");

        dirPane.setVisible(true);

        filePane.setVisible(false);


        parentStage=primaryStage;


        commandChoiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("old Value:"+oldValue+" new:"+newValue);

                selectedMode=modes.get(newValue);

                switch (selectedMode){

                    case "GET":{

                        dirPane.setVisible(true);

                        filePane.setVisible(false);

                    };break;
                    case "DELETE":{

                        dirPane.setVisible(false);

                        filePane.setVisible(false);

                    };break;
                    case "SEND":{

                        dirPane.setVisible(false);

                        filePane.setVisible(true);

                    };break;
                    case "LIST":{

                        dirPane.setVisible(true);

                        filePane.setVisible(false);

                    };break;
                }



            }
        });


        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));


        tableFiles.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {


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
                    logInLoger("\n------\n directory not selected \n------");

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
                    logInLoger("\n------\n file not selected \n------");

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
                    logInLoger("\n------\n path not selected \n------");

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
                    logInLoger("\n------\n directory not selected \n------");

                }

            };break;
        }






    }


    public void logInLoger(String data){
        logArea.appendText(data);

    }



    public void refreshFilesList(ActionEvent actionEvent) {
        try {
            client.sendCommandObject("LIST","","refresh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setFilesList(List<FileName> filesList){

        filesList.forEach(fileName->{

            tableFiles.getItems().add(fileName);



        });



    }


    public boolean connect() {

        Socket socket = new Socket();
        try {

            //подключаемся к серверу
            socket.connect(new InetSocketAddress(ipField.getText(), Integer.parseInt(portField.getText())),2000);

             client = new Client(socket,this);



            new Thread(client).start();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }






//    public void choiceBoxClick(ContextMenuEvent contextMenuEvent) {
//    }

    public void connectButClick(ActionEvent actionEvent) {

        if (!connected){
            connected=connect();
            ConnectButton.setText("Disconnect");
            workPane.setDisable(false);
        }
        else {
            if(client!=null){
                connected= client.closeConnection();
            ConnectButton.setText("Connect");
            }
            workPane.setDisable(true);
        }

    }

    public void choiceFile(ActionEvent actionEvent) {

        fileChooser.setTitle("Select file for send");
        selectedFile = fileChooser.showOpenDialog(parentStage);


        filePathField.setText(selectedFile.getAbsolutePath());




    }
    public void choiceDirectory(ActionEvent actionEvent) {
        directoryChooser.setTitle("Select directory for save");
        directoryChooser.setInitialDirectory(new File("/"));
        selectedDirectory = directoryChooser.showDialog(parentStage);



        directoryField.setText(selectedDirectory.getAbsolutePath()+"\\"+fileName);
        // System.out.println(selectedDirectory.getAbsolutePath());
        //utton button = new Button("Select Directory");


    }
}
