package com.app.hourbook.security;

public class AuthenticatedPrincipal {

    private Integer id;
    private String identifier;
    private String entity;
    private String role;

    public AuthenticatedPrincipal(Integer id,
                                  String identifier,
                                  String entity,
                                  String role) {
        this.id = id;
        this.identifier = identifier;
        this.entity = entity;
        this.role = role;
    }

    public Integer getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getEntity() {
        return entity;
    }

    public String getRole() {
        return role;
    }
}