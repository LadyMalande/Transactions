import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Log {
    /**
     * Log consisting of ordered list of LogEntries monitoring the state of current run of transactions
     */
    ArrayList<LogEntry> log;

    public Log(ArrayList<LogEntry> log) {
        this.log = log;
    }
    public Log(){
        this.log = new ArrayList<>();
    }

    public LogEntry addEntry(LogEntry le){
        log.add(le);
        //System.out.println("Log: " + le.operation.toString() + ", " + le.oid + ", " + le.tid + ", " + le.bi + ", " + le.ai + ", " + le.backpointer);
        return le;
    }

    public LogEntry addEntry(OPS operation, int oid, int tid, Integer[] bi, Integer[] ai, LogEntry backpointer){
        LogEntry le = new LogEntry(operation, oid, tid, bi, ai, backpointer);
        log.add(le);
        //System.out.println("Log: " + operation.toString() + ", " + oid + ", " + tid + ", " + bi + ", " + ai + ", " + backpointer);
        return le;
    }

    public ArrayList<Integer> getActiveTIDs(){
        List<Integer> activeTIDs = IntStream.rangeClosed(0, Resources.getNumberOfTransactions())
                .boxed().collect(Collectors.toList());
        for(LogEntry entry : log){
            if(entry.isFinalAction()){
                activeTIDs.remove(entry.tid);
            }
        }
        return (ArrayList<Integer>) activeTIDs;
    }


    public LogEntry getBackpointer(Integer tid) {
        LogEntry backpointer = null;
        for(LogEntry entry: log){
            if(entry.tid == tid){
                backpointer = entry;
            }
        }
        return backpointer;
    }

    public Integer getStartingIndex(Integer tid) {
        for(LogEntry le : log){
            if(le.tid == tid && le.backpointer == null){
                return log.indexOf(le);
            }
        }
        return Integer.MAX_VALUE;
    }
}
