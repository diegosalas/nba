package com.rsi.nba.model;

public class Data {

    //Atributos
    private Integer code;
    private String description;
    private String response;


    //Constructor
    public Data() {
    }

    //Getters
    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getResponse() {
        return response;
    }

    //Setters
    public void setCode(Integer code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    //To String sin Filtrar
    @Override
    public String toString() {
        return "{" +
                "code:" + this.getCode() +
                ", description:'" + this.getDescription() + '\'' +
                ", response:'" + this.getResponse() + '\'' +
                '}';
    }

}
