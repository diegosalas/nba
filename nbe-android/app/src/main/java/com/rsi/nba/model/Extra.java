package com.rsi.nba.model;

public class Extra {

    //Atributos
    private String nombreAndroid;
    private String nombreIos;
    private String idIos;
    private int codigoRetorno;
    private Data data;
    private String userProfile;
    private String pattern;
    private String textToCopy;
    private String token;
    private String url;
    private String title;
    private String desc;
    private String flag;
    private String biometricValue;
    private String color;

    //Constructor
    public Extra() {
    }

    //Getters
    public String getNombreAndroid() {
        return nombreAndroid;
    }

    public String getNombreIos() {
        return nombreIos;
    }

    public String getIdIos() {
        return idIos;
    }

    public int getCodigoRetorno() {
        return codigoRetorno;
    }

    public Data getData() {
        return data;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public String getPattern() {
        return pattern;
    }

    public String getTextToCopy() {
        return textToCopy;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getFlag() {
        return flag;
    }

    public String getBiometricValue() {
        return biometricValue;
    }

    public String getColor() {
        return color;
    }

    //Setters
    public void setNombreAndroid(String nombreAndroid) {
        this.nombreAndroid = nombreAndroid;
    }

    public void setNombreIos(String nombreIos) {
        this.nombreIos = nombreIos;
    }

    public void setIdIos(String idIos) {
        this.idIos = idIos;
    }

    public void setCodigoRetorno(int codigoRetorno) {
        this.codigoRetorno = codigoRetorno;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setTextToCopy(String textToCopy) {
        this.textToCopy = textToCopy;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setBiometricValue(String biometricValue) {
        this.biometricValue = biometricValue;
    }

    //Tostring sin filtrar

    @Override
    public String toString() {
        return "{" +
                "nombreAndroid:'" + nombreAndroid + '\'' +
                ", nombreIos:'" + nombreIos + '\'' +
                ", idIos:'" + idIos + '\'' +
                ", codigoRetorno:" + codigoRetorno +
                ", data:" + data +
                ", userProfile:'" + userProfile + '\'' +
                ", pattern:'" + pattern + '\'' +
                ", textToCopy:'" + textToCopy + '\'' +
                ", token:'" + token + '\'' +
                ", title:'" + title + '\'' +
                ", desc:'" + desc + '\'' +
                ", flag:'" + flag + '\'' +
                '}';
    }

}
