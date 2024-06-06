package com.example.owlagenda.data.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.owlagenda.data.models.User;

import java.util.List;

@Dao
public interface UsuarioDao {
    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM USUARIO WHERE id = :id")
    User usuarioPorId(String id);

    @Query("SELECT * FROM USUARIO")
    List<User> listarTodosUsuarios();

    @Query("SELECT * FROM USUARIO WHERE email = :email AND senha = :senha")
    User searchEmailAndPassword(String email, String senha);
}


