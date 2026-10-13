package com.finsight.dto;

import lombok.*;

@ToString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {

    private String token;
}
