import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tools {



   static final DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");


    static String[] splitByLine(String command){
        String[] arg = command.split("\n");
        return arg;
    }
    static String[]  splitArray(String command){
        String[] arg = command.split(" ");
        return arg;
    }
    static int splitLenth(String command){

        return command.length();
    }
    static String  firstSplit(String command){
        String[] arg = command.split(" ");
        return arg[0];
    }
    static String  secondSplit(String command){
        String[] arg = command.split(" ");
        if(arg.length>1)
            return arg[1];
        else
            return "";
    }
    static String  fifthSplit(String command){
        String[] arg = command.split(" ");
        if(arg.length>3)
            return arg[3];
        else
            return "";
    }

    static String getDateTime(){


        return LocalDateTime.now().format(myFormatObj);
    }

}
