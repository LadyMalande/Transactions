public class OperationRunner {

    LockManager lm;
    Log log;
    Transaction[] ts;

    public OperationRunner(LockManager lm, Log log, Transaction[] ts) {
        this.lm = lm;
        this.log = log;
        this.ts = ts;
    }

    // do an operation and write changes to log + checkpoint
    public void run(Operation op, Integer[] db, Log log){
        Integer[] beforeImage = new Integer[10];
        Integer[] afterImage = new Integer[10];
        System.arraycopy(db,0,beforeImage,0,10);

        // UNDO operation is used only in ABORT, its never done on its own
        switch(op.rw){
            case READ: read(op, db);
            break;
            case WRITE: write(op, db);
            break;
            // When commiting, we release locks
            case COMMIT: commit(op, db);
            break;
            // When aborting, we also release locks
            case ABORT: abort(op, db);
            break;
            default:
        }
        System.arraycopy(db,0,afterImage,0,10);
        LogEntry logOperation = new LogEntry(op.rw, op.index,op.tid, beforeImage, afterImage, log.getBackpointer(op.tid) );
        log.addEntry(logOperation);
        log.addEntry(OPS.CHECKPOINT, -1, -1, beforeImage, afterImage, logOperation);
    }

    private void read(Operation op, Integer[] db){
        System.out.println("READ transaction " + op.tid);
        ts[op.tid].knownValues[op.index] = db[op.index];
    }

    private void write(Operation op, Integer[] db){
        System.out.println("WRITE transaction " + op.tid);
        if(op.newValue < 5){
            // compute product if we knew the value by reading before this write
            ts[op.tid].knownValues[op.index] = ts[op.tid].knownValues[op.index] == -1 ? op.newValue : op.newValue * ts[op.tid].knownValues[op.index];
        } else {
            // compute sum if we knew the value before, otherwise just update with the new value
            ts[op.tid].knownValues[op.index] = ts[op.tid].knownValues[op.index] == -1 ? op.newValue : op.newValue + ts[op.tid].knownValues[op.index];
        }
        db[op.index] = ts[op.tid].knownValues[op.index];
    }

    private void abort(Operation op, Integer[] db){
        LogEntry newBackpointer = log.getBackpointer(op.tid);
        LogEntry lastAction = log.getBackpointer(op.tid);
        System.out.println("Aborting transaction " + op.tid);
        while(lastAction != null){
            System.out.println("lastAction " + lastAction + " newBackpointer: " + newBackpointer);
            LogEntry nextlastAction = lastAction.backpointer;
            //System.out.println("Size of log " + log.log.size());
            newBackpointer = undo(lastAction, newBackpointer);
            lastAction = nextlastAction;
        }
        ts[op.tid].resetAfterAbort();
        // unlock all acquired locks
        lm.unlock(op.tid);
        if(Resources.isFinalAbort()){
            ts[op.tid].finished = true;
        }
    }

    // switch the after image and before image of the operation we are trying to undo
    private LogEntry undo(LogEntry le, LogEntry nbp){
        //System.out.println(le);
        System.out.println("UNDOing transaction " + le.tid + " operation " + le.operation.toString() + " variable: " + le.oid + " bp: " + le.backpointer);
        return log.addEntry(OPS.UNDO, le.oid, le.tid, le.ai, le.bi, nbp);
    }

    private void commit(Operation op, Integer[] db){
        System.out.println("Commiting transaction " + op.tid);
        lm.unlock(op.tid);
        ts[op.tid].finished = true;
    }

    public void setLM(LockManager lm) {
        this.lm = lm;
    }
}
