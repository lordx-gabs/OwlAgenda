package com.example.owlagenda.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.owlagenda.data.models.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    void insert(Usuario user);

    @Update
    void update(Usuario user);

    @Delete
    void delete(Usuario user);

    @Query("SELECT * FROM usuario WHERE id = :id")
    Usuario usuarioPorId(String id);

    @Query("SELECT * FROM usuario")
    List<Usuario> listarTodosUsuarios();

    @Query("SELECT * FROM usuario WHERE email = :email AND senha = :senha")
    Usuario buscarPorEmailESenha(String email, String senha);
}

