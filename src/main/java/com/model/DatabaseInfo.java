package com.model;

public class DatabaseInfo {

    private String index;
    private String dbName;
    private String url;
    private String userName;
    private String password;

    public DatabaseInfo() {
    }
    public DatabaseInfo(String index, String dbName, String url, String userName,String password) {
        this.index = index;
        this.dbName = dbName;
        this.url = url;
        this.userName = userName;
        this.password = password;
        
    }
    


    // Getters and setters for all fields
    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
