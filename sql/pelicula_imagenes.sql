USE cinedb_in4cm;

-- Imagen del poster almacenada directamente en MySQL.
ALTER TABLE peliculas
ADD COLUMN imagen LONGBLOB NULL AFTER sinopsis;

DROP PROCEDURE IF EXISTS sp_insertarpelicula;
DROP PROCEDURE IF EXISTS sp_listarpeliculas;
DROP PROCEDURE IF EXISTS sp_buscarpelicula;
DROP PROCEDURE IF EXISTS sp_actualizarpelicula;

DELIMITER $$

CREATE PROCEDURE sp_insertarpelicula(
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(20),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _imagen LONGBLOB,
    IN _id_genero INT
)
BEGIN
    INSERT INTO peliculas (
        titulo,
        sinopsis,
        director,
        duracion_minutos,
        clasificacion,
        idioma,
        fecha_estreno,
        imagen,
        id_genero
    )
    VALUES (
        _titulo,
        _sinopsis,
        _director,
        _duracion_minutos,
        _clasificacion,
        _idioma,
        _fecha_estreno,
        _imagen,
        _id_genero
    );

    SELECT LAST_INSERT_ID() AS id_pelicula;
END $$

CREATE PROCEDURE sp_listarpeliculas()
BEGIN
    SELECT
        id_pelicula,
        titulo,
        sinopsis,
        director,
        duracion_minutos,
        clasificacion,
        idioma,
        fecha_estreno,
        imagen,
        id_genero,
        estado
    FROM peliculas
    ORDER BY id_pelicula;
END $$

CREATE PROCEDURE sp_buscarpelicula(IN _id_pelicula INT)
BEGIN
    SELECT
        id_pelicula,
        titulo,
        sinopsis,
        director,
        duracion_minutos,
        clasificacion,
        idioma,
        fecha_estreno,
        imagen,
        id_genero,
        estado
    FROM peliculas
    WHERE id_pelicula = _id_pelicula;
END $$

CREATE PROCEDURE sp_actualizarpelicula(
    IN _id_pelicula INT,
    IN _titulo VARCHAR(150),
    IN _sinopsis TEXT,
    IN _director VARCHAR(150),
    IN _duracion_minutos INT,
    IN _clasificacion VARCHAR(20),
    IN _idioma VARCHAR(50),
    IN _fecha_estreno DATE,
    IN _imagen LONGBLOB,
    IN _id_genero INT
)
BEGIN
    IF EXISTS (
        SELECT 1
        FROM peliculas p
        JOIN funciones f ON f.id_pelicula = p.id_pelicula
        WHERE p.id_pelicula = _id_pelicula
          AND p.duracion_minutos <> _duracion_minutos
          AND f.estado = 'programada'
          AND f.fecha_fin > NOW()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Reprogramar las funciones antes de cambiar la duración.';
    END IF;

    UPDATE peliculas
    SET titulo = _titulo,
        sinopsis = _sinopsis,
        director = _director,
        duracion_minutos = _duracion_minutos,
        clasificacion = _clasificacion,
        idioma = _idioma,
        fecha_estreno = _fecha_estreno,
        imagen = _imagen,
        id_genero = _id_genero
    WHERE id_pelicula = _id_pelicula;
END $$

DELIMITER ;
