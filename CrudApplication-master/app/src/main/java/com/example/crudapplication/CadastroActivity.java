package com.example.crudapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

public class CadastroActivity extends AppCompatActivity {
    EditText editTextNome;
    EditText editTextCpf;
    EditText editTextTelefone;
    Button botao;
    SQLiteDatabase bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextCpf = (EditText) findViewById(R.id.editTextCpf);
        editTextTelefone = (EditText) findViewById(R.id.editTextTelefone);
        botao = (Button) findViewById(R.id.buttonAlterar);

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrar();
            }
        });
    }

    public void cadastrar(){
        if(!TextUtils.isEmpty(editTextNome.getText().toString()) || !TextUtils.isEmpty(editTextCpf.getText().toString()) || !TextUtils.isEmpty(editTextTelefone.getText().toString())){
            try{
                Date now = new Date();
                bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
                String sql = "INSERT INTO coisa (nome, cpf, telefone, data_inicial) VALUES (?, ?, ?, ?)";
                SQLiteStatement stmt = bancoDados.compileStatement(sql);
                stmt.bindString(1,editTextNome.getText().toString());
                stmt.bindString(2,editTextCpf.getText().toString());
                stmt.bindString(3,editTextTelefone.getText().toString());
                stmt.bindString(4,now.toString());
                stmt.executeInsert();
                bancoDados.close();
                finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}