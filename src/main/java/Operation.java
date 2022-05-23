public class Operation {

    OPS rw;
    Integer tid;
    int index;
    int newValue;
    String variable;

    public Operation(OPS rw, int tid, int index, int newValue, String variable){
        this.rw = rw;
        this.tid = tid;
        this.index = index;
        this.newValue = newValue;
        this.variable = variable;
    }

    @Override
    public String toString() {
        String OP;
        String ending;
        switch(rw){
            case READ: OP = "R";
                ending = "(" + index + ","+ newValue + ")" ;
                break;
            case WRITE: OP = "W";
                ending = "(" + index + ","+ newValue + ")" ;
                break;
            case COMMIT: OP = "C";
                ending = "      ";
                break;
            case ABORT: OP = "A";
                ending = "      ";
                break;
            case START: OP = "S";
                ending = "      ";
                break;
            default: {OP = "";ending = "";}

        }

        return OP +
                tid +
                " " + ending;
    }

    public static OPS parseFromString(String op) throws Exception {
        switch(op) {
            case "W":
                return OPS.WRITE;

            case "R":
                return OPS.READ;

            case "C":
                return OPS.COMMIT;

            case "A":
                return OPS.ABORT;

            case "S":
                return OPS.START;

            default:
                throw new Exception("Bad parse of string to OPS.");
        }

    }
}
