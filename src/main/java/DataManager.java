import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataManager{
    ArrayList<Operation> globalSchedule;
    Map<Integer, ArrayList<Operation>> schedules;
    ArrayList<Integer> commitSequence = new ArrayList<>();
    int NUMBER_OF_TRANSACTIONS;
    int NUMBER_OF_OPERATIONS;
    Integer[] db;

    public DataManager(int not, Integer[] database, int numOfOps, Map<Integer, ArrayList<Operation>> schedules ) {
        NUMBER_OF_TRANSACTIONS = not;
        db = database;
        this.schedules = schedules;
        globalSchedule = new ArrayList<>();
        NUMBER_OF_OPERATIONS = numOfOps;
    }



    public int doSth(Operation op, boolean randomSchedule){
        switch(op.rw){
            case READ: read(op);
            break;
            case ABORT: abort(op);
            break;
            case WRITE: write(op);
            break;
            case COMMIT: commit(op);
            break;
            default: System.out.println("ERROR bad operation");
        }
        if(randomSchedule){
            writeSchedule(op);
            writeGlobalSchedule(op);
        }
        return db[op.index];
    }

    private void writeGlobalSchedule(Operation op) {
        globalSchedule.add(op);
    }

    private void writeSchedule(Operation op) {
        ArrayList<Operation> list = schedules.get(op.tid);
        list.add(op);
    }

    private void read(Operation op) {
        //TODO
        // doesn't need anything else
        op.newValue = db[op.index];
    }

    private void abort(Operation op) {
        //TODO
    }

    private void write(Operation op) {

        db[op.index] = op.newValue;
    }

    private void commit(Operation op) {
        commitSequence.add(op.tid);
    }

    private void rollback(Operation op) {
        //TODO
    }


    public void writeSchedulesToFile() {
        try {
            FileWriter myWriter = new FileWriter("schedules.txt");
            for(int i = 0; i < NUMBER_OF_OPERATIONS; i++){
                myWriter.write(schedules.get(1).get(i) + "\t" + schedules.get(2).get(i));
                myWriter.write("\n");
            }


            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    public void writeGlobalScheduleToFile(String s) {
        try {
            FileWriter myWriter = new FileWriter("globalSchedule" + s + ".txt");
            for(int i = 0; i < NUMBER_OF_OPERATIONS * 2; i++){
                if(globalSchedule.get(i).tid == 2){
                        myWriter.write("\t\t\t\t"  + globalSchedule.get(i));
                } else{
                    myWriter.write(globalSchedule.get(i).toString());
                }
                myWriter.write("\n");

            }
            myWriter.write("\n");

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
