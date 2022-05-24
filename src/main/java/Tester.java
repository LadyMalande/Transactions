import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

// Properties tester on global schedules
public class Tester {
    private static final int NUMBER_OF_THREADS = 5;
    private static final float ABORT_PROBABILITY = (float) 0.01;
    ArrayList<ArrayList<Operation>> globalSchedules;
    Map<Integer, ArrayList<Operation> > schedules;
    ArrayList<ArrayList<PropertiesToCheck>> propertiesToCheck;
    ArrayList<StringBuilder> satisfies;
    BufferedWriter bufferedWriter;
    public Tester (){
        globalSchedules = new ArrayList<>();
        propertiesToCheck = new ArrayList<>();
        satisfies = new ArrayList<>();
        //readGlobalScheduleFromFile();
        Generator g = new Generator();
        schedules = g.generateTransactionSchedules();
        PrettyWriter.prettyWriteSchedules(schedules);
        PrettyWriter.prettyWriteSchedulesToFile(schedules);
        //writeGlobalScheduleToOut(2);
        //System.out.println(isGlobalScheduleRecoverable(globalSchedules.get(2)));

        /*
        testRecoverability();
        testSerializability();
        writeResults();

         */
    }

    public void testRecoverability(){
        for(ArrayList<Operation> schedule : globalSchedules){
            StringBuilder sb = new StringBuilder();
                sb.append(" Recoverable: ").append(isGlobalScheduleRecoverable(schedule));
            satisfies.add(sb);
        }
    }

    public void testSerializability(){
        int i = 0;
        for(ArrayList<Operation> schedule : globalSchedules){
            satisfies.get(i).append(" Serializable: "
                    //+ String.valueOf(Main.isThereTheSameSequenceSchedule(dm.schedules, db))
                    );
            i++;
        }
    }

    private void writeResults(){
        int i = 1;
        for(StringBuilder s : satisfies){
            System.out.println(i + " " + s);
            i++;
        }
    }

    private void writeGlobalScheduleToOut(int index){

        for(Operation op : globalSchedules.get(index)){
            System.out.print(op.toString() + ";");
        }
        System.out.println();
    }



    private boolean isGlobalScheduleRecoverable(ArrayList<Operation> schedule){
        // <<index of the variable AND writer>, list of dependant transactions (R after W)>
        HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> map = new HashMap<>();

        for(Operation op : schedule){
            if(op.rw == OPS.WRITE){
                if(map.get(op.index) != null){
                    map.get(op.index).computeIfAbsent(op.tid, k -> new ArrayList<>());
                } else if (map.get(op.index) == null){
                    HashMap<Integer, ArrayList<Integer>> newMap = new HashMap<>();
                    newMap.put(op.tid, new ArrayList<>());
                    map.put(op.index, newMap);
                }

            } else if (op.rw == OPS.READ) {
                if(map.get(op.index) != null){
                    for (Map.Entry<Integer, ArrayList<Integer>> readers : map.get(op.index).entrySet()) {
                        if(!Objects.equals(readers.getKey(), op.tid)){
                            readers.getValue().add(op.tid);
                        }
                    }
                }

            } else if (op.rw == OPS.COMMIT){
                for (HashMap<Integer, ArrayList<Integer>> maps : map.values()) {
                    int countedCommits = 0;
                    if(maps.get(op.tid) != null){
                        for(Integer reader : maps.get(op.tid)){

                            // See if all the dependant readers have commits after this commit
                            int indexOfThisOperation = schedule.indexOf(op);
                            for(int i = indexOfThisOperation + 1; i < schedule.size(); i++){
                                if(schedule.get(i).rw == OPS.COMMIT && reader.equals(schedule.get(i).tid) ){
                                    countedCommits++;
                                }
                            }

                        }
                        if(countedCommits < maps.get(op.tid).size()){
                            return false;
                        }
                    }

                }
            }
        }

        return true;
    }
/*
    public boolean isGlobalScheduleS2PL(ArrayList<Operation> schedule){

    }

 */

    private void readGlobalScheduleFromFile(){
        ArrayList<Operation> globalSchedule = new ArrayList<>();
        try {
            File myObj = new File("testData.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                ArrayList<PropertiesToCheck> properties = new ArrayList<>();
                propertiesToCheck.add(properties);
                String line = myReader.nextLine();
                String[] str = line.split(";");
                for (int i = 0; i < str.length; i++) {
                    System.out.println(str[i] + " index: " + i);
                    if(str[i].length() > 0 && str[i].charAt(0) == ' '){
                        // test result at the line end begins with space
                        if(str[i].length() > 5 && str[i].startsWith("not CS", 1)){
                            properties.add(PropertiesToCheck.NOTCS);
                        }
                        if(str[i].length() > 5 && str[i].startsWith("not R", 2)){
                            properties.add(PropertiesToCheck.NOTR);
                        }
                        if(str[i].length() > 8 && str[i].startsWith("not R", 8)){
                            properties.add(PropertiesToCheck.NOTR);
                        }
                    } else if(str[i].length() > 0 && str[i].length() < 5){
                        // parsing commit
                        globalSchedule.add(new Operation(Operation.parseFromString(str[i].substring(0,1)),Integer.parseInt(str[i].substring(1,2)), -1, 0, ""));

                    } else if (str[i].length() > 0){
                        // parsing RW
                        globalSchedule.add(new Operation(Operation.parseFromString(str[i].substring(0,1)),Integer.parseInt(str[i].substring(1,2)), Integer.parseInt(str[i].substring(4,5)), 0, str[i].substring(3,4)));

                    }
                }
                globalSchedules.add(new ArrayList<>(globalSchedule));
                globalSchedule = new ArrayList<>();
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
