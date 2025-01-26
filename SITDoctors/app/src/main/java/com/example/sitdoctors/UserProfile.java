package com.example.sitdoctors;

public class UserProfile {
    private String name;
    private String age;
    private String phone;
    private String address;
    private String email;
    private String role;

    // Default constructor required for calls to DataSnapshot.getValue(UserProfile.class)
    public UserProfile() {
    }

    public UserProfile(String name, String age, String phone, String address, String email, String role) {
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
