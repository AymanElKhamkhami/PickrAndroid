package dmcs.pickr.models;

import java.io.Serializable;

/**
 * Created by Ayman on 01/12/2016.
 */
public class UserDetails implements Serializable{

    public String email ="";
    public String username ="";
    public String password ="";
    public int reputation = 0;
    public Preferences preferences = new Preferences();
    public String carModel ="";
    public String firstName ="";
    public String surname ="";
    //public DateTime birth;
    public String gender ="";
    //public DateTime memberSince;
    public String mobile ="";
    public String picture ="http://pickrwebservice.somee.com/images/profile/default-m.jpg";
    public String address ="";
    public String mode = "";

    public UserDetails(String email, String username, String password, int reputation, Preferences preferences, String carModel, String firstName, String surname, String gender, String mobile, String picture, String address, String mode) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.reputation = reputation;
        this.preferences = preferences;
        this.carModel = carModel;
        this.firstName = firstName;
        this.surname = surname;
        this.gender = gender;
        this.mobile = mobile;
        this.picture = picture;
        this.address = address;
        this.mode = mode;
    }

    public UserDetails() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
