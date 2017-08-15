package Maintenace;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class WeatherDatabaseMaintenance {
    private Properties props;
    private String url = null;
    private Properties dbProperties;
    private WeatherData wd = new WeatherData();
    Connection conn = null;
    /**
     * Connect to the PostgreSQL database
     *
     * @return a Connection object
     */
    public WeatherDatabaseMaintenance(Properties properties) {
        try {
            dbProperties = new Properties();
            url = properties.getProperty("database");
            String dbuser = properties.getProperty("dbuser");
            String dbpassword = properties.getProperty("dbpassword");
            String dbTimezone = properties.getProperty("timezone");
            dbProperties.setProperty("url", url);
            dbProperties.setProperty("user", dbuser);
            dbProperties.setProperty("password", dbpassword);
            dbProperties.setProperty("timezone", dbTimezone);
        } catch (Exception e) {
            System.out.println("Error reading properties: " + e.getMessage());
        }
    }

    public Connection connect() {
        conn = null;
        //props.setProperty("currentSchema","weather");

        try {
            //Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, dbProperties);
            System.out.println("Connected to the PostgreSQL server successfully.");
            //move to main
            Date oldestDate = getOldestProcessedDay();
            while (this.foundDataToProcess(oldestDate)) {
                // do process the day
                processHistory(oldestDate);
                // now see if there is more data to process
                oldestDate = getOldestProcessedDay();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    private Date getOldestProcessedDay() {
        boolean shouldProcessData = false;
        try {
            Date oldestDate = null;
            Statement stmt = null;
            conn.setAutoCommit(false);
            System.out.println("Opened database successfully");
            //LocalDateTime localDate = LocalDateTime.now();
            //localDate = LocalDateTime.parse(wd.dateutc);
            //System.out.println("new date: " + localDate.toString());
            stmt = conn.createStatement();
            String sql = "SELECT dateutc FROM weather.realtime ORDER BY dateutc ASC LIMIT 1"; // sorted from oldest first
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = rs.getString(1);
                //System.out.println( "dateString : " + dateString);
                oldestDate = df.parse(dateString);
                oldestDate.setHours(0);
                oldestDate.setMinutes(0);
                oldestDate.setSeconds(0);

                // the idea here is to see if we have data from yesterday or earlier that we have not processed yet.  However
                // the before method on date was returning the wrong result so this is a hack (on a hack of using deprecated methods)
                // to see if we have old data to work on
            }
            rs.close();
            stmt.close();
            conn.commit();
            return oldestDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean foundDataToProcess(Date oldestDate) {
        boolean shouldProcessData = false;
        try {
            Date today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);

            // the idea here is to see if we have data from yesterday or earlier that we have not processed yet.  However
            // the before method on date was returning the wrong result so this is a hack (on a hack of using deprecated methods)
            // to see if we have old data to work on
            String stringToday = today.toString();
            if (oldestDate == null)
                return false;
            String stringOldestDate = oldestDate.toString();
            //if (oldestDate.before(oldestDate)) {
            if (stringToday.compareTo(stringOldestDate) != 0) {
                System.out.println( "found old stuff: today : '" + today + "', oldestDate : '" + oldestDate + "'");
                shouldProcessData = true;
            }
            else {
                System.out.println("did not fine old stuff: today : '" + today + "', oldestDate : '" + oldestDate + "'");
                shouldProcessData = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shouldProcessData;
    }

    private boolean processHistory(Date dateToProcess) {
        boolean shouldProcessData = false;
        try {
            Statement stmt = null;
            conn.setAutoCommit(false);
            System.out.println("Opened database successfully to processHistory");
            //LocalDateTime localDate = LocalDateTime.now();
            //localDate = LocalDateTime.parse(wd.dateutc);
            //System.out.println("new date: " + localDate.toString());

            // set date range
            //String pattern = "EEE, dd MMM yyyy 00:00:00 Z";
            String pattern = "EEE MMM dd HH:mm:ss z yyyy";
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            java.sql.Timestamp startTime = new Timestamp(format.parse(dateToProcess.toString()).getTime());
            startTime.setHours(0);
            startTime.setMinutes(0);
            startTime.setSeconds(0);
            java.sql.Timestamp endTime = new Timestamp(format.parse(dateToProcess.toString()).getTime());
            endTime.setHours(23);
            endTime.setMinutes(59);
            endTime.setSeconds(59);

            stmt = conn.createStatement();
            String sql = "SELECT AVG(windspeedmph), AVG(winddir), MAX(windgustmph), MAX(dailyrainin), AVG(tempf), MAX(tempf), MIN(tempf), AVG(baromin), AVG(dewptf), " +
                    "AVG(humidity), MAX(humidity), MIN(humidity), AVG(solarradiation), MAX(solarradiation), AVG(UV), MAX(UV) FROM weather.realtime " +
                    "WHERE tenant='TENANT0' AND dateutc <= '"+ endTime + "-" + dbProperties.getProperty("timezone") +
                    "' AND dateutc >= '" + startTime + "-" + dbProperties.getProperty("timezone") + "'";
            System.out.println("sql : " + sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                wd.windspeedmph_average = rs.getString(1);
                wd.winddir_average = rs.getString(2);
                wd.windgustmph_max = rs.getString(3);
                wd.dailyrainin_total = rs.getString(4);
                wd.temp_f_average = rs.getString(5);
                wd.temp_f_max = rs.getString(6);
                wd.temp_f_min = rs.getString(7);
                wd.baromin_average = rs.getString(8);
                wd.dewptf_average = rs.getString(9);
                wd.humidity_average = rs.getString(10);
                wd.humidity_max = rs.getString(11);
                wd.humidity_min = rs.getString(12);
                wd.solarradiation_average = rs.getString(13);
                wd.solarradiation_max = rs.getString(14);
                wd.UV_average = rs.getString(15);
                wd.UV_max = rs.getString(16);

                sql = "INSERT INTO weather.daily_history(tenant, dateutc, windspeedmphaverage, winddiraverage, windgustmphmax, " +
                        "dailyrainin, tempfaverage, tempfmax, tempfmin, barominaverage, dewptfaverage, humidityaverage, humiditymax, " +
                        "humiditymin, solarradiationaverage, solarradiationmax, uvaverage, uvmax) " +
                        "VALUES ('TENANT0', '" + endTime + "+" + dbProperties.getProperty("timezone") + "', " + wd.windspeedmph_average + ", " + wd.winddir_average + ", " + wd.windgustmph_max +
                        ", " + wd.dailyrainin_total + ", " + wd.temp_f_average + ", " + wd.temp_f_max + ", " + wd.temp_f_min + ", " + wd.baromin_average +
                        ", " + wd.dewptf_average + ", " + wd.humidity_average + ", " + wd.humidity_max + ", " + wd.humidity_min +
                        ", " + wd.solarradiation_average + ", " + wd.solarradiation_max + ", " + wd.UV_average + ", " + wd.UV_max + ")";
                System.out.println("sql : " + sql);
                try {
                    stmt.executeUpdate(sql);
                } catch (Exception e) {
                    stmt.cancel();
                    System.out.println("Could not execute insert statement, canceling: " + e.getMessage());
                }
            }
            rs.close();
            // now need to delete the old data
            String deleteSql = "DELETE FROM weather.realtime WHERE tenant='TENANT0' AND dateutc <= '"+ endTime + "-" + dbProperties.getProperty("timezone") +
                    "' AND dateutc >= '" + startTime + "-" + dbProperties.getProperty("timezone") + "'";
            System.out.println("Executing deleting statment: " + deleteSql);
            try {
                stmt.executeUpdate(deleteSql);
            } catch (Exception e) {
                stmt.cancel();
                System.out.println("Could not execute delete statement, canceling: " + e.getMessage());
            }
            stmt.close();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shouldProcessData;
    }

    public boolean updateDailyHistory(WeatherData wd) {
        try {
            Statement stmt = null;
            conn.setAutoCommit(false);
            System.out.println("Opened database successfully");

            //LocalDateTime localDate = LocalDateTime.now();
            //localDate = LocalDateTime.parse(wd.dateutc);
            //System.out.println("new date: " + localDate.toString());
            stmt = conn.createStatement();
            String sql = "INSERT INTO weather.realtime (tenant,dateutc,windspeedmph,winddir,windgustmph,dailyrainin,tempf,baromin,dewptf,humidity,solarradiation,UV) "
                    + "VALUES ('TENANT0', '" + wd.dateutc + "'," + wd.windspeedmph + "," + wd.winddir + "," + wd.windgustmph + "," + wd.dailyrainin + ","
                    + wd.temp_f + "," + wd.baromin + "," + wd.dewptf + "," + wd.humidity + "," + wd.solarradiation + "," + wd.UV + ");";
            System.out.println("SQL statement: " + sql);
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    public void close() {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

