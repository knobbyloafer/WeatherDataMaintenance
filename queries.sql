select * from weather.realtime ORDER BY dateutc DESC
select count(*) from weather.realtime

select * from weather.daily_history ORDER BY dateutc DESC

-- delete from weather.daily_history

SELECT * FROM weather.realtime WHERE tenant='TENANT0' AND dateutc <= '2017-08-10 23:59:59+04' AND dateutc >= '2017-08-10 00:00:00+04' ORDER BY dateutc

select * from weather.daily_history

SELECT AVG(windspeedmph), AVG(winddir), MAX(windgustmph), MAX(dailyrainin), AVG(tempf), MAX(tempf), MIN(tempf), AVG(baromin), AVG(dewptf),
                    AVG(humidity), MAX(humidity), MIN(humidity), AVG(solarradiation), MAX(solarradiation), AVG(UV), MAX(UV) FROM weather.realtime
                    WHERE tenant='TENANT0' AND dateutc <= '2017-08-10 23:59:59.0+04' AND dateutc >= '2017-08-10 00:00:00.0+04'