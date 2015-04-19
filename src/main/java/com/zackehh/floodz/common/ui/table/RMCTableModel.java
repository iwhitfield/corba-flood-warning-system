package com.zackehh.floodz.common.ui.table;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.floodz.util.SQLiteClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The table model used by the table displayed inside the RMC
 * GUI. This has to be a custom table model due to the need to
 * keep track of previously-added alerts. It is much uglier to
 * handle such a thing in the GUI itself.
 */
public class RMCTableModel extends UneditableTableModel {

    /**
     * A map to store lms/zone->alert mappings.
     */
    private final ConcurrentHashMap<String, Integer>
            alerts = new ConcurrentHashMap<>();

    /**
     * A date formatter for use with timestamps.
     */
    private final SimpleDateFormat
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * A time formatter for use with timestamps.
     */
    private final SimpleDateFormat
            hourFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * A reference to the SQLiteClient singleton.
     */
    private final SQLiteClient sqLiteClient;

    /**
     * Accept Vector input.
     *
     * @param data          the data Vector
     * @param columns       the columns Vector
     */
    public RMCTableModel(SQLiteClient client, Vector<Vector<String>> data, Vector<String> columns){
        super(data, columns);
        this.sqLiteClient = client;
    }

    /**
     * Accept Array input.
     *
     * @param data          the data Array
     * @param columns       the columns Array
     */
    @SuppressWarnings("unused")
    public RMCTableModel(SQLiteClient client, Object[][] data, Object[] columns){
        super(data, columns);
        this.sqLiteClient = client;
    }

    /**
     * Simple wrapper to ${@link #addAlert(Alert, boolean)} for convenience.
     *
     * @param alert the alert instance
     */
    public void addAlert(Alert alert){
        addAlert(alert, true);
    }

    /**
     * Adds an alert to the table model. Takes a parameters to
     * define whether the result also needs to be persisted to the
     * local SQLite DB. This is required otherwise loading the list
     * of alerts from the DB would duplicate entries.
     *
     * @param alert the Alert instance
     * @param persist whether to persist or not
     */
    public void addAlert(Alert alert, boolean persist) {
        // create a Date from the timestamp
        Date d = new Date(alert.reading.time);

        // generate the id
        String id = generateAlertId(alert.meta);
        // generate the date string to display
        String dateString =  hourFormat.format(d) + " on " + dateFormat.format(d);

        // if this is a new alert
        if(!alerts.containsKey(id)){

            // persist if appropriate
            if(persist) {
                sqLiteClient.insertAlert(alert);
            }

            // find the last index in the table
            int index = getRowCount();

            // add the alert to the local map
            alerts.put(id, index);

            // add a row to the end of the table
            insertRow(index, new String[]{
                    alert.meta.lms,
                    dateString,
                    alert.meta.sensorMeta.zone + "/" + alert.meta.sensorMeta.sensor,
                    alert.reading.measurement + "%"
            });
        } else {
            // find the index of the alert in the table
            int index = alerts.get(id);

            // update it on disk
            sqLiteClient.updateAlert(alert);

            // find the currently recorded measurement
            String str = getValueAt(index, 3).toString();

            // parse it as an integer
            int measure = Integer.valueOf(str.substring(0, str.length() - 1));

            // if they do not match
            if(measure != alert.reading.measurement){
                // update the displayed date
                setValueAt(dateString, index, 1);
                // update the measurement on screen
                setValueAt(alert.reading.measurement + "%", index, 3);
            }
        }
    }

    /**
     * Simply returned <lms>:<zone> for tracking.
     *
     * @param meta the alert metadata
     * @return a String identifier
     */
    private String generateAlertId(MetaData meta){
        return meta.lms + ":" + meta.sensorMeta.zone;
    }

    /**
     * Removes an alert from the table. This also removes the
     * alert from the local SQLite DB, and the in-memory tracking.
     *
     * @param metadata the alert metadata
     */
    public void removeAlert(MetaData metadata){
        // grab the id from the metadata
        String id = generateAlertId(metadata);
        // if the alert contains this alert
        if(alerts.containsKey(id)){
            // find the alert index
            int index = alerts.get(id);
            // remove it from the DB
            sqLiteClient.deleteAlert(metadata);
            // remove it from the alert map
            alerts.remove(id);
            // remove the row from the table
            removeRow(index);
        }
    }
}
