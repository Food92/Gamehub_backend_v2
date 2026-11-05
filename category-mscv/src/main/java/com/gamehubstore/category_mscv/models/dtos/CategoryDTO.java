package com.gamehubstore.category_mscv.models.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class CategoryDTO {
    @Schema(example = "1")
    private Long idCategory;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(example = "Notebooks Gamer")
    private String nombreCategory;

    @NotBlank(message = "La descripción es obligatoria")
    @Schema(example = "Computadores portátiles de alto rendimiento")
    private String descripcionCategory;
}
