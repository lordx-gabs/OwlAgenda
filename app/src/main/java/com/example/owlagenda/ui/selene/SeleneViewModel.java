package com.example.owlagenda.ui.selene;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.TaskAttachments;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.ClassRepository;
import com.example.owlagenda.data.repository.SchoolRepository;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.util.ChatBot;
import com.example.owlagenda.util.ModelChatBotSelene;
import com.example.owlagenda.util.NotificationUtil;
import com.google.ai.client.generativeai.type.Content;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SeleneViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final TaskRepository taskRepository;
    private final SchoolRepository schoolRepository;
    private ChatBot chatBotSelene;
    private final MutableLiveData<String> errorMessage;
    private DocumentReference taskOld;
    private MutableLiveData<String> messageChatBot;
    private final Gson gson;

    public SeleneViewModel() {
        gson = new Gson();
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        userRepository = new UserRepository();
        classRepository = new ClassRepository();
        taskRepository = new TaskRepository();
        schoolRepository = new SchoolRepository();
    }

    public LiveData<String> sendMessage(String userMessage, Context context) {
        isLoading.postValue(true);
        messageChatBot = new MutableLiveData<>();
        chatBotSelene.sendMessage(userMessage, new ChatBot.Callback<>() {
            @Override
            public void onSuccess(String result) {
                Log.d("testee", result);
                if (!result.isEmpty()) {
                    ResponseChatbot responseChatbot = gson.fromJson(result, ResponseChatbot.class);
                    if (responseChatbot.getTaskAction() != null) {
                        int taskAction;
                        try {
                            taskAction = Integer.parseInt(responseChatbot.getTaskAction());
                        } catch (NumberFormatException e) {
                            messageChatBot.postValue("Não foi possível realizar a ação. Por favor, tente novamente.");
                            return;
                        }

                        String taskName, taskType, taskClass, taskDescription, taskDate, taskSchool;

                        if (responseChatbot.getTaskTitle() != null) {
                            taskName = responseChatbot.getTaskTitle();
                        } else {
                            taskName = "";
                        }
                        if (responseChatbot.getTaskDescription() != null) {
                            taskDescription = responseChatbot.getTaskDescription();
                        } else {
                            taskDescription = "";
                        }
                        if (responseChatbot.getTaskDate() != null) {
                            taskDate = responseChatbot.getTaskDate();
                        } else {
                            taskDate = "";
                        }
                        if (responseChatbot.getTaskClass() != null) {
                            taskClass = responseChatbot.getTaskClass();
                        } else {
                            taskClass = "";
                        }
                        if (responseChatbot.getTaskType() != null) {
                            taskType = responseChatbot.getTaskType();
                        } else {
                            taskType = "";
                        }
                        if (responseChatbot.getTaskSchool() != null) {
                            taskSchool = responseChatbot.getTaskSchool();
                        } else {
                            taskSchool = "";
                        }
                        Log.d("teste", taskName);
                        Log.d("teste", taskType);
                        Log.d("teste", taskClass);
                        Log.d("teste", taskDate);
                        Log.d("teste", taskDescription);
                        Log.d("teste", taskSchool);
                        Log.d("teste", "" + taskAction);

                        switch (taskAction) {
                            case 1: {
                                // adicionar tarefa ao banco de dados
                                addTask(taskName, taskType, taskClass, taskDate,
                                        taskSchool, taskDescription, responseChatbot, context);
                                break;
                            }
                            case 3: {
                                // editar tarefa do banco de dados
                                if (!taskName.isEmpty()) {
                                    if (taskName.contains(",")) {
                                        String[] tasksName = taskName.split(",");
                                        taskName = tasksName[0];
                                        String taskNameNew = tasksName[1];
                                        updateTask(taskName, taskNameNew, taskClass, taskSchool, taskType, taskDescription, taskDate, responseChatbot, context);
                                    } else {
                                        updateTask(taskName, "", taskClass, taskSchool, taskType, taskDescription, taskDate, responseChatbot, context);
                                    }
                                } else {
                                    messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                                }
                                break;
                            }
                            case 2: {
                                // deletar a tarefa do banco de dados
                                deleteTask(taskName, taskClass, taskSchool, responseChatbot, context);
                                break;
                            }
                            case 4: {
                                // lista de tarefas do dia
                                getTasksByToday();
                                break;
                            }
                            case 5: {
                                // lista de tarefas do mes
                                getTasksByMonth();
                                break;
                            }
                        }
                    } else {
                        messageChatBot.postValue(responseChatbot.getResponse());
                    }
                } else {
                    messageChatBot.postValue("Ocorreu um problema técnico com a Selene, por favor, tente enviar outra mensagem.");
                }
            }

            @Override
            public void onFailure(Throwable t) {
                messageChatBot.postValue(null);
                Log.e("SeleneViewModel", "Erro ao enviar mensagem: " + t.getMessage());
                isLoading.postValue(false);
            }
        });

        return messageChatBot;
    }

    private void getTasksByToday() {
        taskRepository.getTaskByDateToday(FirebaseAuth.getInstance().getCurrentUser().getUid(), task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Suas tarefas do dia são:\n");
                    ArrayList<Task> tasks = new ArrayList<>();

                    // Itera sobre os documentos e processa cada um
                    for (DocumentSnapshot document : documents) {
                        // Aqui você pode converter o DocumentSnapshot em seu objeto de tarefa
                        Task taskDay = document.toObject(Task.class);
                        tasks.add(taskDay);
                    }
                    tasks.forEach(task3 -> stringBuilder.append(task3.getTitle()).append("\n"));
                    messageChatBot.postValue(stringBuilder.toString());
                } else {
                    messageChatBot.postValue("Sem tarefas para hoje :)");
                }
            } else {
                messageChatBot.postValue("Não foi possivel listar as tarefas. Por favor, tente novamente.");
            }
        });
    }

    private void getTasksByMonth() {
        taskRepository.getTaskByDateMonth(FirebaseAuth.getInstance().getCurrentUser().getUid(), task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    String currentMonth = DateTimeFormatter.ofPattern("MM").format(LocalDate.now());
                    String currentYear = DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now());
                    List<Task> filteredTasks = new ArrayList<>();
                    QuerySnapshot querySnapshot = task.getResult();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Suas tarefas do mês são:\n");

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Extrair o campo de data (assumindo que está no formato "dd/MM/yyyy")
                        String dateStr = document.getString("date");

                        if (dateStr != null && dateStr.length() == 10) {
                            // Pegar o mês e o ano da data do documento
                            String docMonth = dateStr.substring(3, 5); // Extrai o mês
                            String docYear = dateStr.substring(6, 10); // Extrai o ano

                            // Comparar com o mês e o ano atuais
                            if (docMonth.equals(currentMonth) && docYear.equals(currentYear)) {
                                filteredTasks.add(document.toObject(Task.class));
                            }
                        }
                    }
                    filteredTasks.forEach(task1 -> stringBuilder.append
                            ("Titulo: " + task1.getTitle() + "\nDescrição: " + task1.getDescription() + "\nData: " + task1.getDate()).append("\n"));
                    messageChatBot.postValue(stringBuilder.toString());
                } else {
                    messageChatBot.postValue("Sem tarefas para o mês :)");
                }
            } else {
                messageChatBot.postValue("Não foi possivel listar as tarefas. Por favor, tente novamente.");
            }
        });
    }

    private void addTask(String taskName, String taskType, String taskClass, String taskDate, String taskSchool, String taskDescription, ResponseChatbot responseChatbot, Context context) {
        if (!taskName.isEmpty() && !taskType.isEmpty() && !taskClass.isEmpty() &&
                !taskDate.isEmpty() && !taskSchool.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
            String currentDay = dayFormat.format(calendar.getTime());
            String currentMonth = monthFormat.format(calendar.getTime());
            String currentYear = yearFormat.format(calendar.getTime());
            // adiciona a tarefa ao banco de dados
            schoolRepository.getSchoolByName(taskSchool.toUpperCase(), task3 -> {
                if (task3.isSuccessful()) {
                    if (!task3.getResult().getDocuments().isEmpty()) {
                        classRepository.getClassByNameAndSchool(taskClass.toUpperCase(),
                                task3.getResult().getDocuments().get(0).getReference(), task -> {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {

                                            String taskDateFormatted = getDateFormatted(taskDate, currentDay, currentMonth, currentYear);

                                            DocumentReference classRef = task.getResult().getDocuments().get(0).getReference();
                                            Task taskData = new Task(FirebaseFirestore.getInstance()
                                                    .collection("usuario").document(FirebaseAuth.getInstance()
                                                            .getCurrentUser().getUid()), taskName.trim(), taskName.trim().toUpperCase(),
                                                    taskDescription, taskType, taskDateFormatted, classRef, task3.getResult()
                                                    .getDocuments().get(0).getReference());
                                            taskData.setId(FirebaseFirestore.getInstance().collection("tarefa").document().getId());

                                            if (isTaskDateInFuture(taskDateFormatted)) {
                                                messageChatBot.postValue("Data inválida. Por favor, informe uma data depois do dia de hoje.");
                                                return;
                                            }
                                            taskRepository.addTask(taskData, task1 -> {
                                                if (task1.isSuccessful()) {
                                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                                    dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Definir o fuso horário

                                                    try {
                                                        // Faz o parse da data
                                                        calendar.setTime(dateFormat.parse(taskDateFormatted));
                                                        calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

                                                        Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                        currentCalendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                                                        calendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
                                                        calendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
                                                        calendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));

                                                        calendar.add(Calendar.MINUTE, -10); // Subtrai o tempo de notificação
                                                    } catch (ParseException e) {
                                                        messageChatBot.postValue("Erro ao adicionar a tarefa. Por favor, tente novamente.");
                                                        return;
                                                    }

                                                    int idNotification = 0;
                                                    try {
                                                        idNotification = Integer.parseInt(taskData.getId().replaceAll("[^0-9]", ""));
                                                    } catch (Exception ignored) {

                                                    }

                                                    NotificationUtil.scheduleNotificationApp.scheduleNotification(context.getApplicationContext(),
                                                            calendar.getTimeInMillis(),
                                                            taskName,
                                                            idNotification);
                                                    messageChatBot.postValue(responseChatbot.getResponse());
                                                } else {
                                                    if (task1.getException() instanceof FirebaseNetworkException) {
                                                        messageChatBot.postValue("Erro de conexão. Verifique sua conexão e tente novamente.");
                                                    } else {
                                                        messageChatBot.postValue("Não foi possível adicionar a tarefa. Por favor, tente novamente.");
                                                    }
                                                }
                                            });
                                        } else {
                                            // pedir para ele criar a turma?
                                            messageChatBot.postValue("Classe não encontrada, por favor informe uma classe válida. ");
                                        }
                                    } else {
                                        messageChatBot.postValue("Não foi possível adicionar a tarefa. Por favor, tente novamente.");
                                    }
                                }
                        );
                    } else {
                        messageChatBot.postValue("Escola não encontrada, por favor informe uma escola válida.");
                    }
                } else {
                    messageChatBot.postValue("Não foi possível adicionar a tarefa. Por favor, tente novamente.");
                }
            });

        } else {
            messageChatBot.postValue("Não foi possível adicionar a tarefa. Por favor, tente novamente.");
        }
    }

    private @NonNull String getDateFormatted(String taskDate, String currentDay, String currentMonth, String currentYear) {
        String taskDateFormatted;
        // Verifica o tamanho da string taskDate
        if (taskDate.length() == 2) {
            // Se for de tamanho 2, assume que está faltando o mês e o ano
            taskDateFormatted = taskDate + "/" + currentMonth + "/" + currentYear;
        } else if (taskDate.length() == 5) {
            // Se for de tamanho 5, assume que está faltando o ano
            taskDateFormatted = taskDate + "/" + currentYear;
        } else if (taskDate.length() == 8) {
            String[] dateParts = taskDate.split("/");

            String day = dateParts[0];
            String month = dateParts[1];
            String year = dateParts[2];

            if (year.length() == 2) {
                year = "20" + year;
            }

            taskDateFormatted = day + "/" + month + "/" + year;
        } else if (taskDate.length() == 10) {
            // Se for de tamanho 10, assume que está completo
            taskDateFormatted = taskDate;
        } else {
            taskDateFormatted = currentDay + "/" + currentMonth + "/" + currentYear;
        }

        return taskDateFormatted;
    }

    private boolean isTaskDateInFuture(String taskDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar currentDate = Calendar.getInstance();
        Date date;
        try {
            date = dateFormat.parse(taskDate);
        } catch (ParseException e) {
            return true;
        }
        return !date.after(currentDate.getTime());
    }

    private void deleteTask(String taskName, String taskClass, String taskSchool, ResponseChatbot responseChatbot, Context context) {
        if (!taskName.isEmpty() && !taskClass.isEmpty() && !taskSchool.isEmpty()) {
            schoolRepository.getSchoolByName(taskSchool.toUpperCase(), task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        classRepository.getClassByNameAndSchool(taskClass.toUpperCase(), task.getResult().getDocuments()
                                .get(0).getReference(), task3 -> {
                            if (task3.isSuccessful()) {
                                if (!task3.getResult().isEmpty()) {
                                    taskRepository.getTaskByTitleAndSchoolClass(taskName.trim().toUpperCase(),
                                            task3.getResult().getDocuments().get(0).getReference(), task12 -> {
                                                if (task12.isSuccessful()) {
                                                    if (!task12.getResult().isEmpty()) {
                                                        task12.getResult().getDocuments().get(0).getReference().delete()
                                                                .addOnCompleteListener(task13 -> {
                                                                    if (task13.isSuccessful()) {
                                                                        ArrayList<TaskAttachments> taskAttachments = task12.getResult().getDocuments().get(0).toObject(Task.class).getTaskDocuments();
                                                                        if (taskAttachments != null) {
                                                                            taskRepository.deleteAttachmentsStorage(task12.getResult().getDocuments().get(0).toObject(Task.class).getTaskDocuments(), task1 -> {
                                                                                if (task1.isSuccessful()) {
                                                                                    Log.d("teste excluir arquivos ia", "sucesso");
                                                                                } else {
                                                                                    Log.d("teste excluir arquivos ia", "erro" + task1.getException().getMessage());
                                                                                }
                                                                            });
                                                                        }
                                                                        int notificationId = 0;
                                                                        try {
                                                                            notificationId = Integer.parseInt(task12.getResult().getDocuments().get(0).getReference().getId().replaceAll("[^0-9]", ""));
                                                                        } catch (
                                                                                NumberFormatException ignored) {

                                                                        }
                                                                        Log.d("teste", "" + notificationId);
                                                                        if (NotificationUtil.scheduleNotificationApp.isAlarmSet(context, taskName,
                                                                                notificationId)) {
                                                                            NotificationUtil.scheduleNotificationApp.cancelNotification(context, taskName,
                                                                                    notificationId);
                                                                            Log.d("testeee", "chegouu");
                                                                        }
                                                                        messageChatBot.postValue(responseChatbot.getResponse());
                                                                    } else {
                                                                        messageChatBot.postValue("Não foi possivel excluir a tarefa. Por favor, tente novamente.");
                                                                    }
                                                                });
                                                    } else {
                                                        messageChatBot.postValue("Nome da tarefa inválido. Informe um nome da tarefa válido.");
                                                    }
                                                } else {
                                                    messageChatBot.postValue("Não foi possivel excluir a tarefa. Por favor, tente novamente.");
                                                }
                                            });
                                } else {
                                    messageChatBot.postValue("Turma inválida. Por favor, informe uma turma válida.");
                                }
                            } else {
                                messageChatBot.postValue("Não foi possivel excluir a tarefa. Por favor, tente novamente.");
                            }
                        });
                    } else {
                        messageChatBot.postValue("Escola inválida. Por favor, informe uma escola válida.");
                    }
                } else {
                    messageChatBot.postValue("Não foi possivel excluir a tarefa. Por favor, tente novamente.");
                }
            });
        } else {
            messageChatBot.postValue("Nome da tarefa, escola, ou turma inválido, por favor tente novamente.");
        }
    }

    private void updateTask(String taskName, String newTaskName, String taskClass, String taskSchool, String taskType, String taskDescription, String taskDate, ResponseChatbot responseChatbot, Context context) {
        taskRepository.getTaskByTitle(taskName.trim().toUpperCase(), task -> {
            if (task.isSuccessful()) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                String currentDay = dayFormat.format(calendar.getTime());
                String currentMonth = monthFormat.format(calendar.getTime());
                String currentYear = yearFormat.format(calendar.getTime());

                if (!task.getResult().isEmpty()) {
                    taskOld = task.getResult().getDocuments().get(0).getReference();
                    if (!taskClass.isEmpty() && !taskSchool.isEmpty()) {
                        schoolRepository.getSchoolByName(taskSchool.toUpperCase(), task14 -> {
                            if (task14.isSuccessful()) {
                                if (!task14.getResult().isEmpty()) {
                                    classRepository.getClassByNameAndSchool(taskClass.toUpperCase(),
                                            task14.getResult().getDocuments().get(0).getReference(), task12 -> {
                                                if (task12.isSuccessful()) {
                                                    if (!task12.getResult().isEmpty()) {
                                                        SchoolClass schoolClass = task12.getResult().getDocuments()
                                                                .get(0).toObject(SchoolClass.class);
                                                        String taskNameTitle;
                                                        if (newTaskName.isEmpty()) {
                                                            taskNameTitle = taskName.trim();
                                                        } else {
                                                            taskNameTitle = newTaskName.trim();
                                                        }
                                                        String taskDateFormatted = "";
                                                        if (!taskDate.isEmpty()) {
                                                            taskDateFormatted = getDateFormatted(taskDate, currentDay, currentMonth, currentYear);
                                                            if (isTaskDateInFuture(taskDateFormatted)) {
                                                                messageChatBot.postValue("Data inválida. Por favor, informe uma data depois do dia de hoje.");
                                                                return;
                                                            }
                                                        }
                                                        String finalTaskDateFormatted = taskDateFormatted;
                                                        taskRepository.updateTaskFields(taskOld, taskNameTitle.trim(), taskType,
                                                                schoolClass.getSchoolId(), task12.getResult().getDocuments().get(0).getReference(),
                                                                taskDescription.trim(), taskDateFormatted, task13 -> {
                                                                    if (task13.isSuccessful()) {
                                                                        if (!taskDate.isEmpty()) {
                                                                            scheduleNotification(finalTaskDateFormatted, context, taskName.trim(), newTaskName.trim());
                                                                        }
                                                                        messageChatBot.postValue(responseChatbot.getResponse());
                                                                    } else {
                                                                        messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                                                                    }
                                                                });
                                                    } else {
                                                        messageChatBot.postValue("Turma não encontrada. Informe uma turma válida.");
                                                    }
                                                } else {
                                                    messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                                                }
                                            });
                                } else {
                                    messageChatBot.postValue("Escola não encontrada. Informe uma escola válida.");
                                }
                            } else {
                                messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                            }
                        });
                    } else {
                        String taskDateFormatted = "";
                        if (!taskDate.isEmpty()) {
                            taskDateFormatted = getDateFormatted(taskDate, currentDay, currentMonth, currentYear);
                            if (isTaskDateInFuture(taskDateFormatted)) {
                                messageChatBot.postValue("Data inválida. Por favor, informe uma data depois do dia de hoje.");
                                return;
                            }
                        }
                        String finalTaskDateFormatted = taskDateFormatted;
                        taskRepository.updateTaskFields(taskOld, newTaskName.trim(), taskType, null,
                                null, taskDescription, taskDateFormatted, task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (!taskDate.isEmpty()) {
                                            scheduleNotification(finalTaskDateFormatted, context, taskName, newTaskName.trim());
                                        }
                                        messageChatBot.postValue(responseChatbot.getResponse());
                                    } else {
                                        messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                                    }
                                });
                    }
                } else {
                    messageChatBot.postValue("Nome da tarefa inválido. Informe um nome da tarefa válido.");
                }
            } else {
                messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
            }
        });
    }

    private void scheduleNotification(String taskDate, Context context, String taskNameOld, String taskNameNew) {
        Log.d("teseee", taskDate);
        if (!taskDate.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Definir o fuso horário

            Calendar calendar = Calendar.getInstance();
            try {
                // Faz o parse da data
                calendar.setTime(dateFormat.parse(taskDate));
                calendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

                Calendar currentCalendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
                currentCalendar.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                calendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, currentCalendar.get(Calendar.SECOND));

                calendar.add(Calendar.MINUTE, -1440); // Subtrai o tempo de notificação
            } catch (ParseException e) {
                messageChatBot.postValue("Erro ao adicionar a tarefa. Por favor, tente novamente.");
            }

            int idNotification = 0;
            try {
                idNotification = Integer.parseInt(taskOld.getId().replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {
            }

            Log.d("testeeee", "" + idNotification);
            if (NotificationUtil.scheduleNotificationApp.isAlarmSet(context.getApplicationContext(), taskNameOld, idNotification)) {
                NotificationUtil.scheduleNotificationApp.cancelNotification(context.getApplicationContext(), taskNameOld, idNotification);
                Log.d("testeee", "chegouu");
                if (taskNameNew.isEmpty()) {
                    NotificationUtil.scheduleNotificationApp.scheduleNotification(context.getApplicationContext()
                            , calendar.getTimeInMillis(),
                            taskNameOld.trim(),
                            idNotification);
                } else {
                    NotificationUtil.scheduleNotificationApp.scheduleNotification(context.getApplicationContext()
                            , calendar.getTimeInMillis(),
                            taskNameNew.trim(),
                            idNotification);
                }
            }
        }
    }

    public void saveHistoryMessageUser(User user, ArrayList<Message> history) {
        userRepository.saveMessageHistory(user.getId(), history, task -> {
            if (!task.isSuccessful()) {
                errorMessage.postValue("Erro ao salvar o histórico de conversa.");
            }
        });
    }

    public void deleteHistoryMessageUser(String uid) {
        userRepository.deleteMessageHistory(uid, task -> {
            if (!task.isSuccessful()) {
                errorMessage.postValue("Erro ao excluir histórico da conversa.");
            }
        });
    }

    public void setChatBotSelene(List<Content> historyMessage) {
        if (historyMessage.isEmpty()) {
            chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat());
        } else {
            chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat(historyMessage));
        }
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
