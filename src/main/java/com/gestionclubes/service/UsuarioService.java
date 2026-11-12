package com.gestionclubes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gestionclubes.dtos.RegistroUsuarioDto;
import com.gestionclubes.models.Rol;
import com.gestionclubes.models.Usuario;
import com.gestionclubes.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EstudianteService estudianteService;
    private final Path directorioPerfiles;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          EstudianteService estudianteService,
                          @Value("${app.profile-upload-dir:uploads/perfiles}") String directorioPerfiles) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.estudianteService = estudianteService;
        this.directorioPerfiles = Paths.get(directorioPerfiles).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public Usuario actualizarPerfil(String email, String nombre, String descripcion, MultipartFile foto)
            throws IOException {
        Usuario usuario = buscarPorEmail(email);
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        usuario.setNombre(nombre.trim());
        usuario.setDescripcion(descripcion == null || descripcion.trim().isEmpty()
                ? null : descripcion.trim());
        if (foto != null && !foto.isEmpty()) {
            validarFoto(foto);
            Files.createDirectories(directorioPerfiles);
            String nombreArchivo = UUID.randomUUID() + "."
                    + extension(foto.getOriginalFilename());
            Path destino = directorioPerfiles.resolve(nombreArchivo).normalize();
            Files.copy(foto.getInputStream(), destino);
            if (usuario.getFotoPerfil() != null && usuario.getFotoPerfil().startsWith("/uploads/perfiles/")) {
                Files.deleteIfExists(directorioPerfiles.resolve(
                        usuario.getFotoPerfil().substring("/uploads/perfiles/".length())).normalize());
            }
            usuario.setFotoPerfil("/uploads/perfiles/" + nombreArchivo);
        }
        return usuarioRepository.save(usuario);
    }

    private void validarFoto(MultipartFile foto) {
        String tipo = foto.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new IllegalArgumentException("La foto de perfil debe ser una imagen");
        }
        if (foto.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La foto de perfil no puede superar 5 MB");
        }
    }

    private String extension(String nombre) {
        if (nombre == null || !nombre.contains(".")) {
            return "jpg";
        }
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
        return extension.matches("png|jpg|jpeg|gif|webp") ? extension : "jpg";
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
        Usuario guardado = usuarioRepository.save(usuario);
        estudianteService.registrar(guardado, dto.getCarne());
        return guardado;
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

