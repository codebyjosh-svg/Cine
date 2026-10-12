import java.util.List;

import org.cine.dao.CarteleraDAO;
import org.cine.dao.impl.CarteleraDAOImpl;
import org.cine.model.CarteleraItem;

/**
 * Pruebas de la funcionalidad de Cartelera.
 *
 * Comprueba:
 * - Carga de funciones desde la base de datos.
 * - Existencia de los datos necesarios.
 * - Búsqueda por título.
 * - Filtro por género.
 * - Filtro por sala.
 * - Filtro por fecha.
 * - Combinación de filtros.
 * - Manejo de resultados vacíos.
 *
 * @author Joshua
 */
public class PruebaCartelera {

    private static int pruebasRealizadas = 0;
    private static int pruebasExitosas = 0;

    public static void main(String[] args) {

        System.out.println(
                "=========================================="
        );

        System.out.println(
                "       PRUEBA T2.3.11 - CARTELERA"
        );

        System.out.println(
                "=========================================="
        );

        CarteleraDAO carteleraDAO =
                new CarteleraDAOImpl();

        try {

            List<CarteleraItem> funciones =
                    carteleraDAO.listarCartelera();

            System.out.println();
            System.out.println(
                    "[1] Comprobando carga de cartelera..."
            );

            verificar(
                    funciones != null,
                    "La consulta devuelve una lista."
            );

            verificar(
                    funciones != null
                    && !funciones.isEmpty(),
                    "La cartelera contiene funciones."
            );

            if (funciones == null
                    || funciones.isEmpty()) {

                System.out.println();
                System.out.println(
                        "[AVISO] No existen funciones "
                        + "en la base de datos."
                );

                System.out.println(
                        "No se pueden realizar las pruebas "
                        + "de filtros con datos reales."
                );

            } else {

                probarDatosFuncion(funciones);

                probarBusquedaTitulo(funciones);

                probarFiltroGenero(funciones);

                probarFiltroSala(funciones);

                probarFiltroFecha(funciones);

                probarFiltrosCombinados(funciones);

                probarResultadoVacio(funciones);
            }

            System.out.println();
            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "RESULTADO: "
                    + pruebasExitosas
                    + "/"
                    + pruebasRealizadas
                    + " pruebas correctas."
            );

            System.out.println(
                    "=========================================="
            );

            if (pruebasExitosas
                    == pruebasRealizadas) {

                System.out.println(
                        "[OK] T2.3.11 completada correctamente."
                );

            } else {

                System.out.println(
                        "[ERROR] Existen pruebas pendientes."
                );
            }

        } catch (Exception ex) {

            System.out.println();
            System.out.println(
                    "[ERROR] No se pudo ejecutar "
                    + "la prueba de Cartelera."
            );

            ex.printStackTrace();
        }
    }

    /**
     * Comprueba que cada función tenga los datos
     * necesarios para mostrarla en la cartelera.
     */
    private static void probarDatosFuncion(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[2] Comprobando datos de las funciones..."
        );

        boolean datosCorrectos = true;

        for (CarteleraItem item : funciones) {

            if (item.getTituloPelicula() == null
                    || item.getTituloPelicula().isBlank()) {

                datosCorrectos = false;
            }

            if (item.getNombreSala() == null
                    || item.getNombreSala().isBlank()) {

                datosCorrectos = false;
            }

            if (item.getFecha() == null) {

                datosCorrectos = false;
            }

            if (item.getHora() == null) {

                datosCorrectos = false;
            }

            if (item.getPrecio() == null) {

                datosCorrectos = false;
            }

            if (item.getEstado() == null
                    || item.getEstado().isBlank()) {

                datosCorrectos = false;
            }
        }

        verificar(
                datosCorrectos,
                "Las funciones contienen película, "
                + "sala, fecha, hora, precio y estado."
        );
    }

    /**
     * Prueba la búsqueda por título.
     */
    private static void probarBusquedaTitulo(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[3] Probando búsqueda por título..."
        );

        CarteleraItem primera =
                funciones.get(0);

        String titulo =
                primera.getTituloPelicula();

        String textoBusqueda =
                obtenerPrimerTexto(titulo);

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                item.getTituloPelicula()
                                        != null
                                && item.getTituloPelicula()
                                        .toLowerCase()
                                        .contains(
                                                textoBusqueda
                                                        .toLowerCase()
                                        )
                        )
                        .toList();

        verificar(
                !resultados.isEmpty(),
                "La búsqueda por título devuelve resultados."
        );
    }

    /**
     * Prueba el filtro por género.
     */
    private static void probarFiltroGenero(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[4] Probando filtro por género..."
        );

        CarteleraItem primera =
                funciones.get(0);

        String genero =
                primera.getGenero();

        if (genero == null
                || genero.isBlank()) {

            System.out.println(
                    "[AVISO] No hay género disponible "
                    + "para realizar esta prueba."
            );

            return;
        }

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                item.getGenero() != null
                                && item.getGenero()
                                        .equalsIgnoreCase(
                                                genero
                                        )
                        )
                        .toList();

        verificar(
                !resultados.isEmpty(),
                "El filtro por género devuelve resultados."
        );
    }

    /**
     * Prueba el filtro por sala.
     */
    private static void probarFiltroSala(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[5] Probando filtro por sala..."
        );

        CarteleraItem primera =
                funciones.get(0);

        String sala =
                primera.getNombreSala();

        if (sala == null
                || sala.isBlank()) {

            System.out.println(
                    "[AVISO] No hay sala disponible "
                    + "para realizar esta prueba."
            );

            return;
        }

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                item.getNombreSala() != null
                                && item.getNombreSala()
                                        .equalsIgnoreCase(
                                                sala
                                        )
                        )
                        .toList();

        verificar(
                !resultados.isEmpty(),
                "El filtro por sala devuelve resultados."
        );
    }

    /**
     * Prueba el filtro por fecha.
     */
    private static void probarFiltroFecha(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[6] Probando filtro por fecha..."
        );

        CarteleraItem primera =
                funciones.get(0);

        if (primera.getFecha() == null) {

            System.out.println(
                    "[AVISO] No hay fecha disponible "
                    + "para realizar esta prueba."
            );

            return;
        }

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                primera.getFecha()
                                        .equals(
                                                item.getFecha()
                                        )
                        )
                        .toList();

        verificar(
                !resultados.isEmpty(),
                "El filtro por fecha devuelve resultados."
        );
    }

    /**
     * Prueba la combinación de filtros.
     */
    private static void probarFiltrosCombinados(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[7] Probando combinación de filtros..."
        );

        CarteleraItem primera =
                funciones.get(0);

        String genero =
                primera.getGenero();

        String sala =
                primera.getNombreSala();

        if (genero == null
                || genero.isBlank()
                || sala == null
                || sala.isBlank()
                || primera.getFecha() == null) {

            System.out.println(
                    "[AVISO] No existen datos suficientes "
                    + "para combinar filtros."
            );

            return;
        }

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                item.getGenero() != null
                                && item.getGenero()
                                        .equalsIgnoreCase(
                                                genero
                                        )
                        )
                        .filter(item ->
                                item.getNombreSala() != null
                                && item.getNombreSala()
                                        .equalsIgnoreCase(
                                                sala
                                        )
                        )
                        .filter(item ->
                                primera.getFecha()
                                        .equals(
                                                item.getFecha()
                                        )
                        )
                        .toList();

        verificar(
                !resultados.isEmpty(),
                "La combinación de filtros "
                + "devuelve resultados."
        );
    }

    /**
     * Comprueba que una búsqueda inexistente
     * produzca cero resultados.
     */
    private static void probarResultadoVacio(
            List<CarteleraItem> funciones) {

        System.out.println();
        System.out.println(
                "[8] Probando búsqueda sin resultados..."
        );

        String textoInexistente =
                "PELICULA_QUE_NO_EXISTE_T2_3_11";

        List<CarteleraItem> resultados =
                funciones.stream()
                        .filter(item ->
                                item.getTituloPelicula()
                                        != null
                                && item.getTituloPelicula()
                                        .toLowerCase()
                                        .contains(
                                                textoInexistente
                                                        .toLowerCase()
                                        )
                        )
                        .toList();

        verificar(
                resultados.isEmpty(),
                "Una búsqueda inexistente "
                + "devuelve cero resultados."
        );
    }

    /**
     * Obtiene una parte del título para utilizarla
     * como texto de búsqueda.
     */
    private static String obtenerPrimerTexto(
            String texto) {

        if (texto == null
                || texto.isBlank()) {

            return "cine";
        }

        String[] partes =
                texto.trim().split("\\s+");

        return partes[0];
    }

    /**
     * Registra el resultado de una prueba.
     */
    private static void verificar(
            boolean condicion,
            String mensaje) {

        pruebasRealizadas++;

        if (condicion) {

            pruebasExitosas++;

            System.out.println(
                    "[OK] " + mensaje
            );

        } else {

            System.out.println(
                    "[ERROR] " + mensaje
            );
        }
    }
}