package com.iproject.tapstor.rest;

public class SendPostValueProfileUpdate {

    private String token;
    private String l_name;
    private String f_name;

    public SendPostValueProfileUpdate(String token, String l_name, String f_name) {

        setF_name(f_name);
        setL_name(l_name);
        setToken(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getL_name() {
        return l_name;
    }

    public void setL_name(String l_name) {
        this.l_name = l_name;
    }

    public String getF_name() {
        return f_name;
    }

    public void setF_name(String f_name) {
        this.f_name = f_name;
    }
}

// {"token":"38025071b295df68d4fdb705625223724a0fb2b4","l_name":"eponimo","f_name":"onoma"}
