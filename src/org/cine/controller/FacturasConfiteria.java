package org.cine.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.cine.util.Conexion;

/** Facturas persistentes de ventas de confitería: consulta y reimpresión. */
public final class FacturasConfiteria {
    private FacturasConfiteria() { }
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private record Registro(int id, LocalDateTime fecha, String cliente, String cajero, BigDecimal total,
                            String pago, String estado) { }

    private static void aviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alerta.setHeaderText("Confitería - Facturas"); alerta.showAndWait();
    }
    private static Registro obtener(int id) throws SQLException {
        String sql = "SELECT v.id_venta, v.fecha_venta, CONCAT(c.nombre_cliente,' ',c.apellido_cliente) AS cliente, "
            + "CONCAT(u.nombre_usuario,' ',u.apellido_usuario) AS cajero, v.total,v.metodo_pago,v.estado "
            + "FROM ventas v JOIN clientes c ON c.id_cliente=v.id_cliente "
            + "JOIN usuarios u ON u.id_usuario=v.id_usuario "
            + "WHERE v.id_venta=? AND EXISTS (SELECT 1 FROM detalle_venta_productos d WHERE d.id_venta=v.id_venta)";
        try(Connection c=Conexion.getInstance().getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery()) {return r.next()?fila(r):null;}
        }
    }
    private static Registro fila(ResultSet r) throws SQLException {
        return new Registro(r.getInt("id_venta"),r.getTimestamp("fecha_venta").toLocalDateTime(),
            r.getString("cliente"),r.getString("cajero"),r.getBigDecimal("total"),
            r.getString("metodo_pago"),r.getString("estado"));
    }
    private static String textoFactura(Registro r) throws SQLException {
        StringBuilder b=new StringBuilder();
        b.append("               CINEMA\n");
        b.append("          FACTURA DE CONFITERÍA\n");
        b.append("----------------------------------------\n");
        b.append("Factura #: ").append(r.id()).append("\n");
        b.append("Fecha: ").append(FECHA.format(r.fecha())).append("\n");
        b.append("Cliente: ").append(r.cliente()).append("\n");
        b.append("Atendió: ").append(r.cajero()).append("\n");
        b.append("Pago: ").append(r.pago()).append("\n");
        b.append("Estado: ").append(r.estado()).append("\n");
        b.append("----------------------------------------\n");
        b.append("PRODUCTO             CANT.  P.UNIT.  TOTAL\n");
        BigDecimal totalProductos=BigDecimal.ZERO;
        String sql="SELECT p.nombre_producto,d.cantidad,d.precio_unitario,d.subtotal "
            +"FROM detalle_venta_productos d JOIN productos p ON p.id_producto=d.id_producto "
            +"WHERE d.id_venta=? ORDER BY d.id_detalle_producto";
        try(Connection c=Conexion.getInstance().getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,r.id());
            try(ResultSet rs=p.executeQuery()) {
                while(rs.next()) {
                    String nombre=rs.getString("nombre_producto");
                    b.append(nombre).append("\n  ").append(rs.getInt("cantidad"))
                      .append(" x Q").append(rs.getBigDecimal("precio_unitario"))
                      .append(" = Q").append(rs.getBigDecimal("subtotal")).append("\n");
                    totalProductos=totalProductos.add(rs.getBigDecimal("subtotal"));
                }
            }
        }
        b.append("----------------------------------------\n");
        b.append("SUBTOTAL CONFITERÍA: Q").append(totalProductos).append("\n");
        // Una venta puede contener también boletos; el total general corresponde al registro de ventas.
        if(r.total().compareTo(totalProductos)!=0)
            b.append("TOTAL DE LA TRANSACCIÓN: Q").append(r.total()).append("\n");
        b.append("----------------------------------------\n");
        b.append("     Gracias por su compra en CINEMA\n");
        b.append("Conserve su número para solicitar copia.\n");
        if("anulada".equalsIgnoreCase(r.estado())) b.append("*** VENTA ANULADA ***\n");
        return b.toString();
    }
    public static void mostrarFactura(int id) {
        try {
            Registro r=obtener(id);
            if(r==null){aviso("No se encontró una factura de confitería con ese número.");return;}
            Stage ventana=new Stage();
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setTitle("CINEMA - Factura #"+id);
            TextArea contenido=new TextArea(textoFactura(r));
            contenido.setEditable(false);contenido.setWrapText(false);contenido.setFont(Font.font("Monospaced",12));
            contenido.setStyle("-fx-control-inner-background: white; -fx-text-fill: #15243c;");
            Button imprimir=new Button("Imprimir factura");
            imprimir.setOnAction(e -> {
                Alert confirmacion = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Factura impresa correctamente.",
                    ButtonType.OK
                );
                confirmacion.initOwner(ventana);
                confirmacion.setTitle("Confitería - Factura");
                confirmacion.setHeaderText("Impresión registrada");
                confirmacion.showAndWait();
            });
            Button cerrar=new Button("Cerrar");cerrar.setOnAction(e->ventana.close());
            HBox botones=new HBox(12,imprimir,cerrar);botones.setPadding(new Insets(10));
            VBox root=new VBox(10,new Label("Factura #"+id+" — "+r.cliente()),contenido,botones);
            root.setPadding(new Insets(15));VBox.setVgrow(contenido,Priority.ALWAYS);
            ventana.setScene(new Scene(root,650,550));ventana.showAndWait();
        } catch(Exception ex) {aviso("No se pudo abrir la factura: "+ex.getMessage());ex.printStackTrace();}
    }
    public static void buscarFacturas() {
        Stage ventana=new Stage();ventana.initModality(Modality.APPLICATION_MODAL);
        ventana.setTitle("CINEMA - Buscar facturas");
        TextField filtro=new TextField();
        filtro.setPromptText("Número de factura, nombre del cliente o fecha (AAAA-MM-DD)");
        TableView<Registro> tabla=new TableView<>();
        TableColumn<Registro,String> numero=new TableColumn<>("Factura");
        numero.setCellValueFactory(c->new SimpleStringProperty("#"+c.getValue().id()));numero.setPrefWidth(85);
        TableColumn<Registro,String> fecha=new TableColumn<>("Fecha");
        fecha.setCellValueFactory(c->new SimpleStringProperty(FECHA.format(c.getValue().fecha())));fecha.setPrefWidth(145);
        TableColumn<Registro,String> cliente=new TableColumn<>("Cliente");
        cliente.setCellValueFactory(c->new SimpleStringProperty(c.getValue().cliente()));cliente.setPrefWidth(220);
        TableColumn<Registro,String> total=new TableColumn<>("Total");
        total.setCellValueFactory(c->new SimpleStringProperty("Q"+c.getValue().total()));total.setPrefWidth(95);
        TableColumn<Registro,String> estado=new TableColumn<>("Estado");
        estado.setCellValueFactory(c->new SimpleStringProperty(c.getValue().estado()));estado.setPrefWidth(105);
        tabla.getColumns().addAll(numero,fecha,cliente,total,estado);
        Runnable cargar=()-> {
            String buscado=filtro.getText()==null?"":filtro.getText().trim();
            String sql="SELECT v.id_venta,v.fecha_venta, CONCAT(c.nombre_cliente,' ',c.apellido_cliente) AS cliente, "
                +"CONCAT(u.nombre_usuario,' ',u.apellido_usuario) AS cajero,v.total,v.metodo_pago,v.estado "
                +"FROM ventas v JOIN clientes c ON c.id_cliente=v.id_cliente JOIN usuarios u ON u.id_usuario=v.id_usuario "
                +"WHERE EXISTS(SELECT 1 FROM detalle_venta_productos d WHERE d.id_venta=v.id_venta) "
                +"AND (CAST(v.id_venta AS CHAR) LIKE ? OR CONCAT(c.nombre_cliente,' ',c.apellido_cliente) LIKE ? "
                +"OR DATE_FORMAT(v.fecha_venta,'%Y-%m-%d') LIKE ?) ORDER BY v.fecha_venta DESC LIMIT 300";
            List<Registro> filas=new ArrayList<>();
            try(Connection con=Conexion.getInstance().getConnection();PreparedStatement ps=con.prepareStatement(sql)) {
                for(int i=1;i<=3;i++)ps.setString(i,"%"+buscado+"%");
                try(ResultSet rs=ps.executeQuery()) {while(rs.next())filas.add(fila(rs));}
                tabla.setItems(FXCollections.observableArrayList(filas));
            } catch(Exception ex){aviso("No se pudo buscar: "+ex.getMessage());}
        };
        Button buscar=new Button("Buscar");buscar.setOnAction(e->cargar.run());filtro.setOnAction(e->cargar.run());
        Button ver=new Button("Ver / reimprimir factura");
        ver.setOnAction(e->{Registro r=tabla.getSelectionModel().getSelectedItem();if(r==null){aviso("Selecciona una factura de la lista.");return;}mostrarFactura(r.id());});
        tabla.setOnMouseClicked(e->{if(e.getClickCount()==2){Registro r=tabla.getSelectionModel().getSelectedItem();if(r!=null)mostrarFactura(r.id());}});
        Button cerrar=new Button("Volver a Confitería");cerrar.setOnAction(e->ventana.close());
        HBox acciones=new HBox(10,buscar,ver,cerrar);
        VBox root=new VBox(12,new Label("Historial de facturas de Confitería"),filtro,acciones,tabla);
        root.setPadding(new Insets(18));VBox.setVgrow(tabla,Priority.ALWAYS);
        ventana.setScene(new Scene(root,730,520));cargar.run();ventana.showAndWait();
    }
}
