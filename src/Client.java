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



    int stepCkeck=0;



    boolean requestInProcessing=true;


    String secondArgument="";

    ObjectOutputStream dout;
    ObjectInputStream din;
    private volatile boolean inConnection;


    String filesDir="clientFiles/";


    public boolean closeConnection(){
        stepCkeck=0;
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

        stepCkeck=0;
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




                //получаем лист
                if(code.contains("149"))
                {
                    interfaceController.logInLogger("\n---------------\n\tcode:"+code+"\n\ttext:\n"+text+"\n---------------");

                    transferSocket=new Socket();
                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferAddress(),interfaceController.getTransferPort()),10000);
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




                    String[] strings = Tools.splitByLine(stringBuilder.toString());




                    for (int j=0;j<strings.length;j++){




                        filesList.add(new FileName(Tools.fifthSplit(strings[j])));
                    }



                    interfaceController.setFilesList(filesList);


                    transferSocket.close();

                }
                //получаем файл
                if(code.contains("150"))
                {
                    interfaceController.logInLogger("\n---------------\n\tcode:"+code+"\n\ttext:\n"+text+"\n---------------");


                    transferSocket=new Socket();
                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferAddress(),interfaceController.getTransferPort()),2000);
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

                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferAddress(),interfaceController.getTransferPort()),2000);
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

                if(code.contains("151")){

                    interfaceController.logInLogger("\n---------------\n\tcode:"+code+"\n\ttext:\n"+text+"\n---------------");

                    transferSocket=new Socket();

                    transferSocket.connect(new InetSocketAddress(interfaceController.getTransferAddress(),interfaceController.getTransferPort()),2000);
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

                if(code.contains("211")){

                    stepCkeck=0;

                    if(text.equals("confirm")){

                        interfaceController.confirmSignIn();

                        interfaceController.logInLogger("\n---------------\n\tcode:"+code+"\n\tlogged in successfully\n---------------");


                    }
                    else if(text.equals("incorrect")){

                        interfaceController.unConfirmSignIn();

                        interfaceController.logInLogger("\n---------------\n\tcode:"+code+"\n\tlogin or pass incorrect\n---------------");

                    }

                }


            } catch (IOException e) {
                e.printStackTrace();

              //  interfaceController.signIn();

                interfaceController.logInLogger("\n-----\n"+"transfer error:\n\tconnect timed out\n inaccessible transfer port"+"\n-----\n");




               // System.exit(1);
                //break;

            }



        }





    }

    public void disconnect(){


        interfaceController.logout();

    }




    public void sendAuthDataObject(String login,String password)  {




        //Создаем тело сообщения
        HashMap<String, Object> newMessage = new HashMap<>();
        //Устанавливаем команду
        newMessage.put("command","AUTH");
        //Устанавливаем специальный текст
        newMessage.put("login",login);
        //Устанавливаем путь
        newMessage.put("password",password);
        //Отправляем сообщение серверу
        try { dout.writeObject(newMessage);
        } catch (IOException e) { e.printStackTrace();
        }






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
