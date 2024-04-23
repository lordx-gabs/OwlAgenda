package com.example.owlagenda.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "usuario")
public class Usuario implements Serializable {

    @PrimaryKey
    private @NonNull String id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "sobrenome")
    private String sobrenome;

    @ColumnInfo(name = "senha")
    private String senha;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "url_foto_perfil")
    private String url_foto_perfil;

    public Usuario() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl_foto_perfil() {
        return url_foto_perfil;
    }

    public void setUrl_foto_perfil(String url_foto_perfil) {
        this.url_foto_perfil = url_foto_perfil;
    }

}
