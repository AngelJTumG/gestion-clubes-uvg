package com.gestionclubes.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionclubes.dtos.RegistroUsuarioDto;
import com.gestionclubes.models.Rol;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroUsuarioDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (!dto.getPassword().equals(dto.getConfirmarPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(Rol.USUARIO);
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .disabled(!usuario.isActivo())
                .build();
    }
}

