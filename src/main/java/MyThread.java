import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;


public class MyThread extends Thread{
    CyclicBarrier cb;
    ArrayList<String> simplifiedOperations;
    ArrayList<String> operations;
    int y;
    int x;
    int index, lastIndex;
    String tabs;
    DataManager dm;
    int tid;
    int NUMBER_OPERATIONS;
    public MyThread(int i, CyclicBarrier cyclicBarrier, DataManager dataManager, int numOfOps){
        cb = cyclicBarrier;
        tid = i;
        dm = dataManager;
        simplifiedOperations = new ArrayList<>(NUMBER_OPERATIONS);
        operations = new ArrayList<>(NUMBER_OPERATIONS);
        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < tid; j++ ){
            sb.append("\t");
        }
        tabs = sb.toString();
        NUMBER_OPERATIONS = numOfOps - 1;
    }

    public void run(){
        try {
            cb.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        System.out.println("Thread " + tid  + " runs");
        for(int i = 0; i < NUMBER_OPERATIONS; i++){
            doSth();
            try {
                sleep((long)(Math.random() * 100));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        doCommit();
    }

    private void doSth(){
// nextInt is normally exclusive of the top value,
// so add 1 to make it inclusive
        int operation = ThreadLocalRandom.current().nextInt(0, 2);
        int index = ThreadLocalRandom.current().nextInt(0, 10);
        y = ThreadLocalRandom.current().nextInt(0, 10);
        switch(operation){
            case 0: read(index);
            break;
            case 1: write(index);
            break;
            default: System.out.println("Bad operation " + operation);
        }
    }

    private void read(int index){
        Operation op = new Operation(OPS.READ, tid, index, 0,"X");
        synchronized(dm){
            x = dm.doSth(op, true);
        }
        System.out.println(op);
        lastIndex = index;
    }

    private void write(int index){

        if(lastIndex == index){
            Operation op = new Operation(OPS.WRITE, tid, index, x+y, "X");
            synchronized (dm) {
                dm.doSth(op, true);
            }
            System.out.println(op);
        } else{
            Operation op = new Operation(OPS.WRITE, tid, index, y, "X");
            synchronized (dm) {
                dm.doSth(op, true);
            }
            lastIndex = index;
            System.out.println(op);
        }

    }

    private void doCommit(){
        Operation op = new Operation(OPS.COMMIT, tid, 0, 0, "X");
        dm.doSth(op, true);
        System.out.println(op);
    }

    public ArrayList<String> getOperations(){
        return operations;
    }

}
