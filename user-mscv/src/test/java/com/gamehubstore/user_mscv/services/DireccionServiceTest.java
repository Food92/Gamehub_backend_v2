package com.gamehubstore.user_mscv.services;


import com.gamehubstore.user_mscv.models.Direccion;
import com.gamehubstore.user_mscv.models.dtos.DireccionDTO;
import com.gamehubstore.user_mscv.repositories.DireccionRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DireccionServiceTest {

    @Mock
    private DireccionRepository direccionRepository;

    @InjectMocks
    private DireccionServiceImpl direccionService;

    private Faker faker;
    private Direccion direccionEntidad;
    private DireccionDTO direccionDTO;

    @BeforeEach
    public void setUp() {
        this.faker = new Faker();

        // 1. Generar datos de localización aleatorios con Faker
        Long userIdFake = faker.number().randomNumber();
        String calleFake = faker.address().streetName();
        String numeroFake = faker.address().buildingNumber();
        String comunaFake = faker.address().city(); // Faker no tiene comunas específicas, usamos city()
        String ciudadFake = faker.address().state();

        // 2. Poblar el DTO de entrada con la data dinámicos de Faker
        this.direccionDTO = new DireccionDTO();
        this.direccionDTO.setUserId(userIdFake);
        this.direccionDTO.setCalle(calleFake);
        this.direccionDTO.setNumero(numeroFake);
        this.direccionDTO.setComuna(comunaFake);
        this.direccionDTO.setCiudad(ciudadFake);

        // 3. Poblar la Entidad simulada de la BD con los mismos datos
        this.direccionEntidad = new Direccion();
        this.direccionEntidad.setDireccionId(faker.number().randomNumber());
        this.direccionEntidad.setUserId(userIdFake);
        this.direccionEntidad.setCalle(calleFake);
        this.direccionEntidad.setNumero(numeroFake);
        this.direccionEntidad.setComuna(comunaFake);
        this.direccionEntidad.setCiudad(ciudadFake);
    }

    @Test
    @DisplayName("Debe guardar una dirección de Faker correctamente y retornar su DTO mapeado")
    public void shouldSaveDireccionSuccessfully() {
        // Arrange
        when(direccionRepository.save(any(Direccion.class))).thenReturn(this.direccionEntidad);

        // Act
        DireccionDTO resultado = direccionService.save(this.direccionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(this.direccionEntidad.getDireccionId(), resultado.getDireccionId());
        assertEquals(this.direccionEntidad.getUserId(), resultado.getUserId());
        assertEquals(this.direccionEntidad.getCalle(), resultado.getCalle());
        assertEquals(this.direccionEntidad.getComuna(), resultado.getComuna());
        verify(direccionRepository, times(1)).save(any(Direccion.class));
    }

    @Test
    @DisplayName("Debe buscar todas las direcciones asociadas a un ID de usuario de Faker")
    public void shouldFindDireccionesByUserId() {
        // Arrange
        Long userIdBuscado = this.direccionEntidad.getUserId();
        List<Direccion> listaSimulada = new ArrayList<>();
        listaSimulada.add(this.direccionEntidad);

        when(direccionRepository.findByUserId(userIdBuscado)).thenReturn(listaSimulada);

        // Act
        List<DireccionDTO> resultado = direccionService.findByUserId(userIdBuscado);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(this.direccionEntidad.getCalle(), resultado.get(0).getCalle());
        assertEquals(this.direccionEntidad.getNumero(), resultado.get(0).getNumero());
        verify(direccionRepository, times(1)).findByUserId(userIdBuscado);
    }

    @Test
    @DisplayName("Debe eliminar una dirección si existe el ID generado por Faker")
    public void shouldDeleteDireccionSuccessfully() {
        // Arrange
        Long idOriginal = this.direccionEntidad.getDireccionId();
        when(direccionRepository.existsById(idOriginal)).thenReturn(true);
        doNothing().when(direccionRepository).deleteById(idOriginal);

        // Act
        direccionService.delete(idOriginal);

        // Assert
        verify(direccionRepository, times(1)).deleteById(idOriginal);
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar eliminar un ID de Faker que no existe")
    public void shouldThrowExceptionWhenDireccionNotFoundOnDelete() {
        // Arrange
        Long idInexistente = faker.number().randomNumber();
        when(direccionRepository.existsById(idInexistente)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            direccionService.delete(idInexistente);
        });

        assertEquals("Dirección no encontrada", exception.getMessage());
        verify(direccionRepository, never()).deleteById(anyLong());
    }
}