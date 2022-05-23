import java.util.*;

public class LockManager {
    // Table of: data_id, <LISTOFLOCKS/WAITLIST, array of Locks {transaction, MODE}>
    private HashMap<Integer, HashMap<LockTableCols, ArrayList<Lock>>> lockTable;
    // Wait-For-Graph. <tid, list of tids it is waiting for>
    private HashMap<Integer, HashSet<Integer>> WFG;
    int DATABASE_SIZE = 10;

    public LockManager(){
        lockTable = new HashMap<>();
        for(int i = 0; i < DATABASE_SIZE; i++){
            HashMap<LockTableCols, ArrayList<Lock>> hm = new HashMap<>();
            hm.put(LockTableCols.LISTOFLOCKS, new ArrayList<>());
            hm.put(LockTableCols.WAITLIST, new ArrayList<>());
            lockTable.put(i, hm);
        }
    }

    // Returns true if no deadlock, returns false if deadlock detected
    // Adds Lock to the LISTOFLOCKS if possible. If not, it goes to WAITLIST
    public boolean addLock(Integer variable, Integer tid, Mode m){
        // LIST OF LOCKS for this variable is empty -> add the Lock
        if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).isEmpty()){
            lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).add(new Lock(tid, m));
        }
        // the list of locks isnt empty, but the lock can be added (R to R)
        else if(lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).stream().anyMatch(o -> o.mode == Mode.READ)){
            lockTable.get(variable).get(LockTableCols.LISTOFLOCKS).add(new Lock(tid, m));
        } // The list of locks isnt empty, there are write locks, we must put this lock to wait list
        else{
            lockTable.get(variable).get(LockTableCols.WAITLIST).add(new Lock(tid, m));
            // We must add WFG relation
            if(WFG.get(tid) != null){
                for(Lock l : lockTable.get(variable).get(LockTableCols.LISTOFLOCKS)){
                    WFG.get(tid).add(l.tid);
                }
            } else{
                HashSet<Integer> newlist = new HashSet<>();
                for(Lock l : lockTable.get(variable).get(LockTableCols.LISTOFLOCKS)){
                    newlist.add(l.tid);
                }
                WFG.put(tid, newlist);
            }
        }

        // Always check if there is deadlock
        return !isDeadlock();
    }

    // Deletes Lock from the lockTable
    public void unlock(Integer variable, Integer tid, Mode m){

    }

    // unlocks all remaining locks in table with this tid
    public void unlock(Integer tid){

    }

    private boolean isDeadlock(){
        if(isDeadlock(1, null)){
            return true;
        }
        return false;
    }

    private boolean isDeadlock(Integer tid, ArrayList<Integer> comingFrom){
        if(WFG.get(tid) == null){
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
    public boolean isDeadlock(ArrayList<Operation> globalSchedule){
        for(Operation operation : globalSchedule){
            if(operation.rw == OPS.READ || operation.rw == OPS.WRITE ){
                Mode m;
                switch(operation.rw){
                    case READ: m = Mode.READ;
                    break;
                    case WRITE : m = Mode.WRITE;
                    break;
                    default: m = null;
                }
                if(!addLock(operation.index, operation.tid, m)){
                    // deadlock
                    return false;
                }
            }
            if(operation.rw == OPS.COMMIT){
                unlock(operation.tid);
            }
        }

        // not deadlock
        return true;
    }
}
