package com.finsight.dto;

public record UserProfileResponse(Long id,
                                  String fullName,
                                  String phoneNumber,
                                  String email
) {
}
