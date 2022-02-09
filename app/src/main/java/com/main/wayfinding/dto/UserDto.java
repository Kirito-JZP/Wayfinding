package com.main.wayfinding.dto;

/**
 * Define the entity class used for storing user info
 *
 * @author Gang
 * @author Last Modified By Gang
 * @version Revision: 0
 * Date: 2022/02/09 1:42
 */
public class UserDto {
    private String firstName;
    private String surname;
    private String country;
    private String phoneNumber;

    public UserDto() {
    }

    public UserDto(String firstName, String surname, String country, String phoneNumber) {
        this.firstName = firstName;
        this.surname = surname;
        this.country = country;
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSurname() {
        return surname;
    }

    public String getCountry() {
        return country;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
