package com.example.owlagenda.ui.selene;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.owlagenda.data.models.SchoolClass;
import com.example.owlagenda.data.models.Task;
import com.example.owlagenda.data.models.User;
import com.example.owlagenda.data.repository.ClassRepository;
import com.example.owlagenda.data.repository.SchoolRepository;
import com.example.owlagenda.data.repository.TaskRepository;
import com.example.owlagenda.data.repository.UserRepository;
import com.example.owlagenda.util.ChatBot;
import com.example.owlagenda.util.ModelChatBotSelene;
import com.google.ai.client.generativeai.type.Content;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SeleneViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoading;
    private final UserRepository userRepository;
    private final ClassRepository classRepository;
    private final TaskRepository taskRepository;
    private final SchoolRepository schoolRepository;
    private ChatBot chatBotSelene;
    private final MutableLiveData<String> errorMessage;
    DocumentReference taskOld;
    private MutableLiveData<String> messageChatBot;

    public SeleneViewModel() {
        errorMessage = new MutableLiveData<>();
        isLoading = new MutableLiveData<>();
        userRepository = new UserRepository();
        classRepository = new ClassRepository();
        taskRepository = new TaskRepository();
        schoolRepository = new SchoolRepository();
    }

    public LiveData<String> sendMessage(String userMessage) {
        isLoading.postValue(true);
        messageChatBot = new MutableLiveData<>();
        chatBotSelene.sendMessage(userMessage, new ChatBot.Callback<>() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
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
                                    taskSchool, taskDescription, responseChatbot);
                        }
                        case 2: {
                            // editar tarefa do banco de dados
                            if (!taskName.isEmpty()) {
                                if (taskName.contains(",")) {
                                    String[] tasksName = taskName.split(",");
                                    taskName = tasksName[0];
                                    String taskNameNew = tasksName[1];
                                    updateTask(taskName, taskNameNew, taskClass, taskSchool, taskType, taskDescription, taskDate, responseChatbot);
                                } else {
                                    updateTask(taskName, "", taskClass, taskSchool, taskType, taskDescription, taskDate, responseChatbot);
                                }
                            } else {
                                messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                            }

                        }
                        case 3: {
                            // deletar a tarefa do banco de dados
                            deleteTask(taskName, taskClass, taskSchool, responseChatbot);
                        }
                        case 4: {
                            // lista de tarefas do dia
                            getTasksByToday();
                        }
                        case 5: {
                            // lista de tarefas do mes
                            getTasksByMonth();
                        }
                    }
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
                    filteredTasks.forEach(task1 -> stringBuilder.append(task1.getTitle()).append("\n"));
                    messageChatBot.postValue(stringBuilder.toString());
                } else {
                    messageChatBot.postValue("Sem tarefas para hoje :)");
                }
            } else {
                messageChatBot.postValue("Não foi possivel listar as tarefas. Por favor, tente novamente.");
            }
        });
    }

    private void addTask(String taskName, String taskType, String taskClass, String taskDate, String taskSchool, String taskDescription, ResponseChatbot responseChatbot) {
        if (!taskName.isEmpty() && !taskType.isEmpty() && !taskClass.isEmpty() &&
                !taskDate.isEmpty() && !taskSchool.isEmpty()) {
            // adiciona a tarefa ao banco de dados
            schoolRepository.getSchoolByName(taskSchool, task3 -> {
                if (task3.isSuccessful()) {
                    if (!task3.getResult().getDocuments().isEmpty()) {
                        classRepository.getClassByNameAndSchool(taskClass,
                                task3.getResult().getDocuments().get(0).getReference(), task -> {
                                    if (task.isSuccessful()) {
                                        if (!task.getResult().isEmpty()) {
                                            DocumentReference classRef = task.getResult().getDocuments().get(0).getReference();
                                            Task taskData = new Task(taskName, taskDescription, taskType, taskDate,
                                                    classRef, task3.getResult().getDocuments().get(0).getReference());
                                            taskRepository.addTask(taskData, task1 -> {
                                                if (task1.isSuccessful()) {
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

    private void deleteTask(String taskName, String taskClass, String taskSchool, ResponseChatbot responseChatbot) {
        if (!taskName.isEmpty() && !taskClass.isEmpty() && !taskSchool.isEmpty()) {
            schoolRepository.getSchoolByName(taskSchool, task -> {
                if (task.isSuccessful()) {
                    if (!task.getResult().isEmpty()) {
                        classRepository.getClassByNameAndSchool(taskClass, task.getResult().getDocuments()
                                .get(0).getReference(), task3 -> {
                            if (task3.isSuccessful()) {
                                if (!task3.getResult().isEmpty()) {
                                    taskRepository.getTaskByTitleAndSchoolClass(taskName,
                                            task3.getResult().getDocuments().get(0).getReference(), task12 -> {
                                                if (task12.isSuccessful()) {
                                                    if (!task12.getResult().isEmpty()) {
                                                        task12.getResult().getDocuments().get(0).getReference().delete()
                                                                .addOnCompleteListener(task13 -> {
                                                                    if (task13.isSuccessful()) {
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

    private void updateTask(String taskName, String newTaskName, String taskClass, String taskSchool, String taskType, String taskDescription, String taskDate, ResponseChatbot responseChatbot) {
        taskRepository.getTaskByTitle(taskName, task -> {
            if (task.isSuccessful()) {
                taskOld = task.getResult().getDocuments().get(0).getReference();
                if (!taskClass.isEmpty() && !taskSchool.isEmpty()) {
                    schoolRepository.getSchoolByName(taskSchool, task14 -> {
                        if (task14.isSuccessful()) {
                            if (!task14.getResult().isEmpty()) {
                                classRepository.getClassByNameAndSchool(taskClass,
                                        task14.getResult().getDocuments().get(0).getReference(), task12 -> {
                                            if (task12.isSuccessful()) {
                                                if (!task12.getResult().isEmpty()) {
                                                    SchoolClass schoolClass = task12.getResult().getDocuments()
                                                            .get(0).toObject(SchoolClass.class);

                                                    taskRepository.updateTaskFields(taskOld, newTaskName, taskType,
                                                            schoolClass.getSchoolId(), task12.getResult().getDocuments().get(0).getReference(),
                                                            taskDescription, taskDate, task13 -> {
                                                                if (task13.isSuccessful()) {
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
                    taskRepository.updateTaskFields(taskOld, newTaskName, taskType, null,
                            null, taskDescription, taskDate, task1 -> {
                                if (task1.isSuccessful()) {
                                    messageChatBot.postValue(responseChatbot.getResponse());
                                } else {
                                    messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
                                }
                            });
                }
            } else {
                messageChatBot.postValue("Não foi possivel editar a tarefa. Por favor, tente novamente.");
            }
        });
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
        chatBotSelene = new ChatBot(ModelChatBotSelene.createChatbotModelSelene().startChat(historyMessage));
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
