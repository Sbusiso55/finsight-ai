package com.finsight.dto;

import lombok.*;

//Data the client sends to log in
@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginRequest {

    private String email;
    private String password;
}

