
package com.ljapps.tripactionchallenge.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Byline {

    private List<Person> person = null;
    private String original;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public List<Person> getPerson() {
        return person;
    }

    public void setPerson(List<Person> person) {
        this.person = person;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
