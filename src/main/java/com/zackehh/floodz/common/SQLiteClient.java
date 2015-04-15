package com.zackehh.floodz.common;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.corba.common.Reading;
import com.zackehh.corba.common.SensorMeta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteClient {

    private final Connection db;
    private static SQLiteClient instance = null;

    private SQLiteClient() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Unable to load db driver!");
        }

        db = DriverManager.getConnection("jdbc:sqlite:local.db");

        Statement createTableStatement = db.createStatement();

        createTableStatement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ALERTS " +
                "(ID            INTEGER     PRIMARY KEY    AUTOINCREMENT," +
                " LMS           TEXT        NOT NULL," +
                " TIME          DATE        NOT NULL," +
                " ZONE          CHAR(26)    NOT NULL," +
                " SENSOR        CHAR(4)     NOT NULL," +
                " MEASUREMENT   INT         NOT NULL)"
        );

        createTableStatement.close();
    }

    public static SQLiteClient getInstance() {
        try {
            if (instance == null) {
                instance = new SQLiteClient();
            }
            return instance;
        } catch(SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean deleteAlert(MetaData metadata) {
        try {
            PreparedStatement deleteStatement = db.prepareStatement(
                    "DELETE FROM ALERTS WHERE LMS=? AND ZONE=? AND SENSOR=?;"
            );

            deleteStatement.setString(1, metadata.lms);
            deleteStatement.setString(2, metadata.sensorMeta.zone);
            deleteStatement.setString(3, metadata.sensorMeta.sensor);

            deleteStatement.executeUpdate();
            deleteStatement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized boolean insertAlert(Alert alert) {
        try {
            PreparedStatement insertStatement = db.prepareStatement(
                    "INSERT INTO ALERTS (LMS,TIME,ZONE,SENSOR,MEASUREMENT)" +
                    "VALUES (?,?,?,?,?);"
            );

            insertStatement.setString(1, alert.meta.lms);
            insertStatement.setLong(2, alert.reading.time);
            insertStatement.setString(3, alert.meta.sensorMeta.zone);
            insertStatement.setString(4, alert.meta.sensorMeta.sensor);
            insertStatement.setInt(5, alert.reading.measurement);

            insertStatement.executeUpdate();
            insertStatement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public synchronized boolean updateAlert(Alert alert) {
        try {
            PreparedStatement updateStatement = db.prepareStatement(
                    "UPDATE ALERTS " +
                            "SET MEASUREMENT=? AND TIME=? " +
                            "WHERE LMS=? AND ZONE=? AND SENSOR=?;"
            );

            updateStatement.setInt(1, alert.reading.measurement);
            updateStatement.setLong(2, alert.reading.time);
            updateStatement.setString(3, alert.meta.lms);
            updateStatement.setString(4, alert.meta.sensorMeta.zone);
            updateStatement.setString(5, alert.meta.sensorMeta.sensor);

            updateStatement.executeUpdate();
            updateStatement.closeOnCompletion();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Alert> retrieveAllAlerts() {
        List<Alert> alertLog = new ArrayList<>();

        try {
            Statement alertQueryStatement = db.createStatement();

            ResultSet resultSet = alertQueryStatement
                    .executeQuery("SELECT * FROM ALERTS ORDER BY TIME ASC;");

            while (resultSet.next()) {
                alertLog.add(new Alert(
                        new MetaData(
                                resultSet.getString("LMS"),
                                new SensorMeta(resultSet.getString("ZONE"), resultSet.getString("SENSOR"), 0)
                        ),
                        new Reading(resultSet.getLong("TIME"), resultSet.getInt("MEASUREMENT"))
                ));
            }

            resultSet.close();
            alertQueryStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return alertLog;
    }

    public List<String> getStoredLMSNames() {
        List<String> lmsNames = new ArrayList<>();

        try {
            Statement lmsNamesStatement = db.createStatement();

            ResultSet resultSet = lmsNamesStatement
                    .executeQuery("SELECT DISTINCT LMS FROM ALERTS;");

            while (resultSet.next()) {
                lmsNames.add(resultSet.getString("LMS"));
            }

            resultSet.close();
            lmsNamesStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lmsNames;
    }

}