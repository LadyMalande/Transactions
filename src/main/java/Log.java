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

    public void addEntry(LogEntry le){
        log.add(le);
    }

    public void addEntry(OPS operation, int oid, int tid, Integer[] bi, Integer[] ai, LogEntry backpointer){
        log.add(new LogEntry(operation, oid, tid, bi, ai, backpointer));
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


}
