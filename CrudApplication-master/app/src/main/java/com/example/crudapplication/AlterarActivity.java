package com.example.crudapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class AlterarActivity extends AppCompatActivity {
    private SQLiteDatabase bancoDados;
    public Button buttonAlterar;
    public EditText editTextNome;
    public EditText editTextCpf;
    public EditText editTextTelefone;
    public ImageView iv_qr;
    public ImageView foto_user;
    public Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alterar);

        buttonAlterar = (Button) findViewById(R.id.buttonAlterar);
        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextCpf = (EditText) findViewById(R.id.CpfEdit);
        editTextTelefone = (EditText) findViewById(R.id.TelefoneEdit);
        iv_qr = findViewById(R.id.iv_qr);

        Intent intent = getIntent();
        id = intent.getIntExtra("id",0);

        carregarDados();

        generateQR();

        buttonAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alterar();
            }
        });

    }

    public void carregarDados(){
        try {
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            Cursor cursor = bancoDados.rawQuery("SELECT id, nome, cpf, telefone FROM coisa WHERE id = " + id.toString(), null);
            cursor.moveToFirst();
            editTextNome.setText(cursor.getString(1));
            editTextCpf.setText(cursor.getString(2));
            editTextTelefone.setText(cursor.getString(3));



        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void alterar(){
        String valueNome, valueCpf, valueTelefone;
        valueNome = editTextNome.getText().toString();
        valueCpf = editTextCpf.getText().toString();
        valueTelefone = editTextTelefone.getText().toString();
        try{
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            String sql = "UPDATE coisa SET nome=?, cpf=?, telefone=? WHERE id=?";
            SQLiteStatement stmt = bancoDados.compileStatement(sql);
            stmt.bindString(1,valueNome);
            stmt.bindString(2,valueCpf);
            stmt.bindString(3,valueTelefone);
            stmt.bindLong(4,id);
            stmt.executeUpdateDelete();
            bancoDados.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    private void generateQR()
    {
        String valueNome, valueCpf, valueTelefone;
        valueNome = editTextNome.getText().toString();
        valueCpf = editTextCpf.getText().toString();
        valueTelefone = editTextTelefone.getText().toString();

        String text = "Nome: " + valueNome + " CPF: " + valueCpf + " Telefone: " + valueTelefone;
        MultiFormatWriter writer = new MultiFormatWriter();
        try
        {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE,600,600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            //set data image to imageview
            iv_qr.setImageBitmap(bitmap);

        } catch (WriterException e)
        {
            e.printStackTrace();
        }
    }
}