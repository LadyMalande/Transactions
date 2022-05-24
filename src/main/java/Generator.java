import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Generator {

    int numberOfTransactions;
    float probabilityOfAbort;
    int NUMBER_OF_OPERATIONS = 10;


    public Generator(){
        this.numberOfTransactions = Resources.getNumberOfTransactions();
        this.probabilityOfAbort = Resources.getProbabilityOfAbort();
    }

    public Map<Integer, ArrayList<Operation>> generateTransactionSchedules(){
        Map<Integer, ArrayList<Operation>> schedules = new HashMap<>();
        for(int i = 0; i < numberOfTransactions; i++){
            schedules.put(i, generateOneTransaction(i));
        }
        return schedules;
    }

    private ArrayList<Operation> generateOneTransaction(int tid){
        ArrayList<Operation> actions = new ArrayList<>();

        for(int i = 0; i < NUMBER_OF_OPERATIONS; i++){
            int operation = ThreadLocalRandom.current().nextInt(2);
            int index = ThreadLocalRandom.current().nextInt(10);
            int y = ThreadLocalRandom.current().nextInt(10);
            int probabilityOfAbortTimes = Math.round(probabilityOfAbort*10000);
            int abortOrnot = ThreadLocalRandom.current().nextInt(probabilityOfAbortTimes);
            //System.out.println(probabilityOfAbortTimes + ", " + abortOrnot);
            // With some probabiliy do ABORT of the transaction
            if(i > 0 && ( abortOrnot == 0)){
                operation = 2;
            }

            switch(operation){
                case 0: actions.add(new Operation(OPS.READ, tid, index,y,""));
                    break;
                case 1: actions.add(new Operation(OPS.WRITE, tid, index,y,""));
                    break;
                case 2: {
                    actions.add(new Operation(OPS.ABORT, tid, 0, 0, ""));
                    return actions;
                }
                default: System.out.println("Bad operation " + operation);
            }
        }
        // the last operation is commit
        actions.add(new Operation(OPS.COMMIT, tid, 0,0,""));
        return actions;
    }
}
