package org.cine.dao.impl;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import org.cine.dao.ReporteDAO;
import org.cine.model.*;
import org.cine.util.Conexion;
/** Solo incluye ventas confirmadas: las anuladas no suman ingresos. */
public class ReporteDAOImpl implements ReporteDAO {
 private List<Reporte> obtener(String sql, LocalDate fecha) throws SQLException {
  List<Reporte> lista=new ArrayList<>();
  try(Connection con=Conexion.getInstance().getConnection();
      CallableStatement st=con.prepareCall(sql)) {
   if(fecha!=null)st.setDate(1, Date.valueOf(fecha));
   try(ResultSet rs=st.executeQuery()) {
    while(rs.next()) lista.add(new Reporte(rs.getInt("id_venta"),rs.getTimestamp("fecha_venta").toLocalDateTime(),
     rs.getString("cliente"),rs.getString("taquillero"),rs.getInt("cantidad_boletos"),rs.getBigDecimal("total_venta")));
   }
  }
  return lista;
 }
 @Override public List<Reporte> listarVentas() throws SQLException {
  return obtener("SELECT * FROM vw_lista_ventas WHERE estado = 'confirmada' ORDER BY fecha_venta DESC, id_venta DESC",null);
 }
 @Override public List<Reporte> porDia(LocalDate f) throws SQLException {return obtener("{call sp_reporteventasdia(?)}",Objects.requireNonNull(f));}
 @Override public List<Reporte> porSemana(LocalDate f) throws SQLException {return obtener("{call sp_reporteventassemana(?)}",Objects.requireNonNull(f));}
 @Override public List<Reporte> porMes(LocalDate f) throws SQLException {return obtener("{call sp_reporteventasmes(?)}",Objects.requireNonNull(f));}
 @Override public IndicadoresAdmin indicadores() throws SQLException {
  try(Connection con=Conexion.getInstance().getConnection(); CallableStatement st=con.prepareCall("{call sp_indicadoresadmin()}"); ResultSet rs=st.executeQuery()) {
   if(!rs.next())throw new SQLException("Sin respuesta del procedimiento de indicadores");
   return new IndicadoresAdmin(rs.getInt("peliculas_activas"),rs.getInt("clientes_activos"),rs.getInt("usuarios_activos"),
    rs.getInt("funciones_pendientes"),rs.getInt("productos_stock_critico"),rs.getInt("ventas_confirmadas"),rs.getBigDecimal("ventas_totales"));
  }
 }
}
