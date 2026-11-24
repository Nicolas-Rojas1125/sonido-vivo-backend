package cl.sonidovivo.contactos.service;

import cl.sonidovivo.contactos.model.Contacto;
import cl.sonidovivo.contactos.repository.ContactoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Indica que esta clase contiene la lógica del negocio.
@Service
public class ContactoService {

    private final ContactoRepository repository;

    // Spring inyecta el repositorio mediante el constructor.
    public ContactoService(ContactoRepository repository) {
        this.repository = repository;
    }

    // POST: guarda un nuevo contacto.
    public Contacto guardar(Contacto contacto) {
        // Asegura que se cree un registro nuevo (si viene un id en el JSON, se ignora).
        contacto.setId(null);
        // Todo contacto nuevo comienza como pendiente.
        contacto.setEstado(Contacto.PENDIENTE);
        // Un teléfono vacío se guarda como NULL.
        if (contacto.getTelefono() != null && contacto.getTelefono().isBlank()) {
            contacto.setTelefono(null);
        }
        // La fecha se asigna desde @PrePersist del modelo.
        return repository.save(contacto);
    }

    // GET: consulta todos los contactos almacenados (los más recientes primero).
    public List<Contacto> listar() {
        return repository.findAllByOrderByFechaCreacionDesc();
    }

    // GET por id: Optional.empty() si no existe.
    public Optional<Contacto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // PUT: cambia el estado del contacto a ATENDIDO.
    public Optional<Contacto> marcarAtendido(Long id) {
        return repository.findById(id).map(contacto -> {
            contacto.setEstado(Contacto.ATENDIDO);
            return repository.save(contacto);
        });
    }

    // DELETE: elimina el contacto. Devuelve false si no existía.
    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
