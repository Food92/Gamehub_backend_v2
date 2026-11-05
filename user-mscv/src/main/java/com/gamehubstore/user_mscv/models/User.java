package com.gamehubstore.user_mscv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;


    @NotBlank(message = "El campo de rut no puede ser vacio")
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El formato del run debe ser xxxxxxxx-x")
    @Column(nullable = false, unique = true)
    private String rut;

    @Column(nullable = false)
    @NotBlank(message = "El campo de nombreCompleto no puede ser vacio")
    private String nombreCompleto;

    @NotBlank(message = "El apellido no puede ser vacio")
    private String apellidoCompleto;

    @NotBlank(message = "El campo de correo no puede ser vacio")
    @Email(message = "El campo de correo tiene que tener el formato de correo")
    @Column(nullable = false, unique = true)
    private String correo;

    @Pattern(
            regexp = "^(\\+56)?\\s?9\\d{8}$",
            message = "El formato del teléfono debe ser +56 9XXXXXXXX")
    @Column(nullable = false)
    @NotBlank(message = "El campo de telefono no puede ser vacio")
    private String telefono;


    @Column(nullable = false)
    private Boolean estado=true;

    @Embedded
    private Audit audit = new Audit();

}