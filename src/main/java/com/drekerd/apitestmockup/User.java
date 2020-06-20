package com.drekerd.apitestmockup;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {

    private long id;
    private String name;
    private String nif;
    private String role;
}
