/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minukucore.user;

/**
 * User is a well defined entity which will have a first name,
 * a last name, email and a set of devices that belong to the user
 * and have the Minuku app installed.
 *
 * Created by Neeraj Kumar on 7/12/2016.
 */
public class User {
    String firstName;
    String lastName;
    String email;

    public User() {
        this.firstName = "";
        this.lastName = "";
        this.email = "";
    }

    public User(String aFirstName, String aLastName, String aEmail) {
        this.firstName = aFirstName;
        this.lastName = aLastName;
        this.email = aEmail;
    }

    /**
     *
     * @return The first name of the user.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     *
     * @return The last name of the user.
     */
    public String getLastName() {
        return lastName;
    }


    /**
     *
     * @return User's email.
     */
    public String getEmail() {
        return email;
    }


    @Override
    public String toString() {
        return this.firstName + ";" + this.lastName + ";" + this.email;
    }
}
