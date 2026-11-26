package com.example.tp4spring.service;

import com.example.tp4spring.entities.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * CARGA DE DATOS DE PRUEBA: un supermercado.
 * Corre al arrancar la app. Si el usuario admin ya existe, no hace nada.
 * Todas las facturas nacen PENDIENTES: las emitidas se informan a ARCA de verdad, desde la pantalla.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class CargaDatos implements CommandLineRunner {

    private final EntityManager em;

    private Usuario admin;
    private ListaPrecio lista2026;
    private final List<ListaPrecioArticulo> catalogo = new ArrayList<>();
    private final Random azar = new Random(42);   // semilla fija: siempre genera lo mismo
    private int siguienteCodigoArticulo = 1;

    // Un cliente junto con la condición IVA con la que se le suele facturar
    private record ClienteSemilla(Cliente cliente, CondicionIva condicion) {}

    @Override
    @Transactional
    public void run(String... args) {
        Long yaCargado = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.usuario = 'admin'", Long.class)
                .getSingleResult();
        if (yaCargado > 0) {
            System.out.println("CargaDatos: los datos de prueba ya estaban cargados. No se hizo nada.");
            return;
        }

        Date inicio = fecha(2026, 1, 2);

        // ---------- USUARIO ----------
        admin = new Usuario();
        admin.setUsuario("admin");
        admin.setClave("admin");
        admin.setNombre("Ana");
        admin.setApellido("Administradora");
        em.persist(admin);

        // ---------- CONDICIONES IVA (códigos de ARCA) ----------
        CondicionIva responsableInscripto = condicionIva(1, "Responsable Inscripto", inicio);
        CondicionIva exento = condicionIva(4, "Exento", inicio);
        CondicionIva consumidorFinal = condicionIva(5, "Consumidor Final", inicio);
        condicionIva(6, "Monotributista", inicio);

        // ---------- MONEDAS ----------
        TipoMoneda pesos = tipoMoneda("PES", "Peso argentino", "$", inicio);
        tipoMoneda("DOL", "Dólar estadounidense", "US$", inicio);

        // ---------- PUNTOS DE VENTA ----------
        PuntoVenta casaCentral = puntoVenta(1, "Casa Central", inicio);
        PuntoVenta sucursalNorte = puntoVenta(2, "Sucursal Norte", inicio);
        PuntoVenta sucursalSur = puntoVenta(3, "Sucursal Sur", inicio);

        // ---------- RUBROS ----------
        Rubro electronica = rubro("R01", "Electrónica", inicio);
        Rubro electrodomesticos = rubro("R02", "Electrodomésticos", inicio);
        Rubro bazar = rubro("R03", "Bazar", inicio);
        Rubro limpieza = rubro("R04", "Limpieza", inicio);
        Rubro higiene = rubro("R05", "Higiene y Perfumería", inicio);
        Rubro almacen = rubro("R06", "Almacén", inicio);
        Rubro bebidas = rubro("R07", "Bebidas", inicio);
        Rubro snacks = rubro("R08", "Snacks", inicio);
        Rubro lacteos = rubro("R09", "Lácteos", inicio);
        Rubro carnes = rubro("R10", "Carnes", inicio);
        Rubro verduleria = rubro("R11", "Frutas y Verduras", inicio);

        // ---------- MARCAS ----------
        Marca dell = marca("M01", "DELL", inicio);
        Marca samsung = marca("M02", "Samsung", inicio);
        Marca logitech = marca("M03", "Logitech", inicio);
        Marca drean = marca("M04", "Drean", inicio);
        Marca philips = marca("M05", "Philips", inicio);
        Marca bgh = marca("M06", "BGH", inicio);
        Marca tramontina = marca("M07", "Tramontina", inicio);
        Marca pyrex = marca("M08", "Pyrex", inicio);
        Marca tupperware = marca("M09", "Tupperware", inicio);
        Marca elegante = marca("M10", "Elegante", inicio);
        Marca ayudin = marca("M11", "Ayudín", inicio);
        Marca vileda = marca("M12", "Vileda", inicio);
        Marca elvive = marca("M13", "Elvive", inicio);
        Marca dove = marca("M14", "Dove", inicio);
        Marca colgate = marca("M15", "Colgate", inicio);
        Marca matarazzo = marca("M16", "Matarazzo", inicio);
        Marca gallo = marca("M17", "Gallo", inicio);
        Marca blancaflor = marca("M18", "Blancaflor", inicio);
        Marca presto = marca("M19", "Presto Pronta", inicio);
        Marca preferido = marca("M20", "Preferido", inicio);
        Marca cocaCola = marca("M21", "Coca-Cola", inicio);
        Marca villavicencio = marca("M22", "Villavicencio", inicio);
        Marca lays = marca("M23", "Lays", inicio);
        Marca serenisima = marca("M24", "La Serenísima", inicio);
        Marca danone = marca("M25", "Danone", inicio);
        Marca paty = marca("M26", "Paty", inicio);

        // ---------- LISTAS DE PRECIOS ----------
        lista2026 = listaPrecio("L02", "Lista precios 2026", inicio);
        ListaPrecio lista2025 = listaPrecio("L01", "Lista precios 2025", fecha(2025, 1, 2));

        // ---------- ARTÍCULOS Y PRECIOS (lista 2026) ----------
        producto("Notebook Inspiron 15", electronica, dell, 950000, inicio);
        producto("Monitor 24 pulgadas", electronica, samsung, 280000, inicio);
        producto("Mouse inalámbrico", electronica, logitech, 25000, inicio);
        producto("Smart TV 50 pulgadas", electronica, samsung, 650000, inicio);

        producto("Lavarropas automático 8kg", electrodomesticos, drean, 750000, inicio);
        producto("Pava eléctrica 1,7L", electrodomesticos, philips, 45000, inicio);
        producto("Microondas 20L", electrodomesticos, bgh, 220000, inicio);

        producto("Pack de 12 platos", bazar, tramontina, 60000, inicio);
        producto("Set de bowls de vidrio", bazar, pyrex, 25000, inicio);
        producto("Tupper hermético 1L", bazar, tupperware, 18000, inicio);

        producto("Papel higiénico x4", limpieza, elegante, 6500, inicio);
        producto("Lavandina 1L", limpieza, ayudin, 2200, inicio);
        producto("Mopa para piso", limpieza, vileda, 15000, inicio);

        producto("Shampoo Elvive 400ml", higiene, elvive, 6800, inicio);
        producto("Jabón Dove 90g", higiene, dove, 2500, inicio);
        producto("Cepillo de dientes", higiene, colgate, 3500, inicio);
        producto("Desodorante Dove 150ml", higiene, dove, 5200, inicio);

        ListaPrecioArticulo fideos = producto("Fideos moñito 500g", almacen, matarazzo, 1800, inicio);
        producto("Arroz doble carolina 1kg", almacen, gallo, 2600, inicio);
        producto("Harina de trigo 000 1kg", almacen, blancaflor, 1500, inicio);
        producto("Harina de maíz 1kg", almacen, presto, 1700, inicio);
        producto("Maple de huevos x30", almacen, null, 6500, inicio);
        producto("Pan rallado 500g", almacen, preferido, 2200, inicio);

        ListaPrecioArticulo gaseosa = producto("Gaseosa cola 1,5L", bebidas, cocaCola, 2500, inicio);
        producto("Agua mineral 500ml", bebidas, villavicencio, 1200, inicio);

        producto("Papas fritas 150g", snacks, lays, 3000, inicio);

        ListaPrecioArticulo leche = producto("Leche entera 1L", lacteos, serenisima, 1600, inicio);
        producto("Manteca 200g", lacteos, serenisima, 3800, inicio);
        producto("Yogur griego 160g", lacteos, danone, 2800, inicio);
        producto("Postrecito Danette", lacteos, danone, 1500, inicio);
        producto("Queso barra x kg", lacteos, serenisima, 14000, inicio);
        producto("Queso cheddar en fetas", lacteos, serenisima, 4500, inicio);
        producto("Queso sardo x kg", lacteos, serenisima, 22000, inicio);

        producto("Filet x kg", carnes, null, 16000, inicio);
        producto("Bola de lomo x kg", carnes, null, 14000, inicio);
        producto("Asado x kg", carnes, null, 12000, inicio);
        producto("Patys x12", carnes, paty, 15000, inicio);
        producto("Pollo x kg", carnes, null, 5500, inicio);
        producto("Filet de merluza x kg", carnes, null, 13000, inicio);

        producto("Papa x kg", verduleria, null, 1200, inicio);
        producto("Cebolla x kg", verduleria, null, 1300, inicio);
        producto("Pimiento x kg", verduleria, null, 4500, inicio);
        producto("Lechuga x kg", verduleria, null, 2500, inicio);
        producto("Tomate x kg", verduleria, null, 3500, inicio);
        producto("Zanahoria x kg", verduleria, null, 1400, inicio);
        producto("Frutilla x kg", verduleria, null, 6000, inicio);
        producto("Banana x kg", verduleria, null, 2500, inicio);
        producto("Kiwi x kg", verduleria, null, 5000, inicio);
        producto("Naranja x kg", verduleria, null, 1800, inicio);

        // Un artículo discontinuado (dado de baja): NO entra al catálogo para facturar
        Articulo dvd = articulo("Reproductor de DVD", electronica, samsung, inicio);
        precio(lista2026, dvd, 80000, inicio);
        darDeBaja(dvd, fecha(2026, 3, 1));

        // La lista 2025, vieja y dada de baja, con algunos precios
        precio(lista2025, fideos.getArticulo(), 1200, fecha(2025, 1, 2));
        precio(lista2025, gaseosa.getArticulo(), 1800, fecha(2025, 1, 2));
        precio(lista2025, leche.getArticulo(), 1100, fecha(2025, 1, 2));
        darDeBaja(lista2025, inicio);

        // ---------- CLIENTES ----------
        List<ClienteSemilla> clientes = new ArrayList<>();

        // Empresas: CUITs reales, porque ARCA verifica que existan en su padrón. Los nombres son inventados.
        clientes.add(cliente("30-71686534-3", "Supermercado El Trébol S.A.", "compras@eltrebol.com", "San Martín", "1200", responsableInscripto, inicio));
        clientes.add(cliente("30-50577985-8", "Distribuidora Andes S.R.L.", "ventas@andes.com", "Las Heras", "450", responsableInscripto, inicio));
        clientes.add(cliente("30-71085744-6", "Restaurante La Tablita", "latablita@gmail.com", "Belgrano", "780", responsableInscripto, inicio));
        clientes.add(cliente("30-71481801-1", "Bodega Luján S.A.", "admin@bodegalujan.com", "San Martín", "3500", responsableInscripto, inicio));
        clientes.add(cliente("30-71175108-0", "Electro Sur S.A.", "ventas@electrosur.com", "Mitre", "1020", responsableInscripto, inicio));
        clientes.add(cliente("30-71595960-3", "Fundación Manos Abiertas", "contacto@manosabiertas.org", "Sarmiento", "230", exento, inicio));

        // Personas: consumidores finales. ARCA no verifica su CUIL (se informan "sin identificar")
        clientes.add(cliente(cuitValido("20", 34567890), "Gómez, Juan", "jgomez@gmail.com", "Belgrano", "455", consumidorFinal, inicio));
        clientes.add(cliente(cuitValido("27", 38111222), "Fernández, Lucía", "lucia.fernandez@gmail.com", "Chile", "1345", consumidorFinal, inicio));
        clientes.add(cliente(cuitValido("27", 40333444), "Rodríguez, Sofía", "sofi.rodriguez@gmail.com", "Colón", "560", consumidorFinal, inicio));
        clientes.add(cliente(cuitValido("20", 36777888), "Herrera, Diego", "diegoh@yahoo.com", "Espejo", "210", consumidorFinal, inicio));
        clientes.add(cliente(cuitValido("20", 31222333), "Pérez, Martín", "mperez@gmail.com", "Godoy Cruz", "140", consumidorFinal, inicio));
        clientes.add(cliente(cuitValido("27", 35444555), "Torres, Valentina", "vtorres@hotmail.com", "Patricias Mendocinas", "610", consumidorFinal, inicio));

        // Un cliente que compró en septiembre y se dio de baja en octubre: conserva su historial
        ClienteSemilla donPepe = cliente(cuitValido("20", 22333444), "Almacén Don Pepe", "donpepe@gmail.com", "Emilio Civit", "77", consumidorFinal, inicio);
        darDeBaja(donPepe.cliente(), fecha(2026, 10, 15));

        // ---------- FACTURAS (todas pendientes) ----------
        PuntoVenta[] puntosVenta = {casaCentral, sucursalNorte, sucursalSur};
        String[] observaciones = {null, null, null, "Entregar por la tarde", "Cliente frecuente", "Pagó en efectivo"};
        Set<Integer> facturasDonPepe = Set.of(8, 14, 20);   // caen en septiembre, antes de su baja

        int cantidadFacturas = 45;
        for (int i = 0; i < cantidadFacturas; i++) {
            Date fechaFactura = sumarDias(fecha(2026, 9, 1), i * 1.5);   // del 1/9 al 6/11
            ClienteSemilla elegido = facturasDonPepe.contains(i)
                    ? donPepe
                    : clientes.get(azar.nextInt(clientes.size()));

            FacturaVenta factura = new FacturaVenta();
            factura.setNumero(1001L + i);
            factura.setFechaEmision(fechaFactura);
            factura.setPuntoVenta(puntosVenta[azar.nextInt(puntosVenta.length)]);
            factura.setCliente(elegido.cliente());
            factura.setCondicionIva(elegido.condicion());
            factura.setTipoMoneda(pesos);
            factura.setObservaciones(observaciones[azar.nextInt(observaciones.length)]);
            factura.setDetalles(new ArrayList<>());
            factura.setEstado("PENDIENTE");

            agregarRenglones(factura, 1 + azar.nextInt(5));
            factura.setImporteCobrado(0);
            factura.setImporteSaldo(factura.getImporteTotal());

            auditar(factura, fechaFactura);
            factura.hacerCopia();   // la copia de cliente, punto de venta, etc. (después de los renglones)
            em.persist(factura);    // los renglones se guardan por la cascada
        }

        System.out.println("CargaDatos: supermercado cargado (" + catalogo.size() + " artículos, "
                + (clientes.size() + 1) + " clientes, " + cantidadFacturas + " facturas pendientes).");
    }

    // ======================= FACTURAS =======================

    private void agregarRenglones(FacturaVenta factura, int cantidadRenglones) {
        List<ListaPrecioArticulo> disponibles = new ArrayList<>(catalogo);
        Collections.shuffle(disponibles, azar);
        double total = 0;

        for (int r = 0; r < cantidadRenglones; r++) {
            ListaPrecioArticulo lpa = disponibles.get(r);
            boolean porKilo = lpa.getArticulo().isFraccionable();
            double cantidad = porKilo ? 0.5 * (1 + azar.nextInt(4)) : 1 + azar.nextInt(4);

            double precio = lpa.getPrecioVenta();
            double subtotal = Math.round(precio * cantidad * 100.0) / 100.0;
            double neto = Math.round(subtotal / 1.21 * 100.0) / 100.0;

            FacturaVentaDetalle detalle = new FacturaVentaDetalle();
            detalle.setFactura(factura);
            detalle.setListaPrecioArticulo(lpa);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precio);
            detalle.setImporteNeto(neto);
            detalle.setImporteIva(Math.round((subtotal - neto) * 100.0) / 100.0);
            detalle.setImporteSubtotal(subtotal);
            factura.agregarDetalle(detalle);

            total += subtotal;
        }
        factura.setImporteTotal(Math.round(total * 100.0) / 100.0);
    }

    // ======================= AUXILIARES =======================

    private Date fecha(int anio, int mes, int dia) {
        // al mediodía, para que ninguna zona horaria la corra de día
        return Date.from(LocalDate.of(anio, mes, dia).atTime(12, 0).toInstant(ZoneOffset.UTC));
    }

    private Date sumarDias(Date fecha, double dias) {
        return new Date(fecha.getTime() + (long) (dias * 24 * 60 * 60 * 1000));
    }

    private void auditar(AuditoriaApp entidad, Date fecha) {
        entidad.setFechaAlta(fecha);
        entidad.setFechaModificacion(fecha);
        entidad.setUsuarioCarga(admin);
        entidad.setUsuarioModificacion(admin);
    }

    private void darDeBaja(AuditoriaApp entidad, Date fecha) {
        entidad.setFechaBaja(fecha);
        entidad.setUsuarioBaja(admin);
        entidad.setFechaModificacion(fecha);
    }

    private CondicionIva condicionIva(int codigoAfip, String denominacion, Date fecha) {
        CondicionIva ci = new CondicionIva();
        ci.setCodigoAfip(codigoAfip);
        ci.setDenominacion(denominacion);
        auditar(ci, fecha);
        em.persist(ci);
        return ci;
    }

    private TipoMoneda tipoMoneda(String codigoAfip, String denominacion, String simbolo, Date fecha) {
        TipoMoneda t = new TipoMoneda();
        t.setCodigoAfip(codigoAfip);
        t.setDenominacion(denominacion);
        t.setSimbolo(simbolo);
        auditar(t, fecha);
        em.persist(t);
        return t;
    }

    private PuntoVenta puntoVenta(int numero, String descripcion, Date fecha) {
        PuntoVenta pv = new PuntoVenta();
        pv.setNumero(numero);
        pv.setDescripcion(descripcion);
        auditar(pv, fecha);
        em.persist(pv);
        return pv;
    }

    private Rubro rubro(String codigo, String denominacion, Date fecha) {
        Rubro r = new Rubro();
        r.setCodigo(codigo);
        r.setDenominacion(denominacion);
        auditar(r, fecha);
        em.persist(r);
        return r;
    }

    private Marca marca(String codigo, String denominacion, Date fecha) {
        Marca m = new Marca();
        m.setCodigo(codigo);
        m.setDenominacion(denominacion);
        auditar(m, fecha);
        em.persist(m);
        return m;
    }

    private ListaPrecio listaPrecio(String codigo, String denominacion, Date fecha) {
        ListaPrecio lista = new ListaPrecio();
        lista.setCodigo(codigo);
        lista.setDenominacion(denominacion);
        auditar(lista, fecha);
        em.persist(lista);
        return lista;
    }

    private Articulo articulo(String denominacion, Rubro rubro, Marca marca, Date fecha) {
        Articulo a = new Articulo();
        a.setCodigo(String.format("A%03d", siguienteCodigoArticulo++));
        a.setDenominacion(denominacion);
        a.setRubro(rubro);
        a.setMarca(marca);
        a.setFraccionable(denominacion.endsWith("x kg"));   // lo que va "x kg" se vende fraccionado
        auditar(a, fecha);
        em.persist(a);
        return a;
    }

    private ListaPrecioArticulo precio(ListaPrecio lista, Articulo articulo, double precioVenta, Date fecha) {
        ListaPrecioArticulo lpa = new ListaPrecioArticulo();
        lpa.setListaPrecio(lista);
        lpa.setArticulo(articulo);
        lpa.setPrecioVenta(precioVenta);
        auditar(lpa, fecha);
        em.persist(lpa);
        return lpa;
    }

    // Crea el artículo, le pone precio en la lista 2026 y lo suma al catálogo para facturar
    private ListaPrecioArticulo producto(String denominacion, Rubro rubro, Marca marca, double precioVenta, Date fecha) {
        Articulo articulo = articulo(denominacion, rubro, marca, fecha);
        ListaPrecioArticulo lpa = precio(lista2026, articulo, precioVenta, fecha);
        catalogo.add(lpa);
        return lpa;
    }

    private ClienteSemilla cliente(String cuit, String denominacion, String email,
                                   String calle, String altura, CondicionIva condicion, Date fecha) {
        Contacto contacto = new Contacto();
        contacto.setEmail(email);

        Domicilio domicilio = new Domicilio();
        domicilio.setNombreCalle(calle);
        domicilio.setNumeroCalle(altura);

        Cliente c = new Cliente();
        c.setCuitCuil(cuit);
        c.setDenominacion(denominacion);
        c.setContacto(contacto);       // se guarda solo, por la cascada
        c.setDomicilio(domicilio);     // se guarda solo, por la cascada
        auditar(c, fecha);
        em.persist(c);
        return new ClienteSemilla(c, condicion);
    }

    // Arma un CUIL con dígito verificador correcto (algoritmo módulo 11), para las personas de prueba
    private String cuitValido(String tipo, long numero) {
        int[] multiplicadores = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        while (true) {
            String base = tipo + String.format("%08d", numero);
            int suma = 0;
            for (int i = 0; i < 10; i++) {
                suma += Character.getNumericValue(base.charAt(i)) * multiplicadores[i];
            }
            int digito = 11 - (suma % 11);
            if (digito == 11) {
                digito = 0;
            }
            if (digito != 10) {   // con 10 no hay dígito válido: probamos con el número siguiente
                return tipo + "-" + String.format("%08d", numero) + "-" + digito;
            }
            numero++;
        }
    }
}