package com.example.tp4spring.service;

import com.example.tp4spring.dto.ClienteDTO;
import com.example.tp4spring.dto.ClienteRequestDTO;
import com.example.tp4spring.entities.Cliente;
import com.example.tp4spring.entities.Contacto;
import com.example.tp4spring.entities.Domicilio;
import com.example.tp4spring.exception.DatosInvalidosException;
import com.example.tp4spring.exception.RecursoDadoDeBajaException;
import com.example.tp4spring.exception.RecursoNoEncontradoException;
import com.example.tp4spring.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service @RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    private ClienteDTO crearClienteDTO (Cliente cliente) {
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setId(cliente.getId());
        clienteDTO.setCuitCuil(cliente.getCuitCuil());
        clienteDTO.setDenominacion(cliente.getDenominacion());
        clienteDTO.setEmailContacto(cliente.getContacto().getEmail());
        clienteDTO.setTelefonoContacto(cliente.getContacto().getTelefono());
        clienteDTO.setCelularContacto(cliente.getContacto().getCelular());
        clienteDTO.setNombreCalleDomicilio(cliente.getDomicilio().getNombreCalle());
        clienteDTO.setNumeroCalleDomicilio(cliente.getDomicilio().getNumeroCalle());
        clienteDTO.setActivo(cliente.getFechaBaja() == null);
        return clienteDTO;
    }

    public ClienteDTO obtener (Long id) {
        Cliente clienteEncontrado = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
        return this.crearClienteDTO(clienteEncontrado);
    }

    public Page<ClienteDTO> buscar (String texto, boolean incluirBajas, String orden, int pagina, int tamanio) {
        if (texto == null) {
            texto = "";
        }
        Page<Cliente> paginaClientes = switch (orden == null ? "" : orden) {
            // Los especiales: la consulta ya trae su ORDER BY, así que el Pageable va SIN orden
            case "mas-compras" -> clienteRepository.buscarPorCantidadDeCompras(texto, incluirBajas, PageRequest.of(pagina, tamanio));
            case "compra-reciente" -> clienteRepository.buscarPorCompraMasReciente(texto, incluirBajas, PageRequest.of(pagina, tamanio));
            // Los simples: el orden va en el Pageable
            default -> clienteRepository.buscar(texto, incluirBajas, PageRequest.of(pagina, tamanio, ordenSimple(orden)));
        };
        return paginaClientes.map(this::crearClienteDTO);
    }

    // Traduce la clave que manda el front. Si llega cualquier otra cosa, ordena por nombre
    private Sort ordenSimple (String orden) {
        Sort porNombre = Sort.by("denominacion");
        if (orden == null) {
            return porNombre;
        }
        return switch (orden) {
            case "nombre-desc" -> porNombre.descending();
            case "nuevos" -> Sort.by("fechaAlta").descending().and(porNombre);
            case "antiguos" -> Sort.by("fechaAlta").and(porNombre);
            default -> porNombre;
        };
    }

    @Transactional
    public ClienteDTO crear (ClienteRequestDTO cDatosIngresados) {
        if (cDatosIngresados.getCuitCuil() == null || cDatosIngresados.getCuitCuil().isBlank()) {
            throw new DatosInvalidosException("El CUIT/CUIL es obligatorio");
        }
        if (cDatosIngresados.getDenominacion() == null || cDatosIngresados.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }

        // Lo escriban como lo escriban, a partir de acá se usa SIEMPRE el normalizado
        String cuit = normalizarCuit(cDatosIngresados.getCuitCuil());

        Optional<Cliente> existente = clienteRepository.findByCuitCuil(cuit);
        if (existente.isPresent()) {
            Cliente clienteExistente = existente.get();
            if (clienteExistente.getFechaBaja() != null) {
                throw new RecursoDadoDeBajaException(
                        "El cliente con CUIT/CUIL " + cuit + " está dado de baja",
                        clienteExistente.getId());
            }
            throw new DatosInvalidosException("Ya existe un cliente con CUIT/CUIL " + cuit);
        }

        Contacto contacto = new Contacto();
        contacto.setEmail(cDatosIngresados.getEmailContacto());
        contacto.setTelefono(cDatosIngresados.getTelefonoContacto());
        contacto.setCelular(cDatosIngresados.getCelularContacto());

        Domicilio domicilio = new Domicilio();
        domicilio.setNombreCalle(cDatosIngresados.getNombreCalleDomicilio());
        domicilio.setNumeroCalle(cDatosIngresados.getNumeroCalleDomicilio());

        Cliente cliente = new Cliente();
        cliente.setCuitCuil(cuit);   // ← el normalizado, no el que llegó
        cliente.setDenominacion(cDatosIngresados.getDenominacion());
        cliente.setContacto(contacto);
        cliente.setDomicilio(domicilio);

        auditoriaService.registrarAlta(cliente);

        Cliente guardado = clienteRepository.save(cliente);
        return crearClienteDTO(guardado);
    }

    @Transactional
    public ClienteDTO actualizar (Long id, ClienteRequestDTO cDatosIngresados) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));

        if (cliente.getFechaBaja() != null) {
            throw new DatosInvalidosException("No se puede modificar un cliente dado de baja");
        }
        if (cDatosIngresados.getDenominacion() == null || cDatosIngresados.getDenominacion().isBlank()) {
            throw new DatosInvalidosException("La denominación es obligatoria");
        }
        // El CUIT es la clave de negocio: no se modifica (se comparan los dos normalizados)
        if (cDatosIngresados.getCuitCuil() != null
                && !normalizarCuit(cDatosIngresados.getCuitCuil()).equals(normalizarCuit(cliente.getCuitCuil()))) {
            throw new DatosInvalidosException("El CUIT/CUIL de un cliente no se puede modificar");
        }

        cliente.setDenominacion(cDatosIngresados.getDenominacion());
        cliente.getContacto().setEmail(cDatosIngresados.getEmailContacto());
        cliente.getContacto().setTelefono(cDatosIngresados.getTelefonoContacto());
        cliente.getContacto().setCelular(cDatosIngresados.getCelularContacto());
        cliente.getDomicilio().setNombreCalle(cDatosIngresados.getNombreCalleDomicilio());
        cliente.getDomicilio().setNumeroCalle(cDatosIngresados.getNumeroCalleDomicilio());

        auditoriaService.registrarModificacion(cliente);

        return crearClienteDTO(cliente);   // sin save: vino de findById y se guarda solo
    }

    @Transactional
    public void darDeBaja (Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));

        if (cliente.getFechaBaja() != null) {
            throw new DatosInvalidosException("El cliente ya estaba dado de baja");
        }

        auditoriaService.registrarBaja(cliente);
    }

    @Transactional
    public ClienteDTO reactivar (Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));

        if (cliente.getFechaBaja() == null) {
            throw new DatosInvalidosException("El cliente ya está activo");
        }

        auditoriaService.quitarBaja(cliente);

        return crearClienteDTO(cliente);
    }

    // Lo escriban como lo escriban, el CUIT se guarda siempre como XX-XXXXXXXX-X
    private String normalizarCuit (String cuit) {
        String digitos = cuit.replaceAll("\\D", "");   // \D = cualquier cosa que NO sea un número
        if (digitos.length() != 11) {
            throw new DatosInvalidosException("El CUIT/CUIL tiene que tener 11 números");
        }
        return digitos.substring(0, 2) + "-" + digitos.substring(2, 10) + "-" + digitos.substring(10);
    }
}