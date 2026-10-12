package com.example.procrastinot.views;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.procrastinot.R;

public class MultimediaActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private VideoView videoConsejo;
    private ImageView imgFoto;

    private final ActivityResultLauncher<Void> tomarFoto =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    (Bitmap foto) -> {
                        if (foto != null) {
                            imgFoto.setImageBitmap(foto);
                        } else {
                            Toast.makeText(
                                    this,
                                    "No se tomó ninguna foto",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

    private final ActivityResultLauncher<String> pedirPermisoCamara =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    concedido -> {
                        if (concedido) {
                            abrirCamara();
                        } else {
                            Toast.makeText(
                                    this,
                                    "Permiso denegado. Puedes seguir usando los consejos. "
                                            + "Para habilitar la cámara, revisa los permisos "
                                            + "de la app en Ajustes.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_multimedia);

        imgFoto = findViewById(R.id.imgFoto);

        findViewById(R.id.btnFoto).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {

                abrirCamara();

            } else if (shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
            )) {

                new AlertDialog.Builder(this)
                        .setTitle("Permiso de cámara")
                        .setMessage(
                                "La cámara permite tomar una foto de tu espacio de trabajo."
                        )
                        .setPositiveButton("Continuar", (dialog, which) ->
                                pedirPermisoCamara.launch(
                                        Manifest.permission.CAMERA
                                )
                        )
                        .setNegativeButton("Cancelar", null)
                        .show();

            } else {
                pedirPermisoCamara.launch(Manifest.permission.CAMERA);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.scrollMultimedia),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        ImageView imgConsejo = findViewById(R.id.imgConsejo);
        Button btnAnimar = findViewById(R.id.btnAnimar);
        Button btnAudio = findViewById(R.id.btnAudio);
        Button btnVideo = findViewById(R.id.btnVideo);
        Button btnVolver = findViewById(R.id.btnVolver);

        videoConsejo = findViewById(R.id.videoConsejo);

        // Animación de aparición de la imagen
        btnAnimar.setOnClickListener(v ->
                imgConsejo.startAnimation(
                        AnimationUtils.loadAnimation(
                                this,
                                R.anim.fade_in
                        )
                )
        );

        // Audio: cada pulsación lo reproduce desde el principio
        btnAudio.setOnClickListener(v -> {
            videoConsejo.pause();
            liberarAudio();

            mediaPlayer = MediaPlayer.create(
                    this,
                    R.raw.consejo_audio
            );

            if (mediaPlayer == null) {
                Toast.makeText(
                        this,
                        "No se pudo abrir el audio",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            mediaPlayer.setOnCompletionListener(player ->
                    liberarAudio()
            );

            mediaPlayer.setOnErrorListener((player, what, extra) -> {
                liberarAudio();

                Toast.makeText(
                        this,
                        "No se pudo reproducir el audio",
                        Toast.LENGTH_SHORT
                ).show();

                return true;
            });

            mediaPlayer.start();
        });

        // Controles para pausar y avanzar el video.
        MediaController controles = new MediaController(this);
        controles.setAnchorView(videoConsejo);
        videoConsejo.setMediaController(controles);

        Uri videoUri = Uri.parse(
                "android.resource://" + getPackageName()
                        + "/" + R.raw.consejo_video
        );

        videoConsejo.setVideoURI(videoUri);

        videoConsejo.setOnErrorListener((player, what, extra) -> {
            Toast.makeText(
                    this,
                    "No se pudo reproducir el video",
                    Toast.LENGTH_SHORT
            ).show();

            return true;
        });

        // Video: cada pulsación lo reproduce desde el principio.
        btnVideo.setOnClickListener(v -> {
            liberarAudio();
            videoConsejo.seekTo(0);
            videoConsejo.start();
        });

        // Regresar a la pantalla anterior.
        btnVolver.setOnClickListener(v -> finish());
    }

    private void liberarAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        liberarAudio();

        if (videoConsejo != null) {
            videoConsejo.pause();
        }
    }

    @Override
    protected void onDestroy() {
        liberarAudio();

        if (videoConsejo != null) {
            videoConsejo.stopPlayback();
        }

        super.onDestroy();
    }

    private void abrirCamara() {
        try {
            tomarFoto.launch(null);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    this,
                    "No hay una aplicación de cámara disponible",
                    Toast.LENGTH_SHORT
            ).show();
        } catch (SecurityException e) {
            Toast.makeText(
                    this,
                    "No se pudo acceder a la cámara. Revisa el permiso.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}