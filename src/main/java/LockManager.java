import java.util.*;

public class LockManager {
    // Table of: data_id, <LISTOFLOCKS/WAITLIST, array of Locks {transaction, MODE}>
    private HashMap<Integer, HashMap<LockTableCols, ArrayList<Lock>>> lockTable;
    // Wait-For-Graph. <tid, list of tids it is waiting for>
    private HashMap<Integer, HashSet<Integer>> WFG;
    int DATABASE_SIZE = 10;
    Transaction[] ts;
    OperationRunner or;
    Log log;
    Integer[] db;

    public LockManager(Transaction[] ts, OperationRunner or, Log log, Integer[] db){
        lockTable = new HashMap<>();
        for(int i = 0; i < DATABASE_SIZE; i++){
            HashMap<LockTableCols, ArrayList<Lock>> hm = new HashMap<>();
            hm.put(LockTableCols.LISTOFLOCKS, new ArrayList<>());
            hm.put(LockTableCols.WAITLIST, new ArrayList<>());
            lockTable.put(i, hm);
        }
        WFG = new HashMap<>();
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            WFG.put(i, new HashSet<>());
        }
        this.ts = ts;
        this.or = or;
        this.db = db;
        this.log = log;
    }

    // Returns true if no deadlock, returns false if deadlock detected
    // Adds Lock to the LISTOFLOCKS if possible. If not, it goes to WAITLIST
    public boolean addLock(Integer variable, Integer tid, Mode m){
        System.out.println("Add lock: " + variable + " tid: " + tid + " mode: " + m);
        // LIST OF LOCKS for this variable is empty -> add the Lock
        if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).isEmpty()){
            lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).add(new Lock(tid, m));
            ts[tid].numberOfLocks++;
            or.run(ts[tid].getActions().get(ts[tid].nextOperationToSchedule), db, log);
            ts[tid].nextOperationToSchedule++;
            for(Lock l : lockTable.get(variable).get(LockTableCols.WAITLIST)){
                if(l.tid != tid){
                    WFG.get(l.tid).add(tid);
                }
            }
        }
        // the list of locks isnt empty, but the lock can be added (R to R)
        else if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).get(0).mode == Mode.READ && m == Mode.READ ){
            // its not locked with R yet with this tid
            boolean modified = true;
            for(Lock locked : lockTable.get(variable).get(LockTableCols.LISTOFLOCKS)){
                if(locked.tid == tid) {
                    modified = false;
                    or.run(ts[tid].getActions().get(ts[tid].nextOperationToSchedule), db, log);
                    ts[tid].nextOperationToSchedule++;
                }
            }
            if(modified){
                lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).add(new Lock(tid, m));
                ts[tid].numberOfLocks++;

                for (Lock l : lockTable.get(variable).get(LockTableCols.WAITLIST)) {
                    if (l.tid != tid) {
                        WFG.get(l.tid).add(tid);
                    }
                }
            }
        }
        // Upgrade Read lock to Write lock for the same transaction if no txs are waiting
        else if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).size() == 1 &&
                lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).get(0).mode == Mode.READ &&
                m == Mode.WRITE &&
                lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).get(0).tid == tid){
            lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).remove(0);
            lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).add(new Lock(tid, m));
            or.run(ts[tid].getActions().get(ts[tid].nextOperationToSchedule), db, log);
            ts[tid].nextOperationToSchedule++;
        }
        else if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).get(0).tid == tid &&
                lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).get(0).mode == Mode.WRITE){
            // TID already owns the W lock
            or.run(ts[tid].getActions().get(ts[tid].nextOperationToSchedule), db, log);
            ts[tid].nextOperationToSchedule++;
        }
        // The list of locks isnt empty, there are write locks, we must put this lock to wait list
        else{

            lockTable.get(variable).get(LockTableCols.WAITLIST).add(new Lock(tid, m));
            // We must add WFG relation
                for(Lock l : lockTable.get(variable).get(LockTableCols.LISTOFLOCKS)){
                        WFG.get(tid).add(l.tid);
                }
            System.out.println("New waitlist" + lockTable.get(variable).get(LockTableCols.WAITLIST).toString());
        }

        // Always check if there is deadlock
        return isDeadlock();
    }

    // Deletes Lock from the lockTable
    public void unlock(Integer variable, Integer tid, Mode m){

    }

    // unlocks all remaining locks in table with this tid
    public void unlock(Integer tid){
        // remove tid from lock table
        for(int i = 0; i < 10; i++){
            for(Map.Entry<LockTableCols, ArrayList<Lock>> entry : lockTable.get(i).entrySet()){
                Lock toRemove = null;
                for(Lock l : entry.getValue()){
                    if(l.tid == tid){
                        toRemove = l;
                    }
                }
                entry.getValue().remove(toRemove);
            }
        }
        // remove TID from wait-for-graph
        WFG.get(tid).clear();
        for(Map.Entry<Integer, HashSet<Integer>> waitingFor : WFG.entrySet()){
            waitingFor.getValue().remove(tid);
        }
        ts[tid].numberOfLocks = 0;
    }

    private boolean isDeadlock(){
        writeLockTable();
        writeWFG();
        //System.out.println(WFG.entrySet().isEmpty());
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            if(isDeadlock(i, new ArrayList<>())){
                System.out.println("Is deadlock");
                return true;
            }
        }
        System.out.println("Is NOT deadlock");
        return false;
    }

    private void writeLockTable() {
        System.out.println("LOCK TABLE");
        for(int i = 0; i < 10; i++){

            System.out.print(i + ": ");
            for(Lock e : lockTable.get(i).get(LockTableCols.LISTOFLOCKS)){
                System.out.print(e.tid +"" + e.mode + ", ");
            }
            System.out.print("\t | ");
            for(Lock e : lockTable.get(i).get(LockTableCols.WAITLIST)){
                System.out.print(e.tid +"" + e.mode + ", ");
            }
            System.out.println();
        }
    }

    private void writeWFG() {
        for(Map.Entry<Integer, HashSet<Integer>> entry : WFG.entrySet()){
            System.out.print(entry.getKey() + ": ");
                    for(Integer i : entry.getValue()){
                        System.out.print(i + ", ");
                    }
            System.out.println();
        }
    }

    private boolean isDeadlock(Integer tid, ArrayList<Integer> comingFrom){
        if(WFG.get(tid).isEmpty()){
            return false;
        }
        for(Integer to : WFG.get(tid)){
            comingFrom.add(tid);
            if(comingFrom.contains(to)){
                return true;
            }
            if(isDeadlock(to, comingFrom)){
                return true;
            }
            comingFrom.remove(tid);
        }
        return false;
    }

    // try to add locks. If none action returns false, then it is S2PL
    public ArrayList<Integer> getDeadlockCycle(){
        ArrayList<Integer> cycle = new ArrayList<>();
        for(int i = 0; i < Resources.getNumberOfTransactions(); i++){
            cycle = getCycleRecursion(i, new ArrayList<>());
            if(cycle != null && !cycle.isEmpty()){
                return cycle;
            }
        }

        return null;
    }

    private ArrayList<Integer> getCycleRecursion(Integer tid, ArrayList<Integer> comingFrom){
        if(WFG.get(tid).isEmpty()){
            return null;
        }
        for(Integer to : WFG.get(tid)){
            comingFrom.add(tid);
            if(comingFrom.contains(to)){
                return comingFrom;
            }
            if(isDeadlock(to, comingFrom)){
                return comingFrom;
            }
            comingFrom.remove(tid);
        }
        return null;
    }

    public ArrayList<Integer> getWaitingTransactions(){
        ArrayList<Integer> list = new ArrayList<>();

        for(Map.Entry<Integer, HashMap<LockTableCols, ArrayList<Lock>>> variableEntry : lockTable.entrySet()){
            for(Lock l : variableEntry.getValue().get(LockTableCols.WAITLIST)){
                if(!list.contains(l.tid)){
                    list.add(l.tid);
                }
            }
        }

        return list;
    }

    public Integer removeNextWaitingTID() {
        Integer candidate = null;
        for(int i = 0; i < 10; i++){
            if(lockTable.get(i).get(LockTableCols.LISTOFLOCKS).isEmpty() && !lockTable.get(i).get(LockTableCols.WAITLIST).isEmpty()){
                Lock toMove = lockTable.get(i).get(LockTableCols.WAITLIST).get(0);
                lockTable.get(i).get(LockTableCols.WAITLIST).remove(0);
                return toMove.tid;
            }
        }
        return candidate;
    }


}
