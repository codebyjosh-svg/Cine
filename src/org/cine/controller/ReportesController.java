package org.cine.controller;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.chart.*;
import org.cine.model.PeliculaEstadistica;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.cine.dao.ReporteDAO;
import org.cine.dao.impl.ReporteDAOImpl;
import org.cine.model.Reporte;
import org.cine.system.Principal;
public class ReportesController {
 @FXML private ComboBox<String> cmbPeriodo;
 @FXML private DatePicker dpFecha;
 @FXML private TableView<Reporte> tablaVentas;
 @FXML private TableColumn<Reporte,Integer> colId, colBoletos;
 @FXML private TableColumn<Reporte,String> colFecha,colCliente,colTaquillero,colTotal;
 @FXML private Label lblCantidad,lblTotal,lblEstado;
 @FXML private Label lblMasVista,lblBoletosPeriodo,lblFuncionesConVentas,lblIngresosBoletos,lblIngresosConfiteria;
 @FXML private BarChart<String,Number> graficaPeliculas;
 @FXML private TableView<PeliculaEstadistica> tablaPeliculas;
 @FXML private TableColumn<PeliculaEstadistica,String> colPelicula;
 @FXML private TableColumn<PeliculaEstadistica,Integer> colAsistencia;
 @FXML private TableColumn<PeliculaEstadistica,String> colIngresosPelicula;
 private final ReporteDAO dao=new ReporteDAOImpl();
 @FXML private void initialize(){
  cmbPeriodo.getItems().setAll("Todas","Día","Semana","Mes"); cmbPeriodo.setValue("Mes"); dpFecha.setValue(LocalDate.now());
  colId.setCellValueFactory(c->new ReadOnlyObjectWrapper<>(c.getValue().getIdVenta()));
  colFecha.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
  colCliente.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getCliente()));
  colTaquillero.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().getTaquillero()));
  colBoletos.setCellValueFactory(c->new ReadOnlyObjectWrapper<>(c.getValue().getBoletos()));
  colTotal.setCellValueFactory(c->new ReadOnlyStringWrapper("Q " + c.getValue().getTotal().toPlainString()));
  if (colPelicula != null) {
   colPelicula.setCellValueFactory(c->new ReadOnlyStringWrapper(c.getValue().pelicula()));
   colAsistencia.setCellValueFactory(c->new ReadOnlyObjectWrapper<>(c.getValue().boletos()));
   colIngresosPelicula.setCellValueFactory(c->new ReadOnlyStringWrapper("Q " + c.getValue().ingresos().toPlainString()));
  }
  cmbPeriodo.setOnAction(e->filtrar()); dpFecha.setOnAction(e->filtrar()); filtrar();
 }
 @FXML private void filtrar(){
  try {
   String periodo=cmbPeriodo.getValue(); LocalDate fecha=dpFecha.getValue();
   if(!"Todas".equals(periodo) && fecha==null){lblEstado.setText("Selecciona una fecha.");return;}
   List<Reporte> datos=switch(periodo){case "Día"->dao.porDia(fecha);case "Semana"->dao.porSemana(fecha);case "Mes"->dao.porMes(fecha);default->dao.listarVentas();};
   tablaVentas.getItems().setAll(datos);
   BigDecimal total=datos.stream().map(Reporte::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
   lblCantidad.setText("Ventas: " + datos.size()); lblTotal.setText("Total: Q " + total.toPlainString());
   lblEstado.setText(datos.isEmpty()?"No hay ventas confirmadas en este período.":"Datos actualizados.");
   cargarEstadisticas(periodo,fecha,total);
  }catch(SQLException ex){tablaVentas.getItems().clear();lblCantidad.setText("Ventas: —");lblTotal.setText("Total: —");lblEstado.setText("Error al consultar MySQL: " + ex.getMessage());}
 }
 private void cargarEstadisticas(String periodo,LocalDate fecha,BigDecimal totalVentas) {
  try {
   List<PeliculaEstadistica> peliculas=dao.peliculasMasVistas(periodo,fecha);
   int boletos=dao.totalBoletos(periodo,fecha);
   int funciones=dao.funcionesConVentas(periodo,fecha);
   BigDecimal ingresoBoletos=dao.ingresosBoletos(periodo,fecha);
   lblMasVista.setText(peliculas.isEmpty()?"Sin ventas de boletos":peliculas.get(0).pelicula());
   lblBoletosPeriodo.setText(Integer.toString(boletos));
   lblFuncionesConVentas.setText(Integer.toString(funciones));
   lblIngresosBoletos.setText("Q " + ingresoBoletos.toPlainString());
   lblIngresosConfiteria.setText("Q " + totalVentas.subtract(ingresoBoletos).max(BigDecimal.ZERO).toPlainString());
   tablaPeliculas.getItems().setAll(peliculas);
   XYChart.Series<String,Number> serie=new XYChart.Series<>();
   serie.setName("Boletos vendidos");
   for(PeliculaEstadistica p:peliculas)serie.getData().add(new XYChart.Data<>(p.pelicula(),p.boletos()));
   graficaPeliculas.getData().setAll(serie);
  } catch(SQLException ex) {
   lblMasVista.setText("No disponible");
   lblBoletosPeriodo.setText("—"); lblFuncionesConVentas.setText("—");
   lblIngresosBoletos.setText("—"); lblIngresosConfiteria.setText("—");
   tablaPeliculas.getItems().clear(); graficaPeliculas.getData().clear();
   lblEstado.setText("Ventas cargadas. Error en estadísticas: " + ex.getMessage());
  }
 }
 @FXML private void volver() throws IOException {Principal.mostrarDashboardSegunRol();}
}
