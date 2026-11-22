package com.finsight.dto;

/** What the frontend sees about the logged-in user. Never includes the password hash. */
public record ProfileResponse(String fullName, String email, String phoneNumber) {}