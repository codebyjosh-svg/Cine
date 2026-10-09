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
   if(fecha!=null)st.setDate(1, java.sql.Date.valueOf(fecha));
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
 @Override public List<Reporte> porDia(LocalDate f) throws SQLException {return obtener("SELECT * FROM vw_lista_ventas WHERE estado = 'confirmada' AND DATE(fecha_venta) = ? ORDER BY fecha_venta DESC",Objects.requireNonNull(f));}
 @Override public List<Reporte> porSemana(LocalDate f) throws SQLException {return obtener("SELECT * FROM vw_lista_ventas WHERE estado = 'confirmada' AND YEARWEEK(fecha_venta,3) = YEARWEEK(?,3) ORDER BY fecha_venta DESC",Objects.requireNonNull(f));}
 @Override public List<Reporte> porMes(LocalDate f) throws SQLException {return obtener("SELECT * FROM vw_lista_ventas WHERE estado = 'confirmada' AND DATE_FORMAT(fecha_venta,'%Y-%m') = DATE_FORMAT(?,'%Y-%m') ORDER BY fecha_venta DESC",Objects.requireNonNull(f));}
 @Override public IndicadoresAdmin indicadores() throws SQLException {
  String sql = "SELECT "
   + "(SELECT COUNT(*) FROM peliculas WHERE estado=1) AS peliculas_activas, "
   + "(SELECT COUNT(*) FROM clientes WHERE estado=1) AS clientes_activos, "
   + "(SELECT COUNT(*) FROM usuarios WHERE estado=1) AS usuarios_activos, "
   + "(SELECT COUNT(*) FROM funciones WHERE estado='programada' AND fecha_inicio>NOW()) AS funciones_pendientes, "
   + "(SELECT COUNT(*) FROM productos WHERE estado=1 AND stock<=stock_minimo) AS productos_stock_critico, "
   + "(SELECT COUNT(*) FROM ventas WHERE estado='confirmada') AS ventas_confirmadas, "
   + "(SELECT COALESCE(SUM(total),0) FROM ventas WHERE estado='confirmada') AS ventas_totales";
  try (Connection con=Conexion.getInstance().getConnection();
       java.sql.PreparedStatement st=con.prepareStatement(sql);
       ResultSet rs=st.executeQuery()) {
   if (!rs.next()) throw new SQLException("Sin datos de indicadores");
   return new IndicadoresAdmin(rs.getInt("peliculas_activas"),rs.getInt("clientes_activos"),rs.getInt("usuarios_activos"),
    rs.getInt("funciones_pendientes"),rs.getInt("productos_stock_critico"),rs.getInt("ventas_confirmadas"),rs.getBigDecimal("ventas_totales"));
  }
 }
}
