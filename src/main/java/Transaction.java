import java.util.ArrayList;

public class Transaction {

    // For commit projection, to repeat sequentially all transactions
    ArrayList<Operation> actions;
    // For case of deadlock - the transaction with least locks is being aborted
    int numberOfLocks;



}
