package com.zackehh.floodz.common.util;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Just here to provide checked access to alert and warning levels
 * defined for zones. Immutable, used by Jackson.
 */
public class Levels {

    /**
     * The level that alerts are triggered at.
     */
    private Integer alert_level;

    /**
     * The level that warnings are triggered at, if configured.
     */
    private Integer warning_level;

    /**
     * Default constructor, allowing for an alert level and a
     * warning level to be passed in. Either *can* be null (but
     * they should not be, without good reason). This is also
     * used by Jackson when (de)serializing).
     *
     * @param alert_level the alert level threshold
     * @param warning_level the warning level threshold
     */
    public Levels(@JsonProperty("alert_level") int alert_level,
                  @JsonProperty("warning_level") int warning_level){
        this.alert_level = alert_level;
        this.warning_level = warning_level;
    }

    /**
     * Returns the alert level bound to this instance.
     *
     * @return an Integer alert level (can be null)
     */
    public Integer getAlertLevel(){
        return alert_level;
    }

    /**
     * Returns the warning level bound to this instance.
     *
     * @return an Integer warning level (can be null)
     */
    public Integer getWarningLevel(){
        return warning_level;
    }

}