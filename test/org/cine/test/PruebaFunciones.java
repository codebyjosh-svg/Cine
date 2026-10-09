package org.cine.test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

import org.cine.dao.FuncionDAO;
import org.cine.dao.impl.FuncionDAOImpl;
import org.cine.model.Funcion;

/**
 * Pruebas de integración de T2.2.12:
 * horarios, conflictos y estados.
 */
public class PruebaFunciones {

    private static int comprobaciones;

    private static final FuncionDAO DAO =
            new FuncionDAOImpl();

    @FunctionalInterface
    private interface Accion {

        void ejecutar()
                throws Exception;
    }

    private static void comprobar(
            boolean valor,
            String mensaje
    ) {

        if (!valor) {

            throw new AssertionError(
                    mensaje
            );
        }

        System.out.println(
                "OK "
                + (++comprobaciones)
                + " · "
                + mensaje
        );
    }

    private static void rechazar(
            Accion accion,
            String textoEsperado,
            String mensaje
    ) throws Exception {

        try {

            accion.ejecutar();

            throw new AssertionError(
                    "Se permitió una operación inválida: "
                    + mensaje
            );

        } catch (SQLException ex) {

            String texto =
                    ex.getMessage() == null
                    ? ""
                    : ex.getMessage()
                            .toLowerCase(
                                    Locale.ROOT
                            );

            String esperado =
                    textoEsperado.toLowerCase(
                            Locale.ROOT
                    );

            if (
                    !"45000".equals(
                            ex.getSQLState()
                    )
                    ||
                    !texto.contains(
                            esperado
                    )
            ) {

                throw ex;
            }

            comprobar(
                    true,
                    mensaje
            );
        }
    }

