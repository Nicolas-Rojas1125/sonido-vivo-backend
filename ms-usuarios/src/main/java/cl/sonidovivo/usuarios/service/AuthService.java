package cl.sonidovivo.usuarios.service;

import cl.sonidovivo.usuarios.config.ErrorNegocio;
import cl.sonidovivo.usuarios.dto.LoginRequest;
import cl.sonidovivo.usuarios.dto.RegistroRequest;
import cl.sonidovivo.usuarios.dto.UsuarioRequest;
import cl.sonidovivo.usuarios.model.Usuario;
import cl.sonidovivo.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Lógica de autenticación: login, registro público y usuario actual.
@Service
public class AuthService {

    // Hash BCrypt de una clave cualquiera: se compara cuando el correo no existe para que la
    // respuesta tarde lo mismo y no se pueda adivinar qué correos están registrados.
    private static final String HASH_FALSO = "$2a$10$SLpu5fsY4MCUqy8/7CWOtuMGLyetD9Huf0XQojqIIamHpCMWOcAcW";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioService usuarioService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, UsuarioService usuarioService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.usuarioService = usuarioService;
    }

    // Verifica correo + contraseña (BCrypt) y que la cuenta esté activa.
    @Transactional(readOnly = true)
    public Usuario autenticar(LoginRequest datos) {
        Usuario usuario = usuarios.findByCorreo(datos.correo().trim().toLowerCase()).orElse(null);
        String hash = usuario != null ? usuario.getClaveHash() : HASH_FALSO;
        boolean claveCorrecta = passwordEncoder.matches(datos.clave(), hash);

        // Mismo mensaje si el correo no existe o si la clave es incorrecta (no revela cuál falló).
        if (usuario == null || !claveCorrecta) {
            throw new ErrorNegocio(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "Tu cuenta está desactivada. Contacta a la tienda.");
        }
        return usuario;
    }

    // Registro público: SIEMPRE crea un CLIENTE (nadie puede registrarse como administrador).
    @Transactional
    public Usuario registrar(RegistroRequest datos) {
        UsuarioRequest comoCliente = new UsuarioRequest(datos.rut(), datos.nombre(), datos.correo(), datos.clave(), Usuario.CLIENTE);
        return usuarioService.crearEntidad(comoCliente);
    }

    // Datos actualizados del usuario del token (por si cambió su rol o fue desactivado).
    @Transactional(readOnly = true)
    public Usuario usuarioActual(Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.UNAUTHORIZED, "La cuenta ya no existe."));
        if (!usuario.isActivo()) {
            throw new ErrorNegocio(HttpStatus.FORBIDDEN, "Tu cuenta está desactivada. Contacta a la tienda.");
        }
        return usuario;
    }
}
