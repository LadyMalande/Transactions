import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class DataManager{
    ArrayList<Operation> globalSchedule;
    Map<Integer, ArrayList<Operation>> schedules;
    ArrayList<Integer> commitSequence = new ArrayList<>();
    int NUMBER_OF_TRANSACTIONS;
    int NUMBER_OF_OPERATIONS;
    Integer[] db;
    // indexOfNextAction[i] = j ... the next action to be scheduled of transaction i is action on position j
    Integer[] indexOfNextAction;
    Transaction[] ts;
    LockManager lm;
    Log log;
    OperationRunner or;

    public DataManager(Integer[] database, Log log,  Map<Integer, ArrayList<Operation>> schedules, Transaction[] ts ) {
        NUMBER_OF_TRANSACTIONS = Resources.getNumberOfTransactions();
        db = database;
        this.schedules = schedules;
        globalSchedule = new ArrayList<>();
        NUMBER_OF_OPERATIONS = Resources.getNumberOfOperations();
        or = new OperationRunner(lm, log, ts);
        lm = new LockManager(ts, or, log, db);
        or.setLM(lm);
        this.log = log;
        this.ts = ts;

    }

    public void makeGlobalSchedule(){

        // any transaction is not yet finished and needs to have its operation scheduled
        while( Arrays.stream(ts).anyMatch(transaction -> transaction.nextOperationToSchedule < transaction.getNumberOfOperations())){
            // there is newly unlocked lock for waiting transactions
            Integer tidToReplaceInLock = lm.removeNextWaitingTID();
            Resources.setFinalAbort(false);
            int whichTransaction = -1;

                if(tidToReplaceInLock != null){
                    System.out.println("tid to replace in Lock " + tidToReplaceInLock);
                    whichTransaction = tidToReplaceInLock;
                } else {
                    whichTransaction = ThreadLocalRandom.current().nextInt(Resources.getNumberOfTransactions());
                }

                ArrayList<Integer> tidsOfWaitingTransactions = lm.getWaitingTransactions();
                if (!tidsOfWaitingTransactions.contains(whichTransaction) && !finishedTransactions().contains(whichTransaction)) {
                    if (schedules.get(whichTransaction).get(ts[whichTransaction].nextOperationToSchedule).rw == OPS.WRITE) {
                        if (lm.addLock(schedules.get(whichTransaction).get(ts[whichTransaction].nextOperationToSchedule).index, whichTransaction, Mode.WRITE)) {
                            System.out.println("Deadlock? " + true);
                            or.run(new Operation(OPS.ABORT, abortWithLeastLocks(), -1, -1, ""), db, log);
                        }
                    } else if (schedules.get(whichTransaction).get(ts[whichTransaction].nextOperationToSchedule).rw == OPS.READ) {
                        if (lm.addLock(schedules.get(whichTransaction).get(ts[whichTransaction].nextOperationToSchedule).index, whichTransaction, Mode.READ)) {
                            System.out.println("Deadlock? " + true);
                            or.run(new Operation(OPS.ABORT, abortWithLeastLocks(), -1, -1, ""), db, log);
                        }
                    } else {
                        if(ts[whichTransaction].getActions().get(ts[whichTransaction].nextOperationToSchedule).rw == OPS.ABORT){
                            Resources.setFinalAbort(true);
                        }
                        or.run(ts[whichTransaction].getActions().get(ts[whichTransaction].nextOperationToSchedule), db, log);
                        ts[whichTransaction].nextOperationToSchedule++;
                        System.out.println("Doing operation )" + ts[whichTransaction].getActions().get(ts[whichTransaction].nextOperationToSchedule-1));

                    }
                    // Transaction is not blocked so it can get its operation scheduled
                }





        }
    }

    private ArrayList<Integer> finishedTransactions() {
        ArrayList<Integer> finished = new ArrayList<>();
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            if(ts[i].nextOperationToSchedule == ts[i].getNumberOfOperations()){
                finished.add(i);
            }
        }
        return finished;
    }

    private int abortWithLeastLocks() {
        ArrayList<Integer> tidsInCycle = lm.getDeadlockCycle();
        ArrayList<Integer> smallestTxs = new ArrayList<>();
        int leastLocks = 20;
        int toAbort;
        System.out.println(tidsInCycle.toString());
        for(Integer tid : tidsInCycle){
            System.out.println("TID: " + tid + " numberOfLocks: " + ts[tid].numberOfLocks);
            if(ts[tid].numberOfLocks == leastLocks){
                smallestTxs.add(tid);
            }
            if(ts[tid].numberOfLocks < leastLocks){
                smallestTxs.clear();
                smallestTxs.add(tid);
                leastLocks = ts[tid].numberOfLocks;
            }
        }
        toAbort = smallestTxs.get(0);
        // find the newset one and abort it
        Map<Integer, Integer> tidsAndStarts = new HashMap<>();
        for(Integer tid : smallestTxs){
            tidsAndStarts.put(tid, log.getStartingIndex(tid));
        }
        int max = Integer.MIN_VALUE;

        for(Map.Entry<Integer, Integer> entry : tidsAndStarts.entrySet()){
            if(entry.getValue() > max){
                max = entry.getValue();
                toAbort = entry.getKey();
            }
        }

        return toAbort;
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
