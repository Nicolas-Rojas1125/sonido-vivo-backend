package cl.sonidovivo.tienda.controller;

import org.springframework.security.oauth2.jwt.Jwt;

// Lee datos del JWT ya verificado por Spring Security (firma, expiración y emisor válidos).
final class Tokens {

    private Tokens() {
    }

    // Claim "uid": id del usuario en ms-usuarios.
    static Long idUsuario(Jwt jwt) {
        Object uid = jwt.getClaims().get("uid");
        return uid instanceof Number numero ? numero.longValue() : Long.valueOf(String.valueOf(uid));
    }

    // Claim "rol": ADMINISTRADOR, VENDEDOR o CLIENTE.
    static boolean esPersonal(Jwt jwt) {
        String rol = jwt.getClaimAsString("rol");
        return "ADMINISTRADOR".equals(rol) || "VENDEDOR".equals(rol);
    }
}
