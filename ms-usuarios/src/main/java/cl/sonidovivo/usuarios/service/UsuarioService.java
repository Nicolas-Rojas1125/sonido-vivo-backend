package cl.sonidovivo.usuarios.service;

import cl.sonidovivo.usuarios.config.ErrorNegocio;
import cl.sonidovivo.usuarios.dto.UsuarioRequest;
import cl.sonidovivo.usuarios.dto.UsuarioResponse;
import cl.sonidovivo.usuarios.model.Usuario;
import cl.sonidovivo.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Gestión de usuarios (solo Administrador): listar, crear, editar, activar/desactivar y asignar rol.
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByIdAsc().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        return UsuarioResponse.desde(obtener(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest datos) {
        return UsuarioResponse.desde(crearEntidad(datos));
    }

    // Lo usan el Administrador (cualquier rol) y el registro público (rol CLIENTE).
    @Transactional
    public Usuario crearEntidad(UsuarioRequest datos) {
        if (datos.clave() == null || datos.clave().isBlank()) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Ingresa una contraseña.");
        }
        String rut = validarRut(datos.rut());
        String correo = datos.correo().trim().toLowerCase();
        if (usuarios.existsByCorreo(correo)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        if (usuarios.existsByRut(rut)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe una cuenta con ese RUT.");
        }

        Usuario nuevo = new Usuario();
        nuevo.setRut(rut);
        nuevo.setNombre(datos.nombre().trim());
        nuevo.setCorreo(correo);
        nuevo.setClaveHash(passwordEncoder.encode(datos.clave()));
        nuevo.setRol(datos.rol());
        nuevo.setActivo(true);
        return usuarios.save(nuevo);
    }

    // Si la clave viene vacía se mantiene la actual. El administrador no puede cambiar su propio rol.
    @Transactional
    public UsuarioResponse actualizar(Long idAdministrador, Long id, UsuarioRequest datos) {
        Usuario usuario = obtener(id);
        if (id.equals(idAdministrador) && !usuario.getRol().equals(datos.rol())) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "No puedes cambiar tu propio rol (perderías el acceso al panel).");
        }
        String rut = validarRut(datos.rut());
        String correo = datos.correo().trim().toLowerCase();
        if (usuarios.existsByCorreoAndIdNot(correo, id)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe otra cuenta con ese correo.");
        }
        if (usuarios.existsByRutAndIdNot(rut, id)) {
            throw new ErrorNegocio(HttpStatus.CONFLICT, "Ya existe otra cuenta con ese RUT.");
        }

        boolean cambioRol = !usuario.getRol().equals(datos.rol());
        usuario.setRut(rut);
        usuario.setNombre(datos.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setRol(datos.rol());
        if (datos.clave() != null && !datos.clave().isBlank()) {
            usuario.setClaveHash(passwordEncoder.encode(datos.clave()));
        }
        // Si cambia el rol o la clave, se cierran sus sesiones para que el nuevo permiso se aplique de inmediato.
        if (cambioRol || (datos.clave() != null && !datos.clave().isBlank())) {
            tokenService.revocarTodos(id);
        }
        return UsuarioResponse.desde(usuario);
    }

    // Las cuentas no se eliminan (los pedidos quedan asociados a ellas): se desactivan.
    @Transactional
    public UsuarioResponse cambiarEstado(Long idAdministrador, Long id, boolean activo) {
        if (id.equals(idAdministrador) && !activo) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta.");
        }
        Usuario usuario = obtener(id);
        usuario.setActivo(activo);
        if (!activo) {
            tokenService.revocarTodos(id); // cierra todas sus sesiones
        }
        return UsuarioResponse.desde(usuario);
    }

    private Usuario obtener(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new ErrorNegocio(HttpStatus.NOT_FOUND, "El usuario no existe."));
    }

    private String validarRut(String rut) {
        if (!Rut.esValido(rut)) {
            throw new ErrorNegocio(HttpStatus.BAD_REQUEST, "Ingresa un RUT válido, por ejemplo 12.345.678-5.");
        }
        return Rut.formatear(rut);
    }
}
