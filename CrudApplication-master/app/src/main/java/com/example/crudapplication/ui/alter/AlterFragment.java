package com.example.crudapplication.ui.alter;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.crudapplication.R;
import com.example.crudapplication.databinding.FragmentAlterBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.core.app.NotificationCompat;

public class AlterFragment extends Fragment {

    private FragmentAlterBinding binding;
    private SQLiteDatabase bancoDados;
    public Button buttonAlterar;
    public EditText editTextNome;
    public EditText editTextCpf;
    public EditText editTextTelefone;
    public ImageView iv_qr;
    public Integer id;
    public Button buttonCancelar;
    public ImageView foto_user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentAlterBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Recupera os argumentos passados
        Bundle args = getArguments();
        if (args != null) {
            id = args.getInt("ID_SELECIONADO", 1); // defaultValue é o valor padrão, caso não seja encontrado
            // Agora você tem o idSelecionado para usar no fragmento
        }

        buttonAlterar = (Button) binding.buttonAlterar;
        editTextNome = (EditText) binding.editTextNome;
        editTextCpf = (EditText) binding.CpfEdit;
        editTextTelefone = (EditText) binding.TelefoneEdit;
        iv_qr = (ImageView) binding.ivQr;
        foto_user  = (ImageView) binding.fotoUser;
        buttonCancelar = binding.buttonCancelar;

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.nav_home);
            }
        });


        carregarDados();

        generateQR();

        buttonAlterar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alterar();
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void carregarDados(){
        try {
            Context contexto = getActivity();

            bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
            Cursor cursor = bancoDados.rawQuery("SELECT id, nome, cpf, telefone, imagem FROM coisa WHERE id = " + id.toString(), null);
            cursor.moveToFirst();
            //id = cursor.getInt(0);
            editTextNome.setText(cursor.getString(1));
            editTextCpf.setText(cursor.getString(2));
            editTextTelefone.setText(cursor.getString(3));
            Log.d("MeuLog", cursor.getString(4));
            //foto_user.setImageBitmap(new BitmapDrawable(getResources(), cursor.getString(4)));

            String imageName = cursor.getString(4);
            File imageFile = new File(contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);

// ...

            Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getPath());
            foto_user.setImageBitmap(imageBitmap);



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
            Context contexto = getActivity();

            bancoDados = contexto.openOrCreateDatabase("crudapp", Context.MODE_PRIVATE, null);
            String sql = "UPDATE coisa SET nome=?, cpf=?, telefone=? WHERE id=?";
            SQLiteStatement stmt = bancoDados.compileStatement(sql);
            stmt.bindString(1,valueNome);
            stmt.bindString(2,valueCpf);
            stmt.bindString(3,valueTelefone);
            stmt.bindLong(4,id);
            long id = stmt.executeUpdateDelete();
            bancoDados.close();

            if (id != -1) {  // Verificar se a inserção foi bem-sucedida (ID válido)
                NavController navController = NavHostFragment.findNavController(this);
                navController.navigate(R.id.nav_home);  // Substitua R.id.nav_alter pelo ID do seu fragmento de destino
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Constants {
        public static final int CHANNEL_ID = 213213;
        public static final int NOTIFICATION_ID = 1; // Change this to a unique ID for each notification
    }

    private void generateQR() {
        String valueNome, valueCpf, valueTelefone;
        valueNome = editTextNome.getText().toString();
        valueCpf = editTextCpf.getText().toString();
        valueTelefone = editTextTelefone.getText().toString();

        String text = "Id: " + id + "\nNome: " + valueNome + " CPF: " + valueCpf + " Telefone: " + valueTelefone;
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(matrix);
            // set data image to imageview
            iv_qr.setImageBitmap(bitmap);

            Context contexto = getActivity();

            File file = new File(contexto.getExternalFilesDir(null), "qrcode.pdf");

            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 600, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();
                Paint paint = new Paint();
                canvas.drawBitmap(bitmap, 0, 0, paint);  // Use paint para ajustar a escala se necessário
                document.finishPage(page);
                document.writeTo(outputStream);
                document.close();
                outputStream.flush();
                outputStream.close();

                // Get the file path
                String filePath = file.getAbsolutePath();

                // Abrir o arquivo PDF usando um Intent
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = FileProvider.getUriForFile(contexto, "com.example.crudapplication.provider", file);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Se não houver um visualizador de PDF instalado, avise o usuário ou forneça um link para baixar um.
                    Toast.makeText(contexto, "Nenhum visualizador de PDF encontrado", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(contexto, "Erro ao criar o arquivo PDF", Toast.LENGTH_SHORT).show();
            }



        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}