package com.gestionclubes.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gestionclubes.models.Club;
import com.gestionclubes.models.Fotografia;
import com.gestionclubes.repository.FotografiaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class FotografiaService {
    private static final String RUTA_PUBLICA = "/uploads/fotografias/";

    private final FotografiaRepository fotografiaRepository;
    private final ClubService clubService;
    private final Path directorioFotografias;

    public FotografiaService(FotografiaRepository fotografiaRepository, ClubService clubService,
                             @Value("${app.upload-dir:uploads/fotografias}") String directorioFotografias) {
        this.fotografiaRepository = fotografiaRepository;
        this.clubService = clubService;
        this.directorioFotografias = Paths.get(directorioFotografias).toAbsolutePath().normalize();
    }

    public List<Fotografia> listarPorClub(Long clubId) {
        return fotografiaRepository.findByClub_IdOrderByIdAsc(clubId);
    }

    public Fotografia buscar(Long id) {
        return fotografiaRepository.findOneById(id)
                .orElseThrow(() -> new EntityNotFoundException("No existe la fotografía solicitada"));
    }

    public Fotografia guardar(Long clubId, Fotografia fotografia, MultipartFile archivo) throws IOException {
        Club club = clubService.buscarPorId(clubId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el club solicitado"));
        boolean tieneArchivo = archivo != null && !archivo.isEmpty();
        boolean tieneUrl = fotografia.getUrlImagen() != null && !fotografia.getUrlImagen().isBlank();
        if (!tieneArchivo && !tieneUrl) {
            throw new IllegalArgumentException("Selecciona una fotografía desde tus archivos");
        }
        if (fotografia.getDescripcion() == null || fotografia.getDescripcion().isBlank()) {
            fotografia.setDescripcion("Fotografía del club");
        }
        if (tieneArchivo) {
            fotografia.setUrlImagen(guardarArchivo(archivo));
        }
        fotografia.setClub(club);
        return fotografiaRepository.save(fotografia);
    }

    public void eliminar(Long id) throws IOException {
        Fotografia fotografia = buscar(id);
        fotografiaRepository.delete(fotografia);
        eliminarArchivoLocal(fotografia.getUrlImagen());
    }

    private String guardarArchivo(MultipartFile archivo) throws IOException {
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("El archivo seleccionado debe ser una imagen");
        }
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        Files.createDirectories(directorioFotografias);
        String nombre = UUID.randomUUID() + extensionImagen(archivo.getOriginalFilename(), tipo);
        Path destino = directorioFotografias.resolve(nombre).normalize();
        if (!directorioFotografias.equals(destino.getParent())) {
            throw new IOException("Ruta de archivo no válida");
        }
        try (var entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino);
        }
        return RUTA_PUBLICA + nombre;
    }

    private String extensionImagen(String nombreOriginal, String tipo) {
        if (nombreOriginal != null) {
            int punto = nombreOriginal.lastIndexOf('.');
            if (punto >= 0 && punto < nombreOriginal.length() - 1) {
                String extension = nombreOriginal.substring(punto).toLowerCase(Locale.ROOT);
                if (extension.matches("\\.(png|jpe?g|gif|webp)")) {
                    return extension;
                }
            }
        }
        return switch (tipo.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private void eliminarArchivoLocal(String url) throws IOException {
        if (url == null || !url.startsWith(RUTA_PUBLICA)) {
            return;
        }
        Path archivo = directorioFotografias.resolve(url.substring(RUTA_PUBLICA.length())).normalize();
        if (directorioFotografias.equals(archivo.getParent())) {
            Files.deleteIfExists(archivo);
        }
    }
}
