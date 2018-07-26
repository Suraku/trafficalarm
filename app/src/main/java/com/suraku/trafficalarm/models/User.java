package com.suraku.trafficalarm.models;

import com.suraku.dev.sqlite.SQLiteModelMetadata;

import java.util.Calendar;
import java.util.UUID;

/**
 * Model
 */

public class User
{
    public User()
    {
        this.userPK = UUID.randomUUID();
        this.username = this.password = this.displayName = "";
        this.createdDate = Calendar.getInstance();
    }

    @SQLiteModelMetadata
    private UUID userPK;

    @SQLiteModelMetadata
    private String username;

    @SQLiteModelMetadata
    private String password;

    @SQLiteModelMetadata
    private String displayName;

    @SQLiteModelMetadata
    private Calendar createdDate;


    // ~~ Getters & Setters ~~ \\

    public UUID getUserPK() { return userPK; }
    public void setUserPK(UUID val) { this.userPK = val; }

    public String getUsername1() { return username; }
    public void setUsername(String val) { this.username = val; }

    public String getPassword() { return password; }
    public void setPassword(String val) { throw new RuntimeException("Missing password hashing"); }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String val) { this.displayName = val; }

    public Calendar getCreatedDate() { return createdDate; }
    public void setCreatedDate(Calendar val) { this.createdDate = val; }

}
