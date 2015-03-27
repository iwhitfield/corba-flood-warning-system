package com.zackehh.floodz.common;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Levels {

    private Integer alert_level;
    private Integer warning_level;

    @SuppressWarnings("unused")
    public Levels(){
        // no-op
    }

    public Levels(int alert_level, int warning_level){
        this.alert_level = alert_level;
        this.warning_level = warning_level;
    }

    public Integer getAlertLevel(){
        return alert_level;
    }

    public Integer getWarningLevel(){
        return warning_level;
    }

    @SuppressWarnings("unused")
    @JsonSetter("alert_level")
    public void setAlertLevel(int alert_level){
        this.alert_level = alert_level;
    }

    @SuppressWarnings("unused")
    @JsonSetter("warning_level")
    public void setWarningLevel(int warning_level){
        this.warning_level = warning_level;
    }

}