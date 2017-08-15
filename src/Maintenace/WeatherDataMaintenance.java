package Maintenace;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class WeatherDataMaintenance {
    public static void main(String[] args) {
        try {
            Properties prop = new Properties();
            InputStream input = null;
            input = new FileInputStream("weatherdata.properties");

            // load a properties file
            prop.load(input);
            WeatherDatabaseMaintenance dbConnection = new WeatherDatabaseMaintenance(prop);

            while (true) {
                //System.out.println("getting weather data..."); // Display the string.
                try {
                    // TODO need much better sleep mechanism.  should wake early in the morning and run once a day
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("k"); // hour 1-24
                    String time = formatter.format(date);
                    int hour = Integer.parseInt(time);
                    if (hour >= 0 && hour <= 24) {
                        System.out.println("Running db maintenance and sleeping for an hour");
                        // do work and then sleep for a long time
                        dbConnection.connect();
                        Thread.sleep(60000 * 60 * 6); // sleep for 6 hours
                    }
                    else
                    {
                        System.out.println("Not in windows, sleeping for another hour");
                        // otherwise check every hour
                        Thread.sleep(60000 * 60); // sleep for an hour
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("Error in main: " + e.getMessage());
        }
    }
}
