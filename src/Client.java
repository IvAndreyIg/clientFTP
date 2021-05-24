import javafx.scene.control.TextArea;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Client implements Runnable {

    static int port = 5217;
   // static int transferPort = 5218;
    private final InterfaceController interfaceController;

    Socket mainSocket;


    Socket transferSocket;




    boolean requestInProcessing=true;


    String secondArgument="";

    ObjectOutputStream dout;
    ObjectInputStream din;
    private volatile boolean inConnection;


    String filesDir="clientFiles/";


    public boolean closeConnection(){

        try {
            this.mainSocket.close();
            inConnection=false;
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }




    public Client(Socket socket, InterfaceController interfaceController) {
        this.mainSocket = socket;
        this.interfaceController = interfaceController;


        try {


            dout = new ObjectOutputStream(socket.getOutputStream());

            din = new ObjectInputStream(socket.getInputStream());

            inConnection=true;

        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public static void main(String[] args)  {
//        Socket socket = new Socket();
//        try {
//
//            //подключаемся к серверу
//            socket.connect(new InetSocketAddress(serverIP,port),2000);
//
//            Client client = new Client(socket, new TextArea());
//
//
//
//            new Thread(client).start();
//
//
//            Scanner in = new Scanner(System.in);
//
//            //считываем команды из терминала
//            while(client.inConnection){
//
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.print("Input command: ");
//                String data = in.nextLine();
//
//                String command=Tools.firstSplit(data);
//
//                String filePath=Tools.secondSplit(data);
//
//
//                //отправляем команду
//                client.sendCommandObject(command,filePath,"");
//            }
//
//
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }


    @Override
    public void run() {

        while(inConnection){

            try {
                HashMap<String, Object> receivedMessage;
                String code="";
                String path="";
                String text="";
                try {


                    //считываем полученную команду
                     receivedMessage=(HashMap<String, Object>)din.readObject();
                     code=receivedMessage.get("code").toString();
                     path=receivedMessage.get("path").toString();
                     text=receivedMessage.get("text").toString();


                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    disconnect();
                }


                //выводим полученную команду
               // System.out.println("\ncode:"+code+" path:"+path+" text:"+text);


                interfaceController.logInLoger("\n---------------\n\tcode:"+code+"\n\ttext:\n"+text+"\n---------------");

                //получаем лист
                if(code.contains("149"))
                {

                    transferSocket=new Socket();
                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferPort()),2000);
                    DataInputStream in = new DataInputStream(transferSocket.getInputStream());
                    // DataOutputStream out=new DataOutputStream(transferSocket.getOutputStream());


                   // File f = new File(path);
                    //    System.out.println("arg:"+path);
                    secondArgument="";


                    byte[] buffer = new byte[1024];
                    int count;

                   // logArea.setText(logArea.getText()+"\n---------------\n\tfiles:\n");


                    List<FileName> filesList = new ArrayList<FileName>();

                    int i=0;

                    StringBuilder stringBuilder = new StringBuilder("");

                    while((count = in.read(buffer)) >= 0){

                        byte[] buffer1 = buffer;

                        //filesList.add(new FileName(String.valueOf(Tools.splitLenth(new String(buffer1,0,count, StandardCharsets.UTF_8)))));

                        stringBuilder.append(new String(buffer1,0,count, StandardCharsets.UTF_8));

                        //System.out.println(++i+"count"+count);

                       // fout.write(buffer,0,count);
                    }




                    String[] strings = Tools.firstSplitEnter(stringBuilder.toString());




                    for (int j=0;j<strings.length;j++){




                        filesList.add(new FileName(Tools.fifthSplit(strings[j])));
                    }



                    interfaceController.setFilesList(filesList);


                    transferSocket.close();

                }
                //получаем файл
                if(code.contains("150"))
                {

                    transferSocket=new Socket();
                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferPort()),2000);
                    DataInputStream in = new DataInputStream(transferSocket.getInputStream());
                   // DataOutputStream out=new DataOutputStream(transferSocket.getOutputStream());

                    if(true){

                    }

                    System.out.println("150:"+150);
                    System.out.println("path"+path);
                    File f = new File(path);
                //    System.out.println("arg:"+path);

                    FileOutputStream fout=new FileOutputStream(f);

                    byte[] buffer = new byte[1024];
                    int count;

                    while((count = in.read(buffer)) >= 0){
                        fout.write(buffer,0,count);
                    }
                    fout.close();
                    transferSocket.close();

                }
                //отправляем файл
                if(code.contains("151")){
                    transferSocket=new Socket();

                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferPort()),2000);
                   // DataInputStream in = new DataInputStream(transferSocket.getInputStream());
                    DataOutputStream out=new DataOutputStream(transferSocket.getOutputStream());

                    File f = new File(path);


                    FileInputStream fin=new FileInputStream(f);

                    byte[] buffer = new byte[1024];
                    int count;
                    while((count=fin.read(buffer)) > 0){
                        //(data,start,len)
                        out.write(buffer, 0, count);
                    }
                    fin.close();
                    transferSocket.close();

                }


            } catch (IOException e) {
                e.printStackTrace();

                //interfaceController.logInLoger();

                interfaceController.logInLoger("\n-----\n"+"transfer error:\n\tconnect timed out\n inaccessible transfer port"+"\n-----\n");




               // System.exit(1);
                //break;

            }



        }





    }

    public void disconnect(){




            interfaceController.ConnectButton.setText("Connect");
            interfaceController.connected= closeConnection();

        interfaceController.workPane.setDisable(true);
    }





    public void sendCommandObject(String command,String filePath,String text) throws IOException {


        //получаем команду
       // String command=command;

       // String filePath=firstSplit(commandData);


        Path testFilePath= Paths.get(filePath);
      //  Path testFilePath= Paths.get("C:\\Users\\Username\\Desktop\\testFile.txt");

        Path fileName = testFilePath.getFileName();


        //Paths.get("C:\\Users\\Username\\Desktop\\testFile.txt");


            // если используем команду list, то и сохраняем в list
            if(command.equals("LIST")){
                fileName=Paths.get("list.txt");
                testFilePath=fileName;
                filePath+="\\"+"list.txt";
            }




      //  System.out.println(fileName);



      //  System.out.println("senfp:"+filePath);



        //Создаем тело сообщения
        HashMap<String, Object> newMessage = new HashMap<>();
        //Устанавливаем команду
        newMessage.put("command",command);
        //Устанавливаем специальный текст
        newMessage.put("text",text);
        //Устанавливаем путь
        newMessage.put("path",filePath);
        //Устанавливаем название
        newMessage.put("name",(fileName.toString()));
        //Отправляем сообщение серверу
        try { dout.writeObject(newMessage);
        } catch (IOException e) { e.printStackTrace();
        }




    }


}
