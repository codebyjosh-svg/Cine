package org.cine.controller;
import java.io.IOException;import java.math.BigDecimal;import java.sql.SQLException;import java.util.*;
import javafx.fxml.FXML;import javafx.scene.control.*;import javafx.scene.layout.FlowPane;
import org.cine.dao.VentaDAO;import org.cine.dao.impl.VentaDAOImpl;import org.cine.model.*;import org.cine.service.SesionContext;import org.cine.system.Principal;
public class VentaController {
 private final VentaDAO dao=new VentaDAOImpl();
 @FXML private ComboBox<VentaDAO.Opcion> cbCliente;@FXML private ComboBox<VentaDAO.Funcion> cbFuncion;
 @FXML private FlowPane panelButacas;@FXML private ListView<Boleto> listaBoletos;@FXML private Label lblTotal,lblEstado;
 private int ventaId;private final Set<Integer> seleccion=new LinkedHashSet<>();
 @FXML private void initialize(){try{cbCliente.getItems().setAll(dao.clientes());cbFuncion.getItems().setAll(dao.funciones());}catch(SQLException e){error(e);}cbFuncion.valueProperty().addListener((o,a,b)->cargarButacas());actualizarTotal();}
 private void cargarButacas(){seleccion.clear();panelButacas.getChildren().clear();if(cbFuncion.getValue()==null)return;
 try{for(var b:dao.butacas(cbFuncion.getValue().id())){ToggleButton t=new ToggleButton(b.nombre()+(b.disponible()?"":" ✕"));t.setPrefSize(85,45);t.setDisable(!b.disponible());t.selectedProperty().addListener((o,old,nuevo)->{if(nuevo)seleccion.add(b.id());else seleccion.remove(b.id());});panelButacas.getChildren().add(t);}}catch(SQLException e){error(e);}}
 @FXML private void agregar(){if(cbCliente.getValue()==null||cbFuncion.getValue()==null||seleccion.isEmpty()){aviso("Selecciona cliente, función y butacas disponibles.");return;}
 try{if(ventaId==0)ventaId=dao.abrir(cbCliente.getValue().id(),SesionContext.getUsuarioActual().getIdUsuario());for(int id:new ArrayList<>(seleccion))dao.agregar(ventaId,cbFuncion.getValue().id(),id);refrescar();cargarButacas();lblEstado.setText("Carrito #"+ventaId);}catch(SQLException e){try{refrescar();}catch(SQLException ignored){}cargarButacas();error(e);}}
 private void refrescar()throws SQLException{listaBoletos.getItems().setAll(ventaId==0?List.of():dao.boletos(ventaId));actualizarTotal();}
 private void actualizarTotal(){BigDecimal total=BigDecimal.ZERO;for(Boleto b:listaBoletos.getItems())total=total.add(b.precio());lblTotal.setText("Total: Q"+total);}
 @FXML private void quitar(){Boleto b=listaBoletos.getSelectionModel().getSelectedItem();if(b==null)return;try{dao.quitar(ventaId,b.id());refrescar();cargarButacas();}catch(SQLException e){error(e);}}
 @FXML private void confirmar(){if(ventaId==0||listaBoletos.getItems().isEmpty()||cbCliente.getValue()==null){aviso("Debes agregar boletos y seleccionar cliente.");return;}
 try{int id=dao.confirmar(ventaId).id();ventaId=0;Principal.mostrarFactura(id);}catch(SQLException|IOException e){error(e);}}
 @FXML private void cancelar(){if(ventaId==0)return;try{dao.cancelar(ventaId);ventaId=0;listaBoletos.getItems().clear();actualizarTotal();cargarButacas();lblEstado.setText("Carrito cancelado");}catch(SQLException e){error(e);}}
 @FXML private void volver()throws IOException{Principal.mostrarDashboardSegunRol();}
 @FXML private void reimprimir()throws IOException{Principal.mostrarFactura(0);}
 private void aviso(String s){new Alert(Alert.AlertType.WARNING,s).showAndWait();}
 private void error(Exception e){new Alert(Alert.AlertType.ERROR,e.getMessage()).showAndWait();}
}
