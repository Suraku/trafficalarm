package com.suraku.trafficalarm.viewmodels;

/**
 * Model
 */
public class ApiGMapResult
{
    private int durationSeconds;
    private String jsonString;
    private String errorMessage;

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int value) { this.durationSeconds = value; }

    public String getJSONString() { return jsonString; }
    public void setJSONString(String value)  { this.jsonString = value; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String val) { this.errorMessage = val; }
}
