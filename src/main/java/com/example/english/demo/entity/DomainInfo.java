package com.example.english.demo.entity;

import java.util.List;

public class DomainInfo {
    private String className;
    private List<PropertyInfo> properties;

    public DomainInfo(String className, List<PropertyInfo> properties) {
        this.className = className;
        this.properties = properties;
    }

    public String getClassName() {
        return className;
    }

    public List<PropertyInfo> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "DomainInfo{" +
                "className='" + className + '\'' +
                ", properties=" + properties +
                '}';
    }
}