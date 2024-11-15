package com.example.owlagenda.ui.taskdetails;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.owlagenda.R;
import com.example.owlagenda.data.models.School;
import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.databinding.ActivityTaskDetailsViewBinding;
import com.example.owlagenda.ui.updatetask.UpdateTaskView;
import com.example.owlagenda.util.NotificationUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class TaskDetailsView extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 400;
    private ActivityTaskDetailsViewBinding binding;
    private TaskDetailsViewModel viewModel;
    private DocumentTaskAdapter adapter;
    private String taskId;
    private int positionDocument;
    private Task task;
    private Handler handler;
    private Runnable runnable;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTaskDetailsViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);

        viewModel = new ViewModelProvider(this).get(TaskDetailsViewModel.class);

        taskId = getIntent().getStringExtra("taskId");

        viewModel.getIsLoading().observe(this, aBoolean -> {
            if (aBoolean) {
                binding.loadingTaskDetails.setVisibility(View.VISIBLE);
            }
        });

        binding.materialToolbar.inflateMenu(R.menu.menu_task_details);

        binding.materialToolbar.setOnMenuItemClickListener(item -> {
            if(item.getItemId() == R.id.menu_edit_task) {
                if(binding.loadingTaskDetails.getVisibility() == View.GONE) {
                    Intent intentEditTask = new Intent(this, UpdateTaskView.class);
                    intentEditTask.putExtra("taskId", taskId);
                    startActivity(intentEditTask);
                    finish();
                } else {
                    Toast.makeText(this, "Carregando tarefa aguarde...", Toast.LENGTH_SHORT).show();
                }
            } else if (item.getItemId() == R.id.menu_delete_task) {
                if(binding.loadingTaskDetails.getVisibility() == View.GONE) {
                    viewModel.deleteTask(task).observe(this, aBoolean -> {
                        if (aBoolean) {
                            snackbar = Snackbar.make(binding.getRoot(), "Tarefa Excluída",
                                    Snackbar.LENGTH_SHORT).setAction("Desfazer", v3 ->
                                    viewModel.addTask(task).observe(this, aBoolean1 -> {
                                        if (aBoolean1) {
                                            Toast.makeText(this, "Tarefa restaurada com sucesso", Toast.LENGTH_SHORT).show();
                                        } else {
                                            finish();
                                            Toast.makeText(this, "Falha ao restaurar tarefa", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                            );

                            snackbar.addCallback(new Snackbar.Callback() {
                                @Override
                                public void onDismissed(Snackbar snackbar, int event) {
                                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                                            event == Snackbar.Callback.DISMISS_EVENT_MANUAL ||
                                            event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                        int notificationId = 0;
                                        try {
                                            notificationId = Integer.parseInt(task.getId().replaceAll("[^0-9]", ""));
                                        } catch (NumberFormatException ignored) {
                                        }

                                        Log.d("teste", "" + notificationId);
                                        if (NotificationUtil.scheduleNotificationApp.isAlarmSet(TaskDetailsView.this,
                                                task.getTitle(), notificationId)) {
                                            NotificationUtil.scheduleNotificationApp
                                                    .cancelNotification(TaskDetailsView.this, task.getTitle(),
                                                            notificationId);
                                            Log.d("testeee", "chegouu");
                                        }
                                        finish();
                                    }
                                    binding.loadingTaskDetails.setVisibility(View.GONE);
                                }
                            });

                            snackbar.show();
                        } else {
                            Toast.makeText(this, "Falha ao deletar tarefa", Toast.LENGTH_SHORT).show();
                        }
                        binding.loadingTaskDetails.setVisibility(View.GONE);
                    });
                } else {
                    Toast.makeText(this, "Carregando tarefa aguarde...", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });

        viewModel.getErrorMessage().observe(this, s ->
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.materialToolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        binding.recycleDocumentTask.setLayoutManager(new LinearLayoutManager(this
                , LinearLayoutManager.HORIZONTAL, false));
        viewModel.getTaskById(taskId).observe(this, task -> {
            if (task != null) {
                this.task = task;
                task.getSchool().get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        binding.schoolTaskDetails.setText("Escola: " + task1.getResult()
                                .toObject(School.class).getSchoolName());
                        task.getSchoolClass().get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful()) {
                                binding.classTaskDetails.setText("Turma: " + task2.getResult()
                                        .toObject(SchoolClass.class).getClassName());
                                binding.titleTaskDetails.setText("Título: " + task.getTitle());
                                binding.dateTaskDetails.setText("Data: " + task.getDate());
                                if (!task.getDescription().isEmpty()) {
                                    binding.descriptionTaskDetails.setText("Descrição: " + task.getDescription());
                                } else {
                                    binding.descriptionTaskDetails.setVisibility(View.GONE);
                                }
                                binding.completeTask.setChecked(task.isCompleted());
                                binding.tagTaskDetails.setText("Tag: " + task.getTag());
                                if (task.getTaskDocuments() != null && !task.getTaskDocuments().isEmpty()) {
                                    binding.recycleDocumentTask.setVisibility(View.VISIBLE);
                                    binding.tvClickDownload.setVisibility(View.VISIBLE);
                                    binding.emptyView.setVisibility(View.GONE);
                                    adapter = new DocumentTaskAdapter(position -> {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(task.getTaskDocuments().get(position).getUri()));
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        try {
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            positionDocument = position;
                                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                        != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(this,
                                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                                                } else {
                                                    startDownload();
                                                }
                                            } else {
                                                downloadFile(task.getTaskDocuments().get(position).getUrl(), task.getTaskDocuments().get(position).getName());
                                            }
                                        }

                                    }, task.getTaskDocuments());
                                    binding.recycleDocumentTask.setAdapter(adapter);
                                } else {
                                    binding.recycleDocumentTask.setVisibility(View.GONE);
                                    binding.tvClickDownload.setVisibility(View.GONE);
                                    binding.emptyView.setVisibility(View.VISIBLE);
                                }
                                binding.loadingTaskDetails.setVisibility(View.GONE);
                            } else {
                                binding.loadingTaskDetails.setVisibility(View.GONE);
                                finish();
                                Toast.makeText(this, "Erro ao carregar turma", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        binding.loadingTaskDetails.setVisibility(View.GONE);
                        finish();
                        Toast.makeText(this, "Erro ao carregar escola", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                binding.loadingTaskDetails.setVisibility(View.GONE);
                finish();
                Toast.makeText(this, "Erro ao carregar tarefa", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openDownloadedFile(String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        Uri fileUri = Uri.fromFile(file); // ou use FileProvider se o arquivo for sensível

        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = getMimeType(file.getAbsolutePath()); // Determina o tipo MIME com base no nome do arquivo
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Não há um aplicativo para abrir este arquivo.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String fileName) {
        String mimeType = "*/*"; // Tipo MIME genérico

        // Extraindo a extensão do arquivo
        String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(fileName)).toString());
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }

        return mimeType;
    }

    private void downloadFile(String fileUrl, String fileName) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileUrl));

        request.setDestinationInExternalFilesDir(this, getExternalCacheDir().getAbsolutePath(), fileName);

        request.setTitle("Baixando " + fileName);
        request.setDescription("Aguarde...");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        // Verifica o status do download
        new Thread(() -> {
            boolean isDownloaded = false;
            while (!isDownloaded) {
                // Verifica o status do download
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);

                        if (statusColumnIndex >= 0) { // Verifica se o índice é válido
                            int status = cursor.getInt(statusColumnIndex);
                            int downloadedColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                            int totalBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                            // Aqui você pode usar o status como quiser
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                isDownloaded = true;
                                runOnUiThread(() -> {
                                    adapter.getDocuments().get(positionDocument).setLoading(false);
                                    adapter.notifyItemChanged(positionDocument);
                                    openDownloadedFile(downloadId);
                                });
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                isDownloaded = true;
                                runOnUiThread(() -> Toast.makeText(this, "Erro ao baixar o arquivo", Toast.LENGTH_SHORT).show());
                            } else if (status == DownloadManager.STATUS_RUNNING) {
                                int bytesDownloaded = cursor.getInt(downloadedColumnIndex);
                                int totalBytes = cursor.getInt(totalBytesColumnIndex);
                                if (totalBytes > 0) {
                                    int progress = (int) ((bytesDownloaded * 100L) / totalBytes);
                                    Log.d("DownloadProgress", "Progresso: " + progress + "%");
                                    runOnUiThread(() -> {
                                        adapter.getDocuments().get(positionDocument).setLoading(true);
                                        adapter.getDocuments().get(positionDocument).setPercent(progress);
                                        adapter.notifyItemChanged(positionDocument);
                                    });
                                } else {
                                    Log.e("Error", "Coluna TOTAL_SIZE_BYTES não encontrada");
                                }
                            }
                        } else {
                            Log.e("Error", "Coluna STATUS não encontrada");
                        }
                    } else {
                        Log.e("Error", "Nenhum resultado encontrado para o downloadId: " + downloadId);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                // Espera um pouco antes de verificar novamente
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    private void openDownloadedFile(long downloadId) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri fileUri = downloadManager.getUriForDownloadedFile(downloadId);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType = getContentResolver().getType(fileUri);
        if (mimeType == null) {
            // Se não for possível determinar o tipo, você pode definir um tipo padrão
            mimeType = "*/*"; // Tipo MIME genérico
        }
        intent.setDataAndType(fileUri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void startDownload() {
        String url = adapter.getDocuments().get(positionDocument).getUrl();
        String fileName = adapter.getDocuments().get(positionDocument).getName();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDestinationInExternalPublicDir(getExternalCacheDir().getAbsolutePath(), fileName);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Baixando " + fileName);
        request.setDescription("Aguarde...");

        // Inicia o download
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    // O download foi concluído, abrir o arquivo
                    openDownloadedFile(fileName);
                    // Não esquecer de cancelar o registro
                    unregisterReceiver(this);
                    adapter.getDocuments().get(positionDocument).setLoading(false);
                    adapter.notifyItemChanged(positionDocument);
                }
            }
        };

        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {

                    int downloadedColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int totalBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                    if (downloadedColumnIndex != -1 && totalBytesColumnIndex != -1) {
                        int bytesDownloaded = cursor.getInt(downloadedColumnIndex);
                        int totalBytes = cursor.getInt(totalBytesColumnIndex);

                        if (totalBytes > 0) {
                            int progress = (int) ((bytesDownloaded * 100L) / totalBytes);
                            runOnUiThread(() -> {
                                adapter.getDocuments().get(positionDocument).setLoading(true);
                                adapter.getDocuments().get(positionDocument).setPercent(progress);
                                adapter.notifyItemChanged(positionDocument);
                            });
                            Log.d("DownloadProgress", "Progresso: " + progress + "%");
                        }
                    }

                    // Continue verificando enquanto o download não for concluído
                    int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (statusColumnIndex != -1 && cursor.getInt(statusColumnIndex) == DownloadManager.STATUS_RUNNING) {
                        handler.postDelayed(this, 1000); // Verifica a cada segundo
                    } else if (statusColumnIndex != -1 && cursor.getInt(statusColumnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        // O download foi concluído, abrir o arquivo
                        openDownloadedFile(fileName);
                        unregisterReceiver(downloadReceiver); // Cancela o registro
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        };

        handler.postDelayed(runnable, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDownload();
            } else {
                Toast.makeText(this, "Permissão negada para gravar no armazenamento", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }
}
