package com.example.english.demo.entity;

public class PropertyInfo {
    private String name;
    private String type;

    public PropertyInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PropertyInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}