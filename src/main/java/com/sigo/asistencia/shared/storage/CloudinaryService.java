package com.sigo.asistencia.shared.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /*
     * =========================================================
     * SUBIR IMAGEN DE ASISTENCIA
     * =========================================================
     */
    public Map<String, Object> subirImagen(
            MultipartFile archivo
    ) throws IOException {

        return subirImagen(
                archivo,
                "sigo/asistencia"
        );
    }

    /*
     * =========================================================
     * SUBIR IMAGEN A UNA CARPETA ESPECÍFICA
     * =========================================================
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> subirImagen(
            MultipartFile archivo,
            String folder
    ) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una imagen"
            );
        }

        if (archivo.getContentType() == null
                || !archivo
                .getContentType()
                .startsWith("image/")) {

            throw new IllegalArgumentException(
                    "El archivo debe ser una imagen"
            );
        }

        String carpeta =
                folder == null || folder.isBlank()
                        ? "sigo"
                        : folder.trim();

        try {

            Map<?, ?> resultado =
                    cloudinary
                            .uploader()
                            .upload(
                                    archivo.getBytes(),
                                    ObjectUtils.asMap(
                                            "folder",
                                            carpeta,

                                            "resource_type",
                                            "image"
                                    )
                            );

            return (Map<String, Object>) resultado;

        } catch (Exception e) {

            System.err.println(
                    "Error al subir imagen a Cloudinary: "
                            + e.getMessage()
            );

            throw e;
        }
    }

    /*
     * =========================================================
     * ELIMINAR IMAGEN DE CLOUDINARY
     * =========================================================
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> eliminarImagen(
            String publicId
    ) throws IOException {

        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException(
                    "El public_id de la imagen es obligatorio"
            );
        }

        try {

            return (Map<String, Object>)
                    cloudinary
                            .uploader()
                            .destroy(
                                    publicId,
                                    ObjectUtils.asMap(
                                            "resource_type",
                                            "image",
                                            "invalidate",
                                            true
                                    )
                            );

        } catch (Exception e) {

            System.err.println(
                    "Error al eliminar imagen de Cloudinary: "
                            + e.getMessage()
            );

            throw e;
        }
    }
}