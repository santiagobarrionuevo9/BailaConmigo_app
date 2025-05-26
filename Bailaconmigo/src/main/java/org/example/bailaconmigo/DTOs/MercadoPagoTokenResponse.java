package org.example.bailaconmigo.DTOs;

import lombok.Data;

@Data
public class MercadoPagoTokenResponse {
    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String scope;
    private Long user_id;
}
