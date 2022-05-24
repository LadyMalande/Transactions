import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Resources {
    public static int getNumberOfTransactions(){
        int numberOfTransactions = 0;

        try (InputStream input = new FileInputStream(Resources.class.getResource("config.properties").getFile())) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            numberOfTransactions = Integer.parseInt(prop.getProperty("numberOfTransactions"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return numberOfTransactions;
    }

    public static float getProbabilityOfAbort() {
        float probabilityOfAbort = 0;
        try (InputStream input = new FileInputStream(Resources.class.getResource("config.properties").getFile())) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            probabilityOfAbort = Float.parseFloat(prop.getProperty("probabilityOfAbort"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return probabilityOfAbort;
    }

    public static int getNumberOfOperations() {
        int numberOfOperations = 0;
        try (InputStream input = new FileInputStream(Resources.class.getResource("config.properties").getFile())) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            numberOfOperations = Integer.parseInt(prop.getProperty("numberOfOperations"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return numberOfOperations;
    }

    public static boolean isFinalAbort() {
        boolean finalAbort = false;
        try (InputStream input = new FileInputStream(Resources.class.getResource("config.properties").getFile())) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            finalAbort = Boolean.parseBoolean(prop.getProperty("finalAbort"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return finalAbort;
    }

    public static void setFinalAbort(boolean fa) {
        try (OutputStream output = new FileOutputStream("config.properties")) {

            Properties prop = new Properties();

            // set the properties value
            prop.setProperty("finalAbort", String.valueOf(fa));

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static boolean sameContent(Path file1, Path file2) {
        try {
            return Files.mismatch(file1, file2) == -1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
