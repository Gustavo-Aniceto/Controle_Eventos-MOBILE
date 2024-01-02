package com.example.crudapplication.ui.create;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.crudapplication.R;
import com.example.crudapplication.databinding.FragmentCreateBinding;
import com.example.crudapplication.ui.home.HomeFragment;

import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CreateFragment extends Fragment {

    private FragmentCreateBinding binding;
    EditText editTextNome;
    EditText editTextCpf;
    EditText editTextTelefone;
    Button botao;
    SQLiteDatabase bancoDados;
    private static final int REQUEST_CODE_CAMERA = 1;
    private String img_name;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);




        binding = FragmentCreateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        editTextNome = (EditText) binding.editTextNome;
        editTextCpf = (EditText) binding.editTextCpf;
        editTextTelefone = (EditText) binding.editTextTelefone;
        botao = (Button) binding.buttonAlterar;

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrar();
            }
        });

        Button btnTirarFoto = binding.btnTirarFoto;
        btnTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraActivity();
            }
        });

        return root;
    }

    private void startCameraActivity() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, start the camera activity
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else {
            // Request the camera permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the camera activity
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } else {
                // Permission denied, display a toast message
                Toast.makeText(getActivity(), "Permissão de câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            // Get the taken photo
            Bitmap foto = (Bitmap) data.getExtras().get("data");

            // Save the photo
            salvarFoto(foto);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private void salvarFoto(Bitmap foto) {
        // Obter a data e hora atuais
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String dataHora = sdf.format(calendar.getTime());

        // Criar um nome para a foto
        String nomeFoto = "foto_" + dataHora + ".jpg";

        img_name = nomeFoto;

        // Criar um arquivo para salvar a foto
        File arquivo = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), nomeFoto);

        // Criar um `FileOutputStream` para o arquivo
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            // Salvar a foto
            foto.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cadastrar(){
        if(!TextUtils.isEmpty(editTextNome.getText().toString()) || !TextUtils.isEmpty(editTextCpf.getText().toString()) || !TextUtils.isEmpty(editTextTelefone.getText().toString())){
            try{
                Date now = new Date();
                Context contexto = getActivity();

                bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
                String sql = "INSERT INTO coisa (nome, cpf, telefone, imagem, data_inicial) VALUES (?, ?, ?, ?, ?)";
                SQLiteStatement stmt = bancoDados.compileStatement(sql);
                stmt.bindString(1,editTextNome.getText().toString());
                stmt.bindString(2,editTextCpf.getText().toString());
                stmt.bindString(3,editTextTelefone.getText().toString());
                stmt.bindString(4,img_name);
                stmt.bindString(5,now.toString());
                //stmt.executeInsert();
                long id = stmt.executeInsert();
                bancoDados.close();

                if (id != -1) {  // Verificar se a inserção foi bem-sucedida (ID válido)
                    NavController navController = NavHostFragment.findNavController(this);
                    navController.navigate(R.id.nav_home);  // Substitua R.id.nav_alter pelo ID do seu fragmento de destino
                }

                //finish();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }





}