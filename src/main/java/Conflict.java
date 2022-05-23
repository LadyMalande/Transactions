public class Conflict {
    boolean RW;
    boolean WR;
    boolean WW;
    boolean incident;

    public Conflict(boolean rw, boolean wr, boolean ww, boolean i){
        RW = rw;
        WR = wr;
        WW = ww;
        incident = i;
    }

    @Override
    public String toString() {
        String rw = RW ? "1" : "0";
        String wr = WR ? "1" : "0";
        String ww = WW ? "1" : "0";
        return "<RW" + rw +
                ", WR" + wr +
                ", WW" + ww +
                ">";
    }
}
