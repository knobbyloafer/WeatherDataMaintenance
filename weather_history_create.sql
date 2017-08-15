-- Table: weather.realtime

-- DROP TABLE weather.realtime;

-- Table: weather.daily_history

-- DROP TABLE weather.daily_history;

CREATE TABLE weather.daily_history
(
  tenant character varying NOT NULL,
  dateutc timestamp with time zone NOT NULL,
  windspeedmphaverage real,
  winddiraverage smallint,
  windgustmphmax real,
  dailyrainin real,
  tempfaverage real,
  tempfmax real,
  tempfmin real,
  barominaverage real,
  dewptfaverage real,
  humidityaverage real,
  humiditymax real,
  humiditymin real,
  solarradiationaverage smallint,
  solarradiationmax smallint,
  uvaverage real,
  uvmax real,
  CONSTRAINT daily_history_pkey PRIMARY KEY (tenant, dateutc)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE weather.daily_history
  OWNER TO weather_user;
GRANT ALL ON TABLE weather.daily_history TO weather_user;
