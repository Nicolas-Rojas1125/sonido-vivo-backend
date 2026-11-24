package cl.sonidovivo.usuarios.dto;

import cl.sonidovivo.usuarios.model.Usuario;

// Lo que la API devuelve de un usuario: SIN la contraseña.
public record UsuarioResponse(Long id, String rut, String nombre, String correo, String rol, boolean activo) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRut(), u.getNombre(), u.getCorreo(), u.getRol(), u.isActivo());
    }
}
