package org.cine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.cine.service.SesionContext;
import org.cine.system.Principal;
import org.cine.util.Conexion;

/** Confiteria: catalogo, mantenimiento y venta de productos. */
public class ConfiteriaController {
    @FXML private TableView<ProductoFila> tabla;
    @FXML private TableColumn<ProductoFila,String> colId,colNombre,colCategoria,colPrecio,colStock,colMinimo,colEstado;
    @FXML private ComboBox<CategoriaFila> categorias;
    @FXML private ComboBox<ClienteFila> clientes;
    @FXML private TextField nombre,descripcion,precio,stockMinimo,cantidad,busqueda;
    @FXML private Label mensaje,total;
    private ProductoFila seleccionado;

    public static class ProductoFila {
        int id, categoria, stock, minimo; String nombre, descripcion, categoriaNombre; BigDecimal precio; boolean activo;
        ProductoFila(ResultSet r) throws SQLException {
            id=r.getInt("id_producto"); categoria=r.getInt("id_categoria_producto");
            nombre=r.getString("nombre_producto"); descripcion=r.getString("descripcion");
            categoriaNombre=r.getString("nombre_categoria"); precio=r.getBigDecimal("precio");
            stock=r.getInt("stock"); minimo=r.getInt("stock_minimo"); activo=r.getBoolean("estado");
        }
    }
    public static class CategoriaFila {
        final int id; final String nombre;
        CategoriaFila(int id,String n){this.id=id;nombre=n;}
        @Override public String toString(){return nombre;}
    }
    public static class ClienteFila {
        final int id; final String nombre;
        ClienteFila(int id,String n){this.id=id;nombre=n;}
        @Override public String toString(){return nombre;}
    }
    @FXML private void initialize() {
        colId.setCellValueFactory(c->new SimpleStringProperty(""+c.getValue().id));
        colNombre.setCellValueFactory(c->new SimpleStringProperty(c.getValue().nombre));
        colCategoria.setCellValueFactory(c->new SimpleStringProperty(c.getValue().categoriaNombre));
        colPrecio.setCellValueFactory(c->new SimpleStringProperty("Q "+c.getValue().precio));
        colStock.setCellValueFactory(c->new SimpleStringProperty(""+c.getValue().stock));
        colMinimo.setCellValueFactory(c->new SimpleStringProperty(""+c.getValue().minimo));
        colEstado.setCellValueFactory(c->new SimpleStringProperty(c.getValue().activo?"Activo":"Inactivo"));
        tabla.getSelectionModel().selectedItemProperty().addListener((o,a,b)->cargarFormulario(b));
        cantidad.setText("1"); recargar();
        cantidad.textProperty().addListener((o,a,b)->actualizarTotal());
    }
    @FXML private void recargar() {
        try(Connection c=Conexion.getInstance().getConnection()){
            var cats=FXCollections.<CategoriaFila>observableArrayList();
            try(PreparedStatement p=c.prepareStatement("SELECT id_categoria_producto,nombre_categoria FROM categorias_producto WHERE estado=1 ORDER BY nombre_categoria");ResultSet r=p.executeQuery()){
                while(r.next())cats.add(new CategoriaFila(r.getInt(1),r.getString(2)));
            }
            categorias.setItems(cats);
            var cs=FXCollections.<ClienteFila>observableArrayList();
            try(PreparedStatement p=c.prepareStatement("SELECT id_cliente,CONCAT(nombre_cliente,' ',apellido_cliente) AS nombre FROM clientes WHERE estado=1 ORDER BY nombre_cliente");ResultSet r=p.executeQuery()){
                while(r.next())cs.add(new ClienteFila(r.getInt(1),r.getString(2)));
            }
            ClienteFila anterior=clientes.getValue(); clientes.setItems(cs);
            if(anterior!=null)cs.stream().filter(x->x.id==anterior.id).findFirst().ifPresent(clientes::setValue);
            mostrarProductos(c);
            mensaje.setText("Catálogo actualizado.");
        }catch(SQLException e){error(e);}
    }
    private void mostrarProductos(Connection c)throws SQLException {
        var filas=FXCollections.<ProductoFila>observableArrayList();
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM vw_lista_productos ORDER BY nombre_producto");ResultSet r=p.executeQuery()){
            while(r.next())filas.add(new ProductoFila(r));
        }
        String filtro=busqueda.getText()==null?"":busqueda.getText().trim().toLowerCase();
        filas.removeIf(p->!p.nombre.toLowerCase().contains(filtro)&&!p.categoriaNombre.toLowerCase().contains(filtro));
        tabla.setItems(filas);
    }
    @FXML private void buscar(){recargar();}
    private void cargarFormulario(ProductoFila p){
        seleccionado=p;
        if(p==null)return;
        nombre.setText(p.nombre);descripcion.setText(p.descripcion==null?"":p.descripcion);
        precio.setText(p.precio.toPlainString());stockMinimo.setText(""+p.minimo);
        categorias.getItems().stream().filter(x->x.id==p.categoria).findFirst().ifPresent(categorias::setValue);
        actualizarTotal();
    }
    @FXML private void limpiar(){seleccionado=null;tabla.getSelectionModel().clearSelection();nombre.clear();descripcion.clear();precio.clear();stockMinimo.clear();categorias.setValue(null);actualizarTotal();}
    private int minimo(){int n=Integer.parseInt(stockMinimo.getText().trim());if(n<0)throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");return n;}
    private BigDecimal precioValido(){BigDecimal p=new BigDecimal(precio.getText().trim());if(p.signum()<0)throw new IllegalArgumentException("El precio debe ser positivo.");return p;}
    @FXML private void guardar(){
        try {
            CategoriaFila cat=categorias.getValue();
            if(cat==null||nombre.getText().isBlank())throw new IllegalArgumentException("Escribe el nombre y selecciona una categoría.");
            try(Connection c=Conexion.getInstance().getConnection()){
                if(seleccionado==null){
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO productos(id_categoria_producto,nombre_producto,descripcion,precio,stock,stock_minimo,estado) VALUES(?,?,?,?,0,?,1)")){
                        p.setInt(1,cat.id);p.setString(2,nombre.getText().trim());p.setString(3,descripcion.getText());p.setBigDecimal(4,precioValido());p.setInt(5,minimo());p.executeUpdate();
                    }
                }else{
                    try(PreparedStatement p=c.prepareStatement("UPDATE productos SET id_categoria_producto=?,nombre_producto=?,descripcion=?,precio=?,stock_minimo=? WHERE id_producto=?")){
                        p.setInt(1,cat.id);p.setString(2,nombre.getText().trim());p.setString(3,descripcion.getText());p.setBigDecimal(4,precioValido());p.setInt(5,minimo());p.setInt(6,seleccionado.id);p.executeUpdate();
                    }
                }
            }
            limpiar();recargar();mensaje.setText("Producto guardado correctamente.");
        }catch(Exception e){error(e);}
    }
    @FXML private void cambiarEstado(){
        if(seleccionado==null){mensaje.setText("Selecciona un producto.");return;}
        try(Connection c=Conexion.getInstance().getConnection();PreparedStatement p=c.prepareStatement("UPDATE productos SET estado=? WHERE id_producto=?")){
            p.setBoolean(1,!seleccionado.activo);p.setInt(2,seleccionado.id);p.executeUpdate();recargar();
        }catch(SQLException e){error(e);}
    }
    @FXML private void crearCategoria(){
        TextInputDialog d=new TextInputDialog();d.setTitle("Nueva categoría");d.setHeaderText("Registrar categoría de confitería");d.setContentText("Nombre:");
        d.showAndWait().ifPresent(n->{
            if(n.isBlank())return;
            try(Connection c=Conexion.getInstance().getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO categorias_producto(nombre_categoria,descripcion,estado) VALUES(?, '', 1)")){
                p.setString(1,n.trim());p.executeUpdate();recargar();
            }catch(SQLException e){error(e);}
        });
    }
    @FXML private void agregarStock(){
        if(seleccionado==null){mensaje.setText("Selecciona un producto.");return;}
        TextInputDialog d=new TextInputDialog("1");d.setTitle("Entrada de inventario");d.setHeaderText("Producto: "+seleccionado.nombre);d.setContentText("Cantidad a ingresar:");
        d.showAndWait().ifPresent(n->{
            try{int q=Integer.parseInt(n.trim());if(q<=0)throw new IllegalArgumentException("Cantidad inválida.");
                var u=SesionContext.getUsuarioActual();if(u==null)throw new IllegalStateException("Sesión no iniciada.");
                try(Connection c=Conexion.getInstance().getConnection()){
                    c.setAutoCommit(false);
                    try(PreparedStatement p=c.prepareStatement("UPDATE productos SET stock=stock+? WHERE id_producto=?")){
                        p.setInt(1,q);p.setInt(2,seleccionado.id);p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO movimientos_inventario(id_producto,id_usuario,tipo_movimiento,cantidad,observacion) VALUES(?,?,'ENTRADA',?,'Entrada en confitería')")){
                        p.setInt(1,seleccionado.id);p.setInt(2,u.getIdUsuario());p.setInt(3,q);p.executeUpdate();
                    }
                    c.commit();
                }
                recargar();mensaje.setText("Entrada registrada.");
            }catch(Exception e){error(e);}
        });
    }
    @FXML private void actualizarTotal(){
        if(seleccionado==null){total.setText("Total: Q 0.00");return;}
        try{int q=Integer.parseInt(cantidad.getText().trim());total.setText("Total: Q "+seleccionado.precio.multiply(BigDecimal.valueOf(Math.max(q,0))));}
        catch(Exception e){total.setText("Cantidad inválida");}
    }
    @FXML private void vender(){
        if(seleccionado==null||clientes.getValue()==null){mensaje.setText("Selecciona un producto y un cliente.");return;}
        int q;
        try{q=Integer.parseInt(cantidad.getText().trim());if(q<=0)throw new NumberFormatException();}
        catch(NumberFormatException e){mensaje.setText("La cantidad debe ser mayor que cero.");return;}
        if(!seleccionado.activo){mensaje.setText("El producto está inactivo.");return;}
        var u=SesionContext.getUsuarioActual();if(u==null){mensaje.setText("Necesitas iniciar sesión.");return;}
        try(Connection c=Conexion.getInstance().getConnection()){
            c.setAutoCommit(false);
            try{
                int ventaId; BigDecimal importe;
                // Bloquea el producto: evita vender existencias que otra caja ya utilizó.
                try(PreparedStatement p=c.prepareStatement("SELECT precio,stock,estado FROM productos WHERE id_producto=? FOR UPDATE")){
                    p.setInt(1,seleccionado.id);
                    try(ResultSet r=p.executeQuery()){
                        if(!r.next()||!r.getBoolean("estado"))throw new SQLException("Producto no disponible.");
                        if(r.getInt("stock")<q)throw new SQLException("Stock insuficiente.");
                        importe=r.getBigDecimal("precio").multiply(BigDecimal.valueOf(q));
                    }
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO ventas(id_cliente,id_usuario,total,metodo_pago,estado,fecha_confirmacion) VALUES(?,?,?,'EFECTIVO','confirmada',NOW())",Statement.RETURN_GENERATED_KEYS)){
                    p.setInt(1,clientes.getValue().id);p.setInt(2,u.getIdUsuario());p.setBigDecimal(3,importe);p.executeUpdate();
                    try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("No se creó la venta.");ventaId=k.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO detalle_venta_productos(id_venta,id_producto,cantidad,precio_unitario,subtotal) VALUES(?,?,?,?,?)")){
                    p.setInt(1,ventaId);p.setInt(2,seleccionado.id);p.setInt(3,q);p.setBigDecimal(4,importe.divide(BigDecimal.valueOf(q)));p.setBigDecimal(5,importe);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("UPDATE productos SET stock=stock-? WHERE id_producto=?")){
                    p.setInt(1,q);p.setInt(2,seleccionado.id);p.executeUpdate();
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO movimientos_inventario(id_producto,id_usuario,id_venta,tipo_movimiento,cantidad,observacion) VALUES(?,?,?,'SALIDA',?,'Venta confitería')")){
                    p.setInt(1,seleccionado.id);p.setInt(2,u.getIdUsuario());p.setInt(3,ventaId);p.setInt(4,q);p.executeUpdate();
                }
                c.commit();recargar();mensaje.setText("Venta #"+ventaId+" registrada. Total: Q "+importe);
                FacturasConfiteria.mostrarFactura(ventaId);
            }catch(Exception ex){c.rollback();throw ex;}finally{c.setAutoCommit(true);}
        }catch(Exception e){error(e);}
    }
    @FXML private void buscarFacturas(){ FacturasConfiteria.buscarFacturas(); }
    @FXML private void volver() throws IOException {Principal.mostrarDashboardSegunRol();}
    private void error(Exception e){mensaje.setText("Error: "+e.getMessage());e.printStackTrace();}
}
