package com.gamehubstore.order_mscv.models.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ValidarCarritoRequest {
    @NotEmpty(message = "El carrito debe contener al menos un producto")
    @Valid
    private List<ItemCarritoDTO> items;

    private String promotionCode;
}
