package com.example.simplecalls;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

public class MainActivity extends AppCompatActivity {

    private EditText phoneInput;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private SharedPreferences prefs;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Находим DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // 2. Находим кнопку, по которой будем открывать меню
        Button menuButton = findViewById(R.id.rightCornerMenu);

        // 3. Устанавливаем слушатель нажатия
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открываем левую панель (start)
                // GravityCompat.START — это правильный способ указать "левую" сторону,
                // который также учитывает языки с письмом справа налево.
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phoneInput = findViewById(R.id.phone_input);

        // Открываем SharedPreferences (файл "simple_calls_prefs")
        prefs = getSharedPreferences(getString(R.string.prefs_name), Context.MODE_PRIVATE);

        // При старте: если сохранён номер — подставляем в поле
        String savedNumber = prefs.getString(getString(R.string.prefs_key_phone), "");
        if (!savedNumber.isEmpty()) {
            phoneInput.setText(savedNumber);
            // Курсор в конец текста
            phoneInput.setSelection(savedNumber.length());
        }

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        makeCall();
                    } else {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        Button callButton = findViewById(R.id.my_button);
        callButton.setOnClickListener(v -> {
            String number = phoneInput.getText().toString().trim();

            if (number.isEmpty()) {
                Toast.makeText(this, R.string.empty_phone, Toast.LENGTH_SHORT).show();
                return;
            }

            // Сохраняем номер при каждом нажатии "Call"
            savePhone(number);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
            }
        });


        Button menu = findViewById(R.id.rightCornerMenu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Shamssain");
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Дополнительно сохраняем при уходе из приложения,
        // даже если пользователь не нажал "Call" — вдруг он просто набрал номер
        String number = phoneInput.getText().toString().trim();
        if (!number.isEmpty()) {
            savePhone(number);
        }
    }

    private void savePhone(String number) {
        prefs.edit()
                .putString(getString(R.string.prefs_key_phone), number)
                .apply();  // apply() — асинхронно и быстрее, чем commit()
    }

    private void makeCall() {
        String number = phoneInput.getText().toString().trim();
        if (number.isEmpty()) {
            Toast.makeText(this, R.string.empty_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        // Сохраняем на случай, если звонок пойдёт после диалога разрешения
        savePhone(number);

        String uri = number.startsWith("tel:") ? number : "tel:" + number;
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));

        if (callIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(callIntent);
        } else {
            Toast.makeText(this, R.string.no_phone_app, Toast.LENGTH_SHORT).show();
        }
    }
}