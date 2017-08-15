package Maintenace;

public class WeatherDataMaintenance {
    public static void main(String[] args) {
        WeatherDatabaseMaintenance dbConnection = new WeatherDatabaseMaintenance();

        while (true) {
            //System.out.println("getting weather data..."); // Display the string.
            try {
                dbConnection.connect();
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
