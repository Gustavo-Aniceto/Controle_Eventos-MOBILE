package com.example.crudapplication.ui.home;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.crudapplication.CaptureAct;
import com.example.crudapplication.MainActivity2;
import com.example.crudapplication.R;
import com.example.crudapplication.databinding.FragmentHomeBinding;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private SQLiteDatabase bancoDados;
    public ListView listViewDados;
    public ArrayList<Integer> arrayIds;
    public Integer idSelecionado;
    public Button btn_scan;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btn_scan = binding.btnScan;

        //final TextView textView = binding.textHome;
        //homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        listViewDados = (ListView) binding.listViewDados;


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanCode();
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

                Bundle bundle = new Bundle();
                bundle.putInt("ID_SELECIONADO", idSelecionado);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.nav_alter, bundle);

                Log.d("MeuApp", "Transação do Fragmento Executada");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        listarDados();
    }


    public void listarDados(){
        try {
            arrayIds = new ArrayList<>();
            Context contexto = getActivity();

            bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
            Cursor meuCursor = bancoDados.rawQuery("SELECT id, nome, cpf, telefone, data_inicial, data_final FROM coisa", null);
            ArrayList<String> linhas = new ArrayList<String>();
            ArrayAdapter meuAdapter = new ArrayAdapter<String>(
                    requireContext(),
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


    public void confirmaExcluir() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(requireContext());
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
            Context contexto = getActivity();

            bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
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

    private void scanCode() {
        try {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to flash on");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(options);
        } catch (Exception e) {
            Log.e("MeuLog", "Error in scanCode", e);
        }
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        try {
            Integer id = 0;
            if (result.getContents() != null) {
                try {
                    String substring = result.getContents().substring(0, result.getContents().indexOf(":")).trim();

                    Pattern pattern = Pattern.compile("Id: (\\d+)");
                    Matcher matcher = pattern.matcher(result.getContents());

                    if (matcher.find()) {
                        String valorNumerico = matcher.group(1);
                        id = Integer.parseInt(valorNumerico);
                        // Restante do código
                    } else {
                        Log.e("MeuLog", "Padrão não encontrado na string: " + result.getContents());
                    }
                    // Restante do código
                } catch (NumberFormatException e) {
                    Log.e("MeuLog", "Erro ao converter para número", e);
                }

                try{
                    Context contexto = getActivity();
                    Date now = new Date();

                    bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
                    String sql = "UPDATE coisa SET data_final=? WHERE id=?";
                    SQLiteStatement stmt = bancoDados.compileStatement(sql);
                    stmt.bindString(1,now.toString());
                    stmt.bindLong(2,id);
                    int rowsAffected = stmt.executeUpdateDelete();

                    if (rowsAffected > 0) {
                        // A atualização foi bem-sucedida
                        Log.d("MeuLog", "Update successful for ID: " + id);
                    } else {
                        // Nenhuma linha foi afetada pela atualização
                        Log.d("MeuLog", "No rows affected for ID: " + id);
                    }
                    bancoDados.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }
        } catch (Exception e) {
            Log.e("MeuLog", "Error in barLauncher", e);
        }
    });


}