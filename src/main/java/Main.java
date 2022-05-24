import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Main {
    static Integer[] db;

    public static void main(String[] args){
        //Tester tester = new Tester();
        // 0) Initialize db
        db = new Integer[10];
        resetdb();
        Transaction[] transactions = generateTransactions();
        Log log = new Log();
        // 1) Generate X transactions with Y maximum number of actions
        Generator gen = new Generator();
        Map<Integer, ArrayList<Operation>> schedules = gen.generateTransactionSchedules();
        PrettyWriter.prettyWriteSchedules(schedules);
        setSchedulesToTransactions(transactions, schedules);

        // 2) Schedule and run the generated actions
        DataManager dm = new DataManager(db, log, schedules, transactions);
        dm.makeGlobalSchedule();

        // 3) RANDOM STOP (restart system when there are still some active transactions) and recover, finish all operations
        // TODO
        // is part of the makeGlobalSchedule method
        // 4) Save the DB state
        String db1 = writeDatabase(db, false, "FullRun");
        // 5) Run COMMIT PROJECTION
        // TODO
        //runCommitProjection(db);
        // 6) Save the DB state
        String db2 = writeDatabase(db, false, "CommitProjection");
        // 7) Compare the DBs. Accept if they are the same.
        System.out.println("Databases have the same output: " + Resources.sameContent(new File(db1).toPath(),new File(db2).toPath()));

    }

    private static void setSchedulesToTransactions(Transaction[] ts, Map<Integer, ArrayList<Operation>> schedules) {
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            ts[i].setActions(schedules.get(i));
        }
    }

    private static void resetdb(){
        for(int i = 0; i < 10; i++){
            db[i] = 10;
        }
    }
