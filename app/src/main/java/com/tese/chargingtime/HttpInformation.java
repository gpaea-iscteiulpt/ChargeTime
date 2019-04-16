package com.tese.chargingtime;

public class HttpInformation {

    public StringBuffer response;
    public String url;
    public int value;

    public HttpInformation(String url, StringBuffer response){
        this.url = url;
        this.response = response;
    }

    public void setValue(int value){
        this.value = value;
    }

}
