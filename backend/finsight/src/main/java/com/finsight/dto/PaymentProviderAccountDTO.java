package com.finsight.dto;

import com.finsight.model.PaymentProviderAccount;
import com.finsight.model.enums.PaymentProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** Shows which provider is connected and since when. The API key (encrypted or not) is never included. */
@Getter
@AllArgsConstructor
public class PaymentProviderAccountDTO {
    private Long id;
    private PaymentProvider provider;
    private LocalDateTime connectedAt;

    public static PaymentProviderAccountDTO from(PaymentProviderAccount a) {
        return new PaymentProviderAccountDTO(a.getId(), a.getProvider(), a.getConnectedAt());
    }
}
