public class Tools {





    static String[]  firstSplitEnter(String command){
        String[] arg = command.split("\n");
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
}
