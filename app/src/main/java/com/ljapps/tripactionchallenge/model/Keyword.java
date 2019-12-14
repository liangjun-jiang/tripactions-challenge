
package com.ljapps.tripactionchallenge.model;

import java.util.HashMap;
import java.util.Map;

public class Keyword {

    private String rank;
    private String isMajor;
    private String name;
    private String value;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getIsMajor() {
        return isMajor;
    }

    public void setIsMajor(String isMajor) {
        this.isMajor = isMajor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
