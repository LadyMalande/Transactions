import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Resources {
    public static int getNumberOfTransactions(){
        int numberOfTransactions = 0;
        try (InputStream input = new FileInputStream("resources/config.properties")) {

            Properties prop = new Properties();

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            numberOfTransactions = Integer.parseInt(prop.getProperty("not"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return numberOfTransactions;
    }
}
