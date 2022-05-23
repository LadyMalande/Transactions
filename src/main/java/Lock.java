public class Lock {
    public Integer tid;
    public Mode mode;

    public Lock(Integer i, Mode m){
        this.tid = i;
        this.mode = m;
    }
}
