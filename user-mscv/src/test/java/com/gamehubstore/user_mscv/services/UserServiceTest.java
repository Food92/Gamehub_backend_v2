package com.gamehubstore.user_mscv.services;


import com.gamehubstore.user_mscv.exceptions.UserException;
import com.gamehubstore.user_mscv.models.User;
import com.gamehubstore.user_mscv.models.dtos.UserDTO;
import com.gamehubstore.user_mscv.repositories.UserRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Faker faker;
    private User userEntidad;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        this.faker = new Faker();

        // 1. Datos generados aleatoriamente con Faker
        String rutFake = faker.idNumber().valid();
        String nombreFake = faker.name().firstName();
        String apellidoFake = faker.name().lastName();
        String correoFake = faker.internet().emailAddress();
        String telefonoFake = faker.phoneNumber().phoneNumber();

        // 2. Poblamos el DTO de entrada con la data de Faker
        this.userDTO = new UserDTO();
        this.userDTO.setRut(rutFake);
        this.userDTO.setNombreCompleto(nombreFake);
        this.userDTO.setApellidoCompleto(apellidoFake);
        this.userDTO.setCorreo(correoFake);
        this.userDTO.setTelefono(telefonoFake);
        this.userDTO.setEstado(true);

        // 3. Poblamos la Entidad simulada de la BD con los mismos datos dinámicos
        this.userEntidad = new User();
        this.userEntidad.setUserId(faker.number().randomNumber());
        this.userEntidad.setRut(rutFake);
        this.userEntidad.setNombreCompleto(nombreFake);
        this.userEntidad.setApellidoCompleto(apellidoFake);
        this.userEntidad.setCorreo(correoFake);
        this.userEntidad.setTelefono(telefonoFake);
        this.userEntidad.setEstado(true);
    }

    @Test
    @DisplayName("Debe guardar un usuario generado con Faker exitosamente y retornar su DTO")
    public void shouldSaveUserSuccessfully() {
        // Arrange
        when(userRepository.existsByCorreo(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(this.userEntidad);

        // Act
        UserDTO resultado = userService.save(this.userDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(this.userEntidad.getUserId(), resultado.getUserId());
        assertEquals(this.userEntidad.getCorreo(), resultado.getCorreo());
        assertEquals(this.userEntidad.getNombreCompleto(), resultado.getNombreCompleto());
        assertTrue(resultado.getEstado());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el correo de Faker ya existe al intentar guardar")
    public void shouldThrowExceptionWhenEmailExistsOnSave() {
        // Arrange
        when(userRepository.existsByCorreo(this.userDTO.getCorreo())).thenReturn(true);

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            userService.save(this.userDTO);
        });

        assertEquals("El correo electrónico ya está registrado", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debe buscar un usuario por ID aleatorio exitosamente")
    public void shouldFindUserByIdSuccessfully() {
        // Arrange
        Long idBuscado = this.userEntidad.getUserId();
        when(userRepository.findById(idBuscado)).thenReturn(Optional.of(this.userEntidad));

        // Act
        User resultado = userService.findById(idBuscado);

        // Assert
        assertNotNull(resultado);
        assertEquals(idBuscado, resultado.getUserId());
        assertEquals(this.userEntidad.getNombreCompleto(), resultado.getNombreCompleto());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el ID aleatorio no existe en findById")
    public void shouldThrowExceptionWhenUserIdNotFound() {
        // Arrange
        Long idInexistente = faker.number().randomNumber();
        when(userRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        UserException exception = assertThrows(UserException.class, () -> {
            userService.findById(idInexistente);
        });

        assertEquals("Usuario no encontrado con el ID: " + idInexistente, exception.getMessage());
    }

    @Test
    @DisplayName("Debe actualizar un usuario con nuevos datos de Faker")
    public void shouldUpdateUserSuccessfully() {
        // Arrange
        Long idOriginal = this.userEntidad.getUserId();
        when(userRepository.findById(idOriginal)).thenReturn(Optional.of(this.userEntidad));

        // Generamos datos nuevos aleatorios para simular la edición
        User nuevosDatos = new User();
        nuevosDatos.setRut(this.userEntidad.getRut());
        nuevosDatos.setNombreCompleto(faker.name().firstName()); // Nombre nuevo
        nuevosDatos.setApellidoCompleto(this.userEntidad.getApellidoCompleto());
        nuevosDatos.setCorreo(faker.internet().emailAddress()); // Correo nuevo
        nuevosDatos.setTelefono(faker.phoneNumber().phoneNumber());
        nuevosDatos.setEstado(true);

        when(userRepository.existsByCorreo(nuevosDatos.getCorreo())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(nuevosDatos);

        // Act
        User resultado = userService.update(idOriginal, nuevosDatos);

        // Assert
        assertNotNull(resultado);
        assertEquals(nuevosDatos.getNombreCompleto(), resultado.getNombreCompleto());
        assertEquals(nuevosDatos.getCorreo(), resultado.getCorreo());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Debe ejecutar baja lógica cambiando el estado a false en desactivar")
    public void shouldDesactivarUserSuccessfully() {
        // Arrange
        Long idOriginal = this.userEntidad.getUserId();
        when(userRepository.findById(idOriginal)).thenReturn(Optional.of(this.userEntidad));
        when(userRepository.save(any(User.class))).thenReturn(this.userEntidad);

        // Act
        userService.desactivar(idOriginal);

        // Assert
        verify(userRepository, times(1)).save(argThat(user -> !user.getEstado()));
    }

    @Test
    @DisplayName("Debe eliminar físicamente un usuario si existe el ID de Faker")
    public void shouldDeleteUserSuccessfully() {
        // Arrange
        Long idOriginal = this.userEntidad.getUserId();
        when(userRepository.existsById(idOriginal)).thenReturn(true);
        doNothing().when(userRepository).deleteById(idOriginal);

        // Act
        userService.delete(idOriginal);

        // Assert
        verify(userRepository, times(1)).deleteById(idOriginal);
    }
}