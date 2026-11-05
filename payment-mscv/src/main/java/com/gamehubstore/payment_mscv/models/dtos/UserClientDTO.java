package com.gamehubstore.payment_mscv.models.dtos;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class UserClientDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
}
