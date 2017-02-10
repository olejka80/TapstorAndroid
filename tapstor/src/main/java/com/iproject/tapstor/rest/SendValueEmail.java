package com.iproject.tapstor.rest;

public class SendValueEmail {

    private String token;
    private String type;
    private String company;
    private String tel;
    private String address;
    private String email;
    private String email2;
    private String name;
    private String tel2;
    private String message;

    public SendValueEmail() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String email2) {
        this.email2 = email2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel2() {
        return tel2;
    }

    public void setTel2(String tel2) {
        this.tel2 = tel2;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void SendValueEmailSuggestion(String token, String type,
                                         String company, String tel, String address, String email,
                                         String email2, String message) {

        setAddress(address);
        setCompany(company);
        setEmail(email);
        setEmail2(email2);
        setMessage(message);
        setTel(tel);
        setToken(token);
        setType(type);
    }

    public void SendValueEmailAdding(String token, String type, String company,
                                     String tel, String address, String email, String name, String tel2,
                                     String message) {

        setAddress(address);
        setCompany(company);
        setEmail(email);
        setMessage(message);
        setTel(tel);
        setToken(token);
        setType(type);
        setTel2(tel2);
        setName(name);

    }
}

//
// {
// "token":"1234",
// "type":"2",
// "company":"aaa",
// "tel":"123",
// "address":"a street 34",
// "email":"mail@mail.com",
// "email2":"mail2@mail.com",
// "message":"this is a message"
// }
