package org.cine.dao;
import java.sql.SQLException;import java.util.List;import org.cine.model.*;import java.math.BigDecimal;
public interface VentaDAO {
 record Opcion(int id,String nombre){public String toString(){return nombre;}}
 record Funcion(int id,String nombre,BigDecimal precio){public String toString(){return nombre+" - Q"+precio;}}
 record Butaca(int id,String nombre,boolean disponible){public String toString(){return nombre;}}
 List<Opcion> clientes() throws SQLException;List<Funcion> funciones() throws SQLException;List<Butaca> butacas(int funcion) throws SQLException;
 int abrir(int cliente,int usuario) throws SQLException;void agregar(int venta,int funcion,int butaca) throws SQLException;void quitar(int venta,int boleto) throws SQLException;
 List<Boleto> boletos(int venta) throws SQLException;Venta confirmar(int venta) throws SQLException;Venta consultar(int id) throws SQLException;List<Venta> ventas() throws SQLException;
 List<String> factura(int id) throws SQLException;void cancelar(int id) throws SQLException;
}
