package com.finsight.dto;

// Data the client sends to register

import lombok.*;
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SignupRequest {

    private String fullName;
    private String email;
    private String password;



}