    public static void main(
            String[] args
    ) throws Exception {

        try (
                DatosFuncionesPrueba datos =
                        new DatosFuncionesPrueba()
        ) {

            LocalDateTime inicio =
                    datos.inicioFuturo();

            /*
             * =================================================
             * 1. PELÍCULAS ACTIVAS
             * =================================================
             */

            comprobar(
                    DAO.listarPeliculasActivas()
                            .stream()
                            .anyMatch(
                                    p ->
                                            p.getIdPelicula()
                                            == datos.pelicula1
                            )
                    &&
                    DAO.listarPeliculasActivas()
                            .stream()
                            .noneMatch(
                                    p ->
                                            p.getIdPelicula()
                                            == datos.peliculaInactiva
                            ),

                    "El listado de películas incluye activas "
                    + "y excluye inactivas"
            );

            /*
             * =================================================
             * 2. SALAS ACTIVAS
             * =================================================
             */

            comprobar(
                    DAO.listarSalasActivas()
                            .stream()
                            .anyMatch(
                                    s ->
                                            s.getIdSala()
                                            == datos.sala1
                            )
                    &&
                    DAO.listarSalasActivas()
                            .stream()
                            .noneMatch(
                                    s ->
                                            s.getIdSala()
                                            == datos.salaInactiva
                            ),

                    "El listado de salas incluye activas "
                    + "y excluye inactivas"
            );

            /*
             * =================================================
             * 3. ALTA
             * =================================================
             */

            Funcion primera =
                    new Funcion(
                            datos.pelicula1,
                            datos.sala1,
                            inicio,
                            new BigDecimal("35.50")
                    );

            int id =
                    DAO.insertar(
                            primera
                    );

            Funcion guardada =
                    DAO.buscarPorId(id)
                            .orElseThrow(
                                    () ->
                                            new AssertionError(
                                                    "No se encontró "
                                                    + "la función creada."
                                            )
                            );

            comprobar(
                    guardada.isProgramada(),
                    "Una función nueva queda "
                    + "en estado programada"
            );

            comprobar(
                    "Horizonte de prueba"
                            .equals(
                                    guardada
                                            .getTituloPelicula()
                            ),

                    "La función devuelve correctamente "
                    + "el título de la película"
            );

            comprobar(
                    guardada.getFechaFin()
                            .equals(
                                    inicio.plusMinutes(100)
                            ),

                    "La fecha final se calcula "
                    + "con duración + 20 minutos"
            );

            /*
             * =================================================
             * 4. CONFLICTO
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.insertar(
                                    new Funcion(
                                            datos.pelicula1,
                                            datos.sala1,
                                            inicio.plusMinutes(99),
                                            new BigDecimal("35.00")
                                    )
                            ),

                    "cruza",

                    "La BD rechaza un horario "
                    + "que se cruza en la misma sala"
            );

            /*
             * =================================================
             * 5. HORARIO CONTIGUO
             * =================================================
             */

            Funcion contigua =
                    new Funcion(
                            datos.pelicula1,
                            datos.sala1,
                            guardada.getFechaFin(),
                            new BigDecimal("35.00")
                    );

            DAO.insertar(
                    contigua
            );

            comprobar(
                    DAO.buscarPorId(
                            contigua.getIdFuncion()
                    ).isPresent(),

                    "Se permite iniciar exactamente "
                    + "cuando termina otra función"
            );

            /*
             * =================================================
             * 6. MISMO HORARIO EN OTRA SALA
             * =================================================
             */

            Funcion otraSala =
                    new Funcion(
                            datos.pelicula1,
                            datos.sala2,
                            inicio,
                            new BigDecimal("35.00")
                    );

            DAO.insertar(
                    otraSala
            );

            comprobar(
                    DAO.buscarPorId(
                            otraSala.getIdFuncion()
                    ).isPresent(),

                    "El mismo horario se permite "
                    + "en otra sala"
            );

            /*
             * =================================================
             * 7. EDICIÓN CON CONFLICTO
             * =================================================
             */

            LocalDateTime inicioOriginal =
                    contigua.getFechaInicio();

            contigua.setFechaInicio(
                    inicio.plusMinutes(50)
            );

            rechazar(
                    () ->
                            DAO.actualizar(
                                    contigua
                            ),

                    "cruza",

                    "La edición rechaza "
                    + "un horario que se cruza"
            );

            comprobar(
                    DAO.buscarPorId(
                            contigua.getIdFuncion()
                    )
                    .orElseThrow()
                    .getFechaInicio()
                    .equals(
                            inicioOriginal
                    ),

                    "Una edición rechazada "
                    + "conserva el horario anterior"
            );

            /*
             * =================================================
             * 8. EDICIÓN VÁLIDA
             * =================================================
             */

            contigua.setFechaInicio(
                    inicio.plusMinutes(200)
            );

            contigua.setIdPelicula(
                    datos.pelicula2
            );

            contigua.setPrecioBoleto(
                    new BigDecimal("45.75")
            );

            DAO.actualizar(
                    contigua
            );

            Funcion editada =
                    DAO.buscarPorId(
                            contigua.getIdFuncion()
                    ).orElseThrow();

            comprobar(
                    editada.getIdPelicula()
                            == datos.pelicula2,

                    "La edición cambia la película"
            );

            comprobar(
                    editada.getPrecioBoleto()
                            .compareTo(
                                    new BigDecimal("45.75")
                            ) == 0,

                    "La edición cambia el precio"
            );

            comprobar(
                    editada.getFechaFin()
                            .equals(
                                    inicio.plusMinutes(330)
                            ),

                    "La edición recalcula "
                    + "la fecha final"
            );

            /*
             * =================================================
             * 9. PELÍCULA INACTIVA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.insertar(
                                    new Funcion(
                                            datos.peliculaInactiva,
                                            datos.sala1,
                                            inicio.plusDays(1),
                                            new BigDecimal("35.00")
                                    )
                            ),

                    "activas",

                    "Una película inactiva "
                    + "no puede programarse"
            );

            /*
             * =================================================
             * 10. SALA INACTIVA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.insertar(
                                    new Funcion(
                                            datos.pelicula1,
                                            datos.salaInactiva,
                                            inicio.plusDays(1),
                                            new BigDecimal("35.00")
                                    )
                            ),

                    "activas",

                    "Una sala inactiva "
                    + "no puede programarse"
            );

            /*
             * =================================================
             * 11. SALA SIN BUTACAS
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.insertar(
                                    new Funcion(
                                            datos.pelicula1,
                                            datos.salaSinButacas,
                                            inicio.plusDays(1),
                                            new BigDecimal("35.00")
                                    )
                            ),

                    "butacas",

                    "Una sala sin butacas activas "
                    + "no puede programarse"
            );

            /*
             * =================================================
             * 12. FECHA PASADA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.insertar(
                                    new Funcion(
                                            datos.pelicula1,
                                            datos.sala1,
                                            DAO.obtenerHoraServidor()
                                                    .minusDays(1),
                                            new BigDecimal("35.00")
                                    )
                            ),

                    "futura",

                    "Una fecha pasada "
                    + "no puede programarse"
            );

            /*
             * =================================================
             * 13. FINALIZAR ANTES DE TIEMPO
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.finalizar(
                                    contigua.getIdFuncion()
                            ),

                    "no finaliza",

                    "Una función no puede finalizar "
                    + "antes de terminar su horario"
            );

            /*
             * =================================================
             * 14. CANCELACIÓN
             * =================================================
             */

            DAO.cancelar(
                    id
            );

            comprobar(
                    "cancelada".equals(
                            DAO.buscarPorId(id)
                                    .orElseThrow()
                                    .getEstado()
                    ),

                    "La cancelación cambia "
                    + "la función a cancelada"
            );

            /*
             * =================================================
             * 15. CANCELACIÓN REPETIDA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.cancelar(id),

                    "programada",

                    "Una función cancelada "
                    + "no puede cancelarse nuevamente"
            );

            /*
             * =================================================
             * 16. EDICIÓN DE CANCELADA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.actualizar(
                                    primera
                            ),

                    "programada",

                    "Una función cancelada "
                    + "no puede editarse"
            );

            /*
             * =================================================
             * 17. HORARIO LIBERADO
             * =================================================
             */

            Funcion reemplazo =
                    new Funcion(
                            datos.pelicula1,
                            datos.sala1,
                            inicio,
                            new BigDecimal("35.00")
                    );

            DAO.insertar(
                    reemplazo
            );

            comprobar(
                    DAO.buscarPorId(
                            reemplazo.getIdFuncion()
                    ).isPresent(),

                    "Una función cancelada "
                    + "libera su horario"
            );

            /*
             * =================================================
             * 18. FUNCIÓN VENCIDA
             * =================================================
             */

            int vencida =
                    datos.funcionVencida();

            DAO.finalizar(
                    vencida
            );

            comprobar(
                    "finalizada".equals(
                            DAO.buscarPorId(vencida)
                                    .orElseThrow()
                                    .getEstado()
                    ),

                    "Una función cuyo horario terminó "
                    + "puede finalizarse"
            );

            /*
             * =================================================
             * 19. FINALIZACIÓN REPETIDA
             * =================================================
             */

            rechazar(
                    () ->
                            DAO.finalizar(
                                    vencida
                            ),

                    "ya termino",

                    "Una función finalizada "
                    + "no puede finalizarse nuevamente"
            );

            /*
             * =================================================
             * 20. PRECIOS INVÁLIDOS
             * =================================================
             */

            BigDecimal[] preciosInvalidos = {

                BigDecimal.ZERO,

                new BigDecimal(
                        "35.123"
                ),

                new BigDecimal(
                        "100000000.00"
                )
            };

            for (
                    BigDecimal precio :
                    preciosInvalidos
            ) {

                try {

                    DAO.insertar(
                            new Funcion(
                                    datos.pelicula1,
                                    datos.sala2,
                                    inicio.plusDays(5),
                                    precio
                            )
                    );

                    throw new AssertionError(
                            "Se permitió un precio inválido: "
                            + precio
                    );

                } catch (
                        IllegalArgumentException ex
                ) {

                    comprobar(
                            true,

                            "El DAO rechaza "
                            + "el precio inválido "
                            + precio
                    );
                }
            }
        }

        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "RESULTADO FINAL: "
                + comprobaciones
                + " comprobaciones correctas."
        );

        System.out.println(
                "Los datos temporales fueron retirados."
        );

        System.out.println(
                "=============================================="
        );
    }
}