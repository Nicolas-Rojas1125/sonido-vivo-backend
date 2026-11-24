package cl.sonidovivo.usuarios.dto;

// Respuesta de login, registro y refresh.
// El refresh token NO va aquí: viaja en una cookie httpOnly que JavaScript no puede leer.
public record SesionResponse(String accessToken, String tipo, long expiraEnSegundos, UsuarioResponse usuario) {
}
