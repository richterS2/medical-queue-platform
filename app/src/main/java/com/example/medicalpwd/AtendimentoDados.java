package com.example.medicalpwd;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class AtendimentoDados implements Serializable {

    private String codigo;
    private String descricao;
    private String identificador;


    public AtendimentoDados(String codigo, String descricao, String identificador)
    {
        this.codigo = codigo;
        this.descricao = descricao;
        this.identificador = identificador;
    }

    public String getCodigo()
    {
        return codigo;
    }
    public String getDescricao()
    {
        return descricao;
    }
    public String getIdentificador()
    {
        return identificador;
    }


    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("codigo", codigo);
            obj.put("descricao", descricao);
            obj.put("identificador", identificador );
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

}