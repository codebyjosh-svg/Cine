package org.cine.controller;
import java.io.IOException;import javafx.fxml.FXML;import javafx.scene.control.Label;import org.cine.service.SesionContext;import org.cine.system.Principal;
public class DashboardTaquilleroController{
 @FXML private Label lblUsuario;
 @FXML private void initialize(){if(SesionContext.getUsuarioActual()!=null)lblUsuario.setText(SesionContext.getUsuarioActual().getNombreCompleto());}
 @FXML private void vender()throws IOException{Principal.mostrarVenta();}
 @FXML private void cerrarSesion()throws IOException{Principal.cerrarSesion();}
}