/*
    private static void writeSchedules(ArrayList<MyThread> threads, String name){
        try {
            FileWriter myWriter = new FileWriter("schedules"  + name + ".txt");
            for(int i = 0; i < 10; i++){
                for(MyThread t : threads){
                    myWriter.write(t.getOperations().get(i) + "\t");
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
*/
    private static String writeDatabase(Integer[] db, boolean append, String name){
        for(int i = 0; i < 10; i++){
            System.out.print(db[i] + " ");
        }
        System.out.println();
        String nameOfFile = "database" + name + ".txt";
        try {
            FileWriter myWriter = new FileWriter(nameOfFile, append);
            for(int i = 0; i < 10; i++){
                    myWriter.write(db[i] + " ");

            }
            myWriter.write("\n");

            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return nameOfFile;
    }

    private static void sequenceSchedules(){
        StringBuilder permutation = new StringBuilder();
        Integer[] db = new Integer[10];

        // TODO make thread permutations and do the operations in given thread order


        writeDatabase(db, true, "Sequence");
    }

    private static Transaction[] generateTransactions(){
        Transaction[] ts = new Transaction[Resources.getNumberOfTransactions()];
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            ts[i] = new Transaction(i, 0);
        }
        return ts;
    }

    /*
    public static boolean isThereTheSameSequenceSchedule(Map<Integer, ArrayList<Operation>> ops, Integer[] db){
        Integer[] newDB = new Integer[10];
        resetdb(newDB);
        DataManager dm = new DataManager(NUMBER_OF_THREADS, newDB, NUMBER_OF_OPERATIONS);
        for(int perm =1; perm < NUMBER_OF_THREADS + 1; perm++){
            int otherTID = perm == 1 ? 2 : 1;
            System.out.println("Checking permutation" + perm + "" + otherTID);
            System.out.println();
            for(int i =1; i < NUMBER_OF_THREADS + 1; i++){
                for(int j = 0; j < NUMBER_OF_OPERATIONS; j++) {
                    dm.doSth(ops.get(i == 1 ? perm : otherTID).get(j), false);
                    for(int k = 0; k < 10; k++){
                        System.out.print(newDB[k] + " ");
                    }
                    System.out.println();
                }
            }

            //writeDatabase(newDB, true);

            if(Arrays.equals(db, newDB) ){
                return true;
            }
            resetdb(newDB);
        }



        return false;
    }

    private static  String theSameSequenceSchedule(Map<Integer, ArrayList<Operation>> ops, Integer[] db){
        boolean thereIs = false;
        String samePermutation = "";
        Integer[] newDB = new Integer[10];
        resetdb(newDB);
        DataManager dm = new DataManager(NUMBER_OF_THREADS, newDB, NUMBER_OF_OPERATIONS);
        for(int perm =1; perm < NUMBER_OF_THREADS + 1; perm++){
            int otherTID = perm == 1 ? 2 : 1;
            for(int i =1; i < NUMBER_OF_THREADS + 1; i++){
                for(int j = 0; j < NUMBER_OF_OPERATIONS; j++) {
                    dm.doSth(ops.get(i == 1 ? perm : otherTID).get(j), false);
                }
            }

            writeDatabase(newDB, true);
            for(int dbIndex = 0; dbIndex < 10; dbIndex++){
                if(Arrays.equals(db, newDB) ){
                    thereIs = true;
                    samePermutation = String.valueOf(perm) + String.valueOf(otherTID);
                }
            }
            resetdb(newDB);
        }
        if(thereIs){
            return samePermutation;
        }
        return null;
    }



    private static Conflict[][] incidenceMatrixOfSchedule(ArrayList<Operation> schedule){
        Conflict[][]  matrix= new Conflict[NUMBER_OF_THREADS][NUMBER_OF_THREADS];
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[0].length; j++){
                matrix[i][j] = new Conflict(false,false,false,false);
            }
        }
        ArrayList<Operation> opsPreceding = new ArrayList<>();
        // for all possible indexes (0..9) search the global schedule and look for conflicting operations
        // conflicting operations are RW, WR, WW on the same index
        for(int i = 0; i < 10; i++){
            for(Operation op: schedule){
                if(op.index == i){
                    if(op.rw == OPS.WRITE){
                        for(int k=0; k<opsPreceding.size(); k++){
                            if((opsPreceding.get(k).rw.equals(OPS.WRITE) ||
                                    opsPreceding.get(k).rw.equals(OPS.READ)) &&
                                    !opsPreceding.get(k).tid.equals(op.tid)){
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].WW = true;
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].incident = true;
                                System.out.println(opsPreceding.get(k).toString() + " -> " + op.toString());
                            }
                            if((opsPreceding.get(k).rw.equals(OPS.READ)) &&
                                    !opsPreceding.get(k).tid.equals(op.tid)){
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].RW = true;
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].incident = true;
                                System.out.println(opsPreceding.get(k).toString() + " -> " + op.toString());
                            }
                        }

                    } else if(op.rw == OPS.READ){
                        for(int k=0; k<opsPreceding.size(); k++){
                            if(opsPreceding.get(k).rw.equals(OPS.WRITE)  &&
                                    !opsPreceding.get(k).tid.equals(op.tid)){
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].WR = true;
                                matrix[opsPreceding.get(k).tid-1][op.tid-1].incident = true;
                                System.out.println(opsPreceding.get(k).toString() + " -> " + op.toString());
                            }
                        }
                    }
                    // check if conflicting
                    opsPreceding.add(op);
                }
            }
            // clear all preceding to compute another index
            opsPreceding.clear();
        }
        printMatrix(matrix);
        return matrix;
    }

    private static void printMatrix(Conflict[][] matrix){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[0].length; j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static boolean hasCycle(Conflict[][] matrix){
        for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[0].length; j++){
                if(matrix[i][j].incident) {
                    ArrayList<Integer> visitedThreads = new ArrayList<>();
                    if (followTheTrail(matrix, i, j, visitedThreads) == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean followTheTrail(Conflict[][] matrix, int i, int j, ArrayList<Integer> visitedThrads){
        visitedThrads.add(i);
        for(int k = 0; k < NUMBER_OF_THREADS; k++){
            if(matrix[j][k].incident && visitedThrads.contains(k)){
                System.out.println("Cycle closed from T" + (j+1) + " to T" + (k+1));
                System.out.println("Schedule is not conflict serializable");
                return true;
            } else if (matrix[j][k].incident && !visitedThrads.contains(k)){
                return followTheTrail(matrix, j, k, visitedThrads);
            }
        }
        return false;
    }
*/

}
