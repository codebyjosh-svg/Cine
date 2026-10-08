package org.cine.test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.cine.dao.FuncionDAO;
import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.model.Funcion;

/** Pruebas reales de horarios y estados, T2.2.12. */
public class PruebaFunciones {
    private static int comprobaciones;
    private static final FuncionDAO DAO = new FuncionDAOImpl();
    @FunctionalInterface private interface Accion { void ejecutar() throws Exception; }

    private static void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
        System.out.println("OK " + (++comprobaciones) + " · " + mensaje);
    }

    private static void rechazar(Accion accion, String texto, String mensaje) throws Exception {
        try {
            accion.ejecutar();
            throw new AssertionError("Se permitió: " + mensaje);
        } catch (SQLException ex) {
            if (!"45000".equals(ex.getSQLState()) || !ex.getMessage().contains(texto)) throw ex;
            comprobar(true, mensaje);
        }
    }

    public static void main(String[] args) throws Exception {
        try (DatosFuncionesPrueba datos = new DatosFuncionesPrueba()) {
            LocalDateTime inicio = datos.inicioFuturo();
            comprobar(DAO.listarPeliculasActivas().stream().anyMatch(p -> p.getIdPelicula() == datos.pelicula1)
                    && DAO.listarPeliculasActivas().stream().noneMatch(p -> p.getIdPelicula() == datos.peliculaInactiva),
                    "ComboBox de películas incluye activas y excluye inactivas");
            comprobar(DAO.listarSalasActivas().stream().anyMatch(s -> s.getIdSala() == datos.sala1)
                    && DAO.listarSalasActivas().stream().noneMatch(s -> s.getIdSala() == datos.salaInactiva),
                    "ComboBox de salas incluye activas y excluye inactivas");
            Funcion primera = new Funcion(datos.pelicula1, datos.sala1, inicio, new BigDecimal("35.50"));
            int id = DAO.insertar(primera);
            Funcion guardada = DAO.buscarPorId(id).orElseThrow();
            comprobar(guardada.isProgramada() && guardada.getTituloPelicula().equals("Horizonte de prueba")
                    && guardada.getFechaFin().equals(inicio.plusMinutes(100)), "Alta con fecha final automática y 20 minutos de limpieza");
            rechazar(() -> DAO.insertar(new Funcion(datos.pelicula1, datos.sala1, inicio.plusMinutes(99), new BigDecimal("35"))),
                    "cruza", "MySQL rechaza un minuto de solapamiento en la misma sala");
            Funcion contigua = new Funcion(datos.pelicula1, datos.sala1, guardada.getFechaFin(), new BigDecimal("35"));
            DAO.insertar(contigua);
            comprobar(DAO.buscarPorId(contigua.getIdFuncion()).isPresent(), "Horarios contiguos permitidos sin solapamiento");
            Funcion otraSala = new Funcion(datos.pelicula1, datos.sala2, inicio, new BigDecimal("35"));
            DAO.insertar(otraSala);
            comprobar(DAO.buscarPorId(otraSala.getIdFuncion()).isPresent(), "El mismo horario se permite en otra sala");
            contigua.setFechaInicio(inicio.plusMinutes(50));
            rechazar(() -> DAO.actualizar(contigua), "cruza", "La edición rechaza un horario que se cruza");
            comprobar(DAO.buscarPorId(contigua.getIdFuncion()).orElseThrow().getFechaInicio().equals(inicio.plusMinutes(100)),
                    "Un conflicto de edición conserva los datos anteriores");
            contigua.setFechaInicio(inicio.plusMinutes(160));
            contigua.setIdPelicula(datos.pelicula2);
            contigua.setPrecioBoleto(new BigDecimal("45.75"));
            DAO.actualizar(contigua);
            Funcion editada = DAO.buscarPorId(contigua.getIdFuncion()).orElseThrow();
            comprobar(editada.getIdPelicula() == datos.pelicula2 && editada.getPrecioBoleto().compareTo(new BigDecimal("45.75")) == 0
                    && editada.getFechaFin().equals(inicio.plusMinutes(290)), "Edición cambia película y precio y recalcula el final");
            rechazar(() -> DAO.insertar(new Funcion(datos.peliculaInactiva, datos.sala1, inicio.plusDays(1), new BigDecimal("35"))),
                    "activas", "Película inactiva rechazada aunque se envíe desde fuera del ComboBox");
            rechazar(() -> DAO.insertar(new Funcion(datos.pelicula1, datos.salaInactiva, inicio, new BigDecimal("35"))),
                    "activas", "Sala inactiva rechazada por el procedimiento");
            rechazar(() -> DAO.insertar(new Funcion(datos.pelicula1, datos.salaSinButacas, inicio, new BigDecimal("35"))),
                    "butacas", "Una sala sin butacas activas no puede programarse");
            rechazar(() -> DAO.insertar(new Funcion(datos.pelicula1, datos.sala1, DAO.obtenerHoraServidor().minusDays(1), new BigDecimal("35"))),
                    "futura", "Una fecha pasada no puede programarse");
            rechazar(() -> DAO.finalizar(contigua.getIdFuncion()), "no finaliza", "La función no puede finalizar antes de tiempo");
            DAO.cancelar(id);
            comprobar("cancelada".equals(DAO.buscarPorId(id).orElseThrow().getEstado()), "Cancelación conserva la función y su estado");
            rechazar(() -> DAO.cancelar(id), "programada", "Una función cancelada no puede cancelarse nuevamente");
            rechazar(() -> DAO.actualizar(primera), "programada", "Una función cancelada no puede editarse");
            Funcion reemplazo = new Funcion(datos.pelicula1, datos.sala1, inicio, new BigDecimal("35"));
            DAO.insertar(reemplazo);
            comprobar(DAO.buscarPorId(reemplazo.getIdFuncion()).isPresent(), "Cancelar libera el horario de la sala");
            int boleto = datos.reservarBoleto(otraSala);
            rechazar(() -> DAO.cancelar(otraSala.getIdFuncion()), "Anular primero", "Los boletos activos impiden cancelar la función");
            rechazar(() -> DAO.actualizar(otraSala), "historial", "El historial de boletos impide editar la función");
            datos.anularBoleto(boleto);
            rechazar(() -> DAO.actualizar(otraSala), "historial", "Los boletos anulados también conservan el bloqueo de edición");
            DAO.cancelar(otraSala.getIdFuncion());
            comprobar("cancelada".equals(DAO.buscarPorId(otraSala.getIdFuncion()).orElseThrow().getEstado()),
                    "La cancelación se permite cuando ya no hay boletos activos");
            int vencida = datos.funcionVencida();
            DAO.finalizar(vencida);
            comprobar("finalizada".equals(DAO.buscarPorId(vencida).orElseThrow().getEstado()), "Una función cuyo horario terminó puede finalizarse");
            rechazar(() -> DAO.finalizar(vencida), "ya terminó", "La finalización repetida se rechaza");
            for (BigDecimal precioInvalido : new BigDecimal[]{BigDecimal.ZERO, new BigDecimal("35.123"), new BigDecimal("100000000.00")}) {
                try {
                    DAO.insertar(new Funcion(datos.pelicula1, datos.sala1, inicio.plusDays(5), precioInvalido));
                    throw new AssertionError("Se permitió un precio inválido: " + precioInvalido);
                } catch (IllegalArgumentException ex) {
                    comprobar(true, "El DAO rechaza el precio inválido " + precioInvalido + " antes de enviarlo a MySQL");
                }
            }
        }
        System.out.println("RESULTADO: " + comprobaciones + " comprobaciones de funciones correctas; datos de prueba retirados.");
    }
}
