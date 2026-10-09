package org.cine.dao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.cine.model.IndicadoresAdmin;
import org.cine.model.Reporte;
public interface ReporteDAO {
 List<Reporte> listarVentas() throws SQLException;
 List<Reporte> porDia(LocalDate fecha) throws SQLException;
 List<Reporte> porSemana(LocalDate fecha) throws SQLException;
 List<Reporte> porMes(LocalDate fecha) throws SQLException;
 IndicadoresAdmin indicadores() throws SQLException;
}
