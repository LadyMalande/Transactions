import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class PrettyWriter {

    final static String FILE_PATH = "prettySchedules.txt";

    public static void prettyWriteSchedulesToFile(Map<Integer, ArrayList<Operation>> schedules){
        try {
            FileWriter myWriter = new FileWriter(FILE_PATH);
            for(int i = 0; i < 11; i++){
                for(Map.Entry<Integer, ArrayList<Operation>> entry : schedules.entrySet()){
                    try {
                        myWriter.write(entry.getValue().get(i).toString() + "\t");
                    } catch (IndexOutOfBoundsException e) {
                        myWriter.write("            " + "\t");
                    }

                }
                myWriter.write("\n");
            }


            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public static void prettyWriteSchedules(Map<Integer, ArrayList<Operation>> schedules){
        for(int i = 0; i < 11; i++){
            for(Map.Entry<Integer, ArrayList<Operation>> entry : schedules.entrySet()){
                try {
                    System.out.print(entry.getValue().get(i).toString() + "\t");
                } catch (IndexOutOfBoundsException e) {
                    System.out.print("            " + "\t");
                }
            }
            System.out.println();
        }
    }
}
