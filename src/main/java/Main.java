import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Main {
    static int NUMBER_OF_THREADS = 2;
    static int NUMBER_OF_OPERATIONS = 10;

    public static void main(String[] args){
        Tester tester = new Tester();

        // We want to start just 2 threads at the same time, but let's control that
        // timing from the main thread. That's why we have 3 "parties" instead of 2.
        /*
        int numberOfThreads;
        if(args.length > 0){
            numberOfThreads = Integer.parseInt(args[0]);

        } else {
            numberOfThreads = 2;
        }
        Integer[] db = new Integer[10];
        resetdb(db);
        final CyclicBarrier gate = new CyclicBarrier(numberOfThreads);

        DataManager dm = new DataManager(2, db, NUMBER_OF_OPERATIONS);
        ArrayList<MyThread> threads = new ArrayList<MyThread>();
        for(int i = 0; i < numberOfThreads; i++){
            MyThread t = new MyThread(i + 1, gate, dm, NUMBER_OF_OPERATIONS);
            threads.add(t);
            t.start();
        }

// At this point, t1 and t2 are blocking on the gate.
// Since we gave "3" as the argument, gate is not opened yet.
// Now if we block on the gate from the main thread, it will open
// and all threads will start to do stuff!

        System.out.println("all threads started");

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();
        dm.writeSchedulesToFile();
        dm.writeGlobalScheduleToFile("0");
        writeDatabase(db, false);

        if(isThereTheSameSequenceSchedule(dm.schedules, db)){
            System.out.println("There is the same result in the database with permutation " + theSameSequenceSchedule(dm.schedules, db) );
        } else {
            System.out.println("There are no equivalent sequential schedules. ");
        }
        hasCycle(incidenceMatrixOfSchedule(dm.globalSchedule));
*/
    }

    private static void resetdb(Integer[] db){
        for(int i = 0; i < 10; i++){
            db[i] = 10;
        }
    }

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

    private static void writeDatabase(Integer[] db, boolean append){
        for(int i = 0; i < 10; i++){
            System.out.print(db[i] + " ");
        }
        System.out.println();
        try {
            FileWriter myWriter = new FileWriter("database.txt", append);
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
    }

    private static void sequenceSchedules(){
        StringBuilder permutation = new StringBuilder();
        Integer[] db = new Integer[10];

        // TODO make thread permutations and do the operations in given thread order


        writeDatabase(db, true);
    }

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


}
