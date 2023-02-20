package org.example;


import org.springframework.data.annotation.Id;

public class People {

    @Id
    private String id;

    private  String username;

    private  String password;

    public People(String id,String username, String password) {
        this.id  = id;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
