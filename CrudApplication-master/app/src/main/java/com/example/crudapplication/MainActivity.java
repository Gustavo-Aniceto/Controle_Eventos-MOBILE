package com.example.crudapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase bancoDados;
    public ListView listViewDados;
    public Button botao;
    public Button btn_scan;
    public ArrayList<Integer> arrayIds;
    public Integer idSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        criarBancoDados();

        listViewDados = (ListView) findViewById(R.id.listViewDados);
        botao = (Button) findViewById(R.id.buttonAlterar);

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirTelaCadastro();
            }
        });

        listViewDados.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                idSelecionado = arrayIds.get(i);
                confirmaExcluir();
                return true;
            }
        });

        listViewDados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                idSelecionado = arrayIds.get(i);
                abrirTelaAlterar();
            }
        });

        btn_scan =findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->
        {
            scanCode();
        });


        //inserirDadosTemp();
        listarDados();
    }

    @Override
    protected void onResume(){
        super.onResume();
        listarDados();
    }

    public void criarBancoDados(){
        try {
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            bancoDados.execSQL("DROP TABLE IF EXISTS coisa");
            bancoDados.execSQL("CREATE TABLE IF NOT EXISTS coisa(" +
                    " id INTEGER PRIMARY KEY AUTOINCREMENT" +
                    " , nome VARCHAR" +
                    " , cpf VARCHAR" +
                    " , telefone VARCHAR" +
                    " , imagem VARCHAR" +
                    " , data_inicial datetime" +
                    " , data_final datetime" +
                    ")");
            bancoDados.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listarDados(){
        try {
            arrayIds = new ArrayList<>();
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            Cursor meuCursor = bancoDados.rawQuery("SELECT id, nome, cpf, telefone, data_inicial, data_final FROM coisa", null);
            ArrayList<String> linhas = new ArrayList<String>();
            ArrayAdapter meuAdapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_list_item_1,
                    android.R.id.text1,
                    linhas
            );
            listViewDados.setAdapter(meuAdapter);
            meuCursor.moveToFirst();
            while(meuCursor!=null){
                linhas.add("NOME: " + meuCursor.getString(1) + " \nCPF: " + meuCursor.getString(2) + " \nTELEFONE: " + meuCursor.getString(3)+ " \nDATAS: " + meuCursor.getString(4)+ " - " + meuCursor.getString(5));
                arrayIds.add(meuCursor.getInt(0));
                meuCursor.moveToNext();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void inserirDadosTemp(){
        try{
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            String sql = "INSERT INTO coisa (nome, cpf, telefone) VALUES (?, ?, ?)";
            SQLiteStatement stmt = bancoDados.compileStatement(sql);

            stmt.bindString(1, "Coisa 1");
            stmt.bindString(2, "4535345345");
            stmt.bindString(3, "213123123");
            stmt.executeInsert();

            stmt.bindString(1, "Coisa 2");
            stmt.bindString(2, "12312323");
            stmt.bindString(3, "4564564646");
            stmt.executeInsert();

            stmt.bindString(1, "Coisa 3");
            stmt.bindString(2, "2342342334");
            stmt.bindString(3, "56757567567");
            stmt.executeInsert();

            bancoDados.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirTelaCadastro(){
        Intent intent = new Intent(this,CadastroActivity.class);
        startActivity(intent);
    }

    public void confirmaExcluir() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(MainActivity.this);
        msgBox.setTitle("Excluir");
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage("Você realmente deseja excluir esse registro?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                excluir();
                listarDados();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        msgBox.show();
    }

    public void excluir(){
        //Toast.makeText(this, i.toString(), Toast.LENGTH_SHORT).show();
        try{
            bancoDados = openOrCreateDatabase("crudapp", MODE_PRIVATE, null);
            String sql = "DELETE FROM coisa WHERE id =?";
            SQLiteStatement stmt = bancoDados.compileStatement(sql);
            stmt.bindLong(1, idSelecionado);
            stmt.executeUpdateDelete();
            listarDados();
            bancoDados.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void abrirTelaAlterar(){
        Intent intent = new Intent(this, AlterarActivity.class);
        intent.putExtra("id",idSelecionado);
        startActivity(intent);
    }

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        //barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });
}