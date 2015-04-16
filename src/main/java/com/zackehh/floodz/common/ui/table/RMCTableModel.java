package com.zackehh.floodz.common.ui.table;

import com.zackehh.corba.common.Alert;
import com.zackehh.corba.common.MetaData;
import com.zackehh.floodz.util.SQLiteClient;
import com.zackehh.floodz.rmc.RMCUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class RMCTableModel extends UneditableTableModel {

    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final SQLiteClient sqLiteClient;

    private final ConcurrentHashMap<String, Integer> alerts = new ConcurrentHashMap<>();

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

    public void addAlert(Alert alert){
        addAlert(alert, true);
    }

    public void addAlert(Alert alert, boolean persist) {
        Date d = new Date(alert.reading.time);

        String id = RMCUtil.generateAlertId(alert);
        String dateString =  hourFormat.format(d) + " on " + dateFormat.format(d);

        if(!alerts.containsKey(id)){

            if(persist) {
                sqLiteClient.insertAlert(alert);
            }

            int index = getRowCount();

            alerts.put(id, index);

            insertRow(index, new String[]{
                    alert.meta.lms,
                    dateString,
                    alert.meta.sensorMeta.zone + "/" + alert.meta.sensorMeta.sensor,
                    alert.reading.measurement + "%"
            });
        } else {
            int index = alerts.get(id);

            sqLiteClient.updateAlert(alert);

            String str = getValueAt(index, 3).toString();
            int measure = Integer.valueOf(str.substring(0, str.length() - 1));

            if(measure != alert.reading.measurement){
                setValueAt(dateString, index, 1);
                setValueAt(alert.reading.measurement + "%", index, 3);
            }
        }
    }

    public void removeAlert(MetaData metadata){
        String id = RMCUtil.generateAlertId(metadata);

        if(alerts.containsKey(id)){
            int index = alerts.get(id);

            sqLiteClient.deleteAlert(metadata);

            alerts.remove(id);

            removeRow(index);
        }
    }

}
