package org.cine.dao;
import java.sql.SQLException;import java.util.List;import org.cine.model.MovimientoInventario;
public interface MovimientoInventarioDAO {void registrar(int producto,int usuario,String tipo,int cantidad,String observacion)throws SQLException;List<MovimientoInventario> listar()throws SQLException;}
