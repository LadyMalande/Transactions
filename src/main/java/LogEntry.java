public class LogEntry {

    OPS operation;
    int oid;
    int tid;
    Integer[] bi;
    Integer[] ai;
    LogEntry backpointer;

    /**
     * Log Entry is one row of log consisting of needed information for each action in the current run of transactions.
     * @param operation distincts between checkpoint, UNDO or other operations
     * @param oid id of the object being manipulated with
     * @param tid id of the transaction doing said operation
     * @param bi before image - state of the database before this action has been taken
     * @param ai after image - state of database after this action has been taken
     * @param backpointer points to the action of the transaction immediatelz before this one. Null if this is
     *                    the first action of the transaction
     */
    public LogEntry (OPS operation, int oid, int tid, Integer[] bi, Integer[] ai, LogEntry backpointer){
        this.ai = ai;
        this.backpointer = backpointer;
        this.bi = bi;
        this.tid = tid;
        this.oid = oid;
        this.operation = operation;
    }

    public OPS getOperation() {
        return operation;
    }

    public int getOid() {
        return oid;
    }

    public int getTid() {
        return tid;
    }

    public Integer[] getBi() {
        return bi;
    }

    public Integer[] getAi() {
        return ai;
    }

    public LogEntry getBackpointer() {
        return backpointer;
    }

    public boolean isFirst(){
        return (this.backpointer == null);
    }

    public boolean isFinalAction() {
        return operation == OPS.ABORT || operation == OPS.COMMIT;
    }
}
