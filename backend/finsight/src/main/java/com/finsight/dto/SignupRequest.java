package com.finsight.dto;



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
    private String phoneNumber;



}
