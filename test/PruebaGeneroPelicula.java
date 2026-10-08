import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.cine.dao.GeneroDAO;
import org.cine.dao.PeliculaDAO;
import org.cine.dao.impl.GeneroDAOImpl;
import org.cine.dao.impl.PeliculaDAOImpl;
import org.cine.model.Genero;
import org.cine.model.Pelicula;

public class PruebaGeneroPelicula {

    private static int pruebasRealizadas = 0;
    private static int pruebasExitosas = 0;

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println(" PRUEBA T1.3.12 - GENERO Y PELICULA");
        System.out.println("========================================");

        GeneroDAO generoDAO = new GeneroDAOImpl();
        PeliculaDAO peliculaDAO = new PeliculaDAOImpl();

        Pelicula peliculaPrueba = null;

        try {

            // ========================================
            // 1. CONSULTAR GENEROS
            // ========================================

            System.out.println("\n[1] Consultando géneros...");

            List<Genero> generos = generoDAO.listarTodos();

            verificar(
                    !generos.isEmpty(),
                    "Existe al menos un género registrado."
            );

            Genero genero = null;

            for (Genero g : generos) {

                if (g.getEstado() == 1) {
                    genero = g;
                    break;
                }
            }

            verificar(
                    genero != null,
                    "Existe un género activo para relacionar."
            );

            System.out.println(
                    "Género seleccionado: "
                    + genero.getNombreGenero()
                    + " (ID: "
                    + genero.getIdGenero()
                    + ")"
            );

            // ========================================
            // 2. CREAR PELICULA
            // ========================================

            System.out.println("\n[2] Creando película de prueba...");

            String titulo =
                    "PRUEBA T1.3.12 "
                    + System.currentTimeMillis();

            peliculaPrueba = new Pelicula(
                    0,
                    titulo,
                    "Película creada para comprobar T1.3.12.",
                    "Director de prueba",
                    120,
                    "PG-13",
                    "Español",
                    Date.valueOf(LocalDate.now()),
                    genero.getIdGenero(),
                    1
            );

            boolean insertada =
                    peliculaDAO.insertar(peliculaPrueba);

            verificar(
                    insertada,
                    "La película fue insertada correctamente."
            );

            verificar(
                    peliculaPrueba.getIdPelicula() > 0,
                    "La película recibió un ID."
            );

            System.out.println(
                    "Película creada: "
                    + peliculaPrueba.getTitulo()
                    + " (ID: "
                    + peliculaPrueba.getIdPelicula()
                    + ")"
            );

            // ========================================
            // 3. COMPROBAR RELACION GENERO -> PELICULA
            // ========================================

            System.out.println(
                    "\n[3] Comprobando relación género → película..."
            );

            Pelicula encontrada =
                    peliculaDAO.buscarPelicula(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    encontrada != null,
                    "La película puede ser consultada."
            );

            verificar(
                    encontrada.getIdGenero()
                    == genero.getIdGenero(),
                    "La película quedó relacionada con el género correcto."
            );

            System.out.println(
                    "Relación correcta: "
                    + genero.getNombreGenero()
                    + " → "
                    + encontrada.getTitulo()
            );

            // ========================================
            // 4. EDITAR
            // ========================================

            System.out.println("\n[4] Probando edición...");

            String tituloEditado =
                    titulo + " EDITADA";

            peliculaPrueba.setTitulo(tituloEditado);
            peliculaPrueba.setDuracionMinutos(125);

            boolean actualizada =
                    peliculaDAO.actualizar(peliculaPrueba);

            verificar(
                    actualizada,
                    "La película fue actualizada."
            );

            Pelicula editada =
                    peliculaDAO.buscarPelicula(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    editada != null
                    && editada.getTitulo().equals(tituloEditado),
                    "El título fue actualizado correctamente."
            );

            verificar(
                    editada != null
                    && editada.getDuracionMinutos() == 125,
                    "La duración fue actualizada correctamente."
            );

            // ========================================
            // 5. DESACTIVAR
            // ========================================

            System.out.println("\n[5] Probando desactivación...");

            boolean desactivada =
                    peliculaDAO.cambiarEstado(
                            peliculaPrueba.getIdPelicula(),
                            0
                    );

            verificar(
                    desactivada,
                    "La película fue desactivada."
            );

            Pelicula inactiva =
                    peliculaDAO.buscarPelicula(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    inactiva != null
                    && inactiva.getEstado() == 0,
                    "El estado cambió a Inactivo."
            );

            // ========================================
            // 6. ACTIVAR
            // ========================================

            System.out.println("\n[6] Probando activación...");

            boolean activada =
                    peliculaDAO.cambiarEstado(
                            peliculaPrueba.getIdPelicula(),
                            1
                    );

            verificar(
                    activada,
                    "La película fue activada."
            );

            Pelicula activa =
                    peliculaDAO.buscarPelicula(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    activa != null
                    && activa.getEstado() == 1,
                    "El estado cambió a Activo."
            );

            // ========================================
            // 7. ELIMINAR
            // ========================================

            System.out.println("\n[7] Probando eliminación...");

            boolean eliminada =
                    peliculaDAO.eliminar(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    eliminada,
                    "La película fue eliminada."
            );

            Pelicula despuesEliminar =
                    peliculaDAO.buscarPelicula(
                            peliculaPrueba.getIdPelicula()
                    );

            verificar(
                    despuesEliminar == null,
                    "La película ya no existe después de eliminarla."
            );

            // ========================================
            // RESULTADO
            // ========================================

            System.out.println("\n========================================");
            System.out.println(" RESULTADO DE LA PRUEBA");
            System.out.println("========================================");

            System.out.println(
                    "Pruebas realizadas: "
                    + pruebasRealizadas
            );

            System.out.println(
                    "Pruebas exitosas: "
                    + pruebasExitosas
            );

            if (pruebasRealizadas == pruebasExitosas) {

                System.out.println(
                        "\nPRUEBA EXITOSA"
                );

            } else {

                System.out.println(
                        "\nPRUEBA CON ERRORES"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "\nERROR DURANTE LA PRUEBA"
            );

            e.printStackTrace();

        } finally {

            /*
             * Si alguna prueba falla antes de eliminar
             * la película, intentamos eliminarla para
             * no dejar datos de prueba en la BD.
             */

            if (peliculaPrueba != null
                    && peliculaPrueba.getIdPelicula() > 0) {

                try {

                    peliculaDAO.eliminar(
                            peliculaPrueba.getIdPelicula()
                    );

                } catch (Exception e) {

                    System.out.println(
                            "No fue posible limpiar la película de prueba."
                    );
                }
            }
        }
    }

    private static void verificar(
            boolean resultado,
            String mensaje) {

        pruebasRealizadas++;

        if (resultado) {

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