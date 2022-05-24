import java.util.ArrayList;

public class Transaction {

    int tid;

    // For commit projection, to repeat sequentially all transactions
    ArrayList<Operation> actions;
    // For case of deadlock - the transaction with least locks is being aborted
    int numberOfLocks;
    boolean finished;
    Integer[] knownValues;

    int nextOperationToSchedule;

    public Transaction(int tid, int numberOfLocks) {
        this.tid = tid;
        this.numberOfLocks = numberOfLocks;
        resetKnownValues();
        nextOperationToSchedule = 0;
        finished = false;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public void resetAfterAbort(){
        resetKnownValues();
        resetNextOperationToSchedule();
    }

    public void resetKnownValues(){
        knownValues = new Integer[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    }

    public void resetNextOperationToSchedule(){
        nextOperationToSchedule = 0;
    }

    public ArrayList<Operation> getActions() {
        return actions;
    }

    public void setActions(ArrayList<Operation> actions) {
        this.actions = actions;
    }

    public int getNumberOfLocks() {
        return numberOfLocks;
    }

    public void setNumberOfLocks(int numberOfLocks) {
        this.numberOfLocks = numberOfLocks;
    }

    public int getNumberOfOperations(){
        return actions.size();
    }
}
