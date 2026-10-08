package org.cine.controller;
import java.io.IOException;import java.sql.SQLException;import javafx.fxml.FXML;import javafx.scene.control.*;import javafx.print.PrinterJob;import org.cine.dao.VentaDAO;import org.cine.dao.impl.VentaDAOImpl;import org.cine.system.Principal;
public class FacturaController{
 private final VentaDAO dao=new VentaDAOImpl();@FXML private TextField txtIdVenta;@FXML private TextArea txtFactura;@FXML private Label lblTotal;
 public void mostrar(int id){if(id>0){txtIdVenta.setText(String.valueOf(id));consultar();}}
 @FXML private void consultar(){try{int id=Integer.parseInt(txtIdVenta.getText().trim());var v=dao.consultar(id);if(!"confirmada".equals(v.estado())&&!"anulada".equals(v.estado()))throw new SQLException("La venta no está confirmada.");StringBuilder b=new StringBuilder("CINEMA - FACTURA #"+id+"\nCLIENTE: "+v.cliente()+"\nESTADO: "+v.estado()+"\n\n");for(String linea:dao.factura(id))b.append(linea).append('\n');txtFactura.setText(b.toString());lblTotal.setText("TOTAL: Q"+v.total());}catch(SQLException|NumberFormatException e){new Alert(Alert.AlertType.ERROR,e.getMessage()).showAndWait();}}
 @FXML private void imprimir(){if(txtFactura.getText().isBlank())return;PrinterJob job=PrinterJob.createPrinterJob();if(job!=null&&job.showPrintDialog(txtFactura.getScene().getWindow())){if(job.printPage(txtFactura)){job.endJob();}else job.cancelJob();}}
 @FXML private void volver()throws IOException{Principal.mostrarVenta();}
}
