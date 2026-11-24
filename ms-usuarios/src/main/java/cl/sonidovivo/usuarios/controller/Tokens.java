package cl.sonidovivo.usuarios.controller;

import org.springframework.security.oauth2.jwt.Jwt;

// Lee datos del JWT ya verificado por Spring Security.
final class Tokens {

    private Tokens() {
    }

    // Claim "uid": id del usuario en la tabla usuario.
    static Long idUsuario(Jwt jwt) {
        Object uid = jwt.getClaims().get("uid");
        return uid instanceof Number numero ? numero.longValue() : Long.valueOf(String.valueOf(uid));
    }
}
