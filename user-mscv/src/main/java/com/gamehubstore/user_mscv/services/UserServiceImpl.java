package com.gamehubstore.user_mscv.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gamehubstore.user_mscv.exceptions.UserException;
import com.gamehubstore.user_mscv.models.User;
import com.gamehubstore.user_mscv.models.dtos.UserDTO;
import com.gamehubstore.user_mscv.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private FirestoreSyncService firestoreSyncService;

    @Override
    @Transactional
    public UserDTO save(UserDTO userDTO) {
        log.info("operation={} started", "save");
        // Regla de Negocio: No duplicar Email
        if (userRepository.existsByCorreo(userDTO.getCorreo())) {
            throw new UserException("El correo electrónico ya está registrado");
        }

        User newUser = new User();
        newUser.setRut(userDTO.getRut());
        newUser.setNombreCompleto(userDTO.getNombreCompleto());
        newUser.setApellidoCompleto(userDTO.getApellidoCompleto());
        newUser.setCorreo(userDTO.getCorreo());
        newUser.setTelefono(userDTO.getTelefono());

        // Si el DTO no envía estado, por defecto el modelo nace en true
        if (userDTO.getEstado() != null) {
            newUser.setEstado(userDTO.getEstado());
        }

        User savedUser = userRepository.save(newUser);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarUsuario(savedUser);

        // Mapear de Entidad de regreso al DTO de salida
        UserDTO dto = new UserDTO();
        dto.setUserId(savedUser.getUserId());
        dto.setRut(savedUser.getRut());
        dto.setNombreCompleto(savedUser.getNombreCompleto());
        dto.setApellidoCompleto(savedUser.getApellidoCompleto());
        dto.setCorreo(savedUser.getCorreo());
        dto.setTelefono(savedUser.getTelefono());
        dto.setEstado(savedUser.getEstado());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserException("Usuario no encontrado con el ID: " + id));
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
        log.info("operation={} started", "update");
        User updateUser = userRepository.findById(id).orElseThrow(
                () -> new UserException("Usuario no encontrado para actualizar"));

        // Validar email único (excepto si el correo pertenece al mismo usuario que estamos editando)
        if (!updateUser.getCorreo().equalsIgnoreCase(user.getCorreo()) &&
                userRepository.existsByCorreo(user.getCorreo())) {
            throw new UserException("El correo electrónico ya se encuentra registrado por otro usuario");
        }

        // Actualizar los campos permitidos del caso semestral
        updateUser.setRut(user.getRut());
        updateUser.setNombreCompleto(user.getNombreCompleto());
        updateUser.setApellidoCompleto(user.getApellidoCompleto());
        updateUser.setCorreo(user.getCorreo());
        updateUser.setTelefono(user.getTelefono());

        if (user.getEstado() != null) {
            updateUser.setEstado(user.getEstado());
        }

        User saved = userRepository.save(updateUser);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarUsuario(saved);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("operation={} started", "delete");
        // CORRECCIÓN CRÍTICA: Si NO existe (!), se debe lanzar el error.
        if (!userRepository.existsById(id)) {
            throw new UserException("No se puede eliminar. Usuario no encontrado");
        }
        userRepository.deleteById(id);
        if (firestoreSyncService != null) firestoreSyncService.eliminarUsuario(id);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        log.info("operation={} started", "desactivar");
        User findUser = userRepository.findById(id).orElseThrow(
                () -> new UserException("No se puede desactivar. Usuario no encontrado"));

        findUser.setEstado(false); // Baja lógica requerida por el caso semestral
        User saved = userRepository.save(findUser);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarUsuario(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByEstadoTrue() {
        return userRepository.findByEstadoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByEstadoFalse() {
        return userRepository.findByEstadoFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public User findByCorreo(String correo) {
        return userRepository.findByCorreo(correo)
                .orElseThrow(() -> new UserException("Usuario no encontrado con el correo: " + correo));
    }

    @Override
    @Transactional
    public User updateByCorreo(String correo, UserDTO dto) {
        log.info("operation={} started", "updateByCorreo");
        User existing = findByCorreo(correo);
        if (dto.getNombreCompleto() != null && !dto.getNombreCompleto().isBlank()) {
            existing.setNombreCompleto(dto.getNombreCompleto());
        }
        if (dto.getApellidoCompleto() != null && !dto.getApellidoCompleto().isBlank()) {
            existing.setApellidoCompleto(dto.getApellidoCompleto());
        }
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            existing.setTelefono(dto.getTelefono());
        }
        User saved = userRepository.save(existing);
        if (firestoreSyncService != null) firestoreSyncService.sincronizarUsuario(saved);
        return saved;
    }
}