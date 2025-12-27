package com.genai.test;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * GUI приложение для тестирования Google Gen AI Java SDK.
 * Позволяет вводить API ключ, выбирать модель и отправлять запросы.
 */
public class GenAiTester extends JFrame {
    private JTextField apiKeyField;
    private JComboBox<String> modelComboBox;
    private JTextArea promptArea;
    private JTextArea responseArea;
    private JButton sendButton;
    private JButton refreshModelsButton;
    private JLabel statusLabel;
    private Client client;

    public GenAiTester() {
        initializeGUI();
    }

    /**
     * Инициализация графического интерфейса.
     */
    private void initializeGUI() {
        setTitle("Google Gen AI Tester");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(900, 700);

        // Панель настроек (верхняя часть)
        JPanel settingsPanel = createSettingsPanel();
        add(settingsPanel, BorderLayout.NORTH);

        // Панель запроса (левая часть)
        JPanel requestPanel = createRequestPanel();
        add(requestPanel, BorderLayout.WEST);

        // Панель ответа (правая часть)
        JPanel responsePanel = createResponsePanel();
        add(responsePanel, BorderLayout.CENTER);

        // Панель статуса (нижняя часть)
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // Центрирование окна
        setLocationRelativeTo(null);
    }

    /**
     * Создание панели настроек (API ключ и модель).
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Настройки"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // API ключ
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("API Ключ:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        apiKeyField = new JTextField(40);
        apiKeyField.setToolTipText("Введите ваш Google Gen AI API ключ");
        panel.add(apiKeyField, gbc);

        // Модель
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Модель:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        modelComboBox = new JComboBox<>();
        modelComboBox.setToolTipText("Введите API ключ и нажмите 'Обновить список моделей'");
        panel.add(modelComboBox, gbc);
        
        // Кнопка обновления списка моделей
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        refreshModelsButton = new JButton("Обновить список моделей");
        refreshModelsButton.addActionListener(new RefreshModelsListener());
        panel.add(refreshModelsButton, gbc);

        return panel;
    }

    /**
     * Создание панели запроса.
     */
    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Запрос"));
        panel.setPreferredSize(new Dimension(400, 0));

        promptArea = new JTextArea(20, 30);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        promptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(promptArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        sendButton = new JButton("Отправить запрос");
        sendButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        sendButton.addActionListener(new SendButtonListener());
        panel.add(sendButton, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Создание панели ответа.
     */
    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Ответ"));

        responseArea = new JTextArea(20, 30);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        responseArea.setBackground(new Color(245, 245, 245));
        JScrollPane scrollPane = new JScrollPane(responseArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Создание панели статуса.
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Готов к работе");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel);
        return panel;
    }

    /**
     * Обработчик кнопки обновления списка моделей.
     */
    private class RefreshModelsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String apiKey = apiKeyField.getText().trim();
            
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(
                    GenAiTester.this,
                    "Пожалуйста, введите API ключ перед обновлением списка моделей",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Отключение кнопки во время загрузки
            refreshModelsButton.setEnabled(false);
            statusLabel.setText("Загрузка списка моделей...");
            statusLabel.setForeground(Color.ORANGE);
            
            // Асинхронная загрузка списка моделей
            CompletableFuture.supplyAsync(() -> {
                try {
                    return fetchModelsList(apiKey);
                } catch (Exception ex) {
                    return null;
                }
            }).thenAccept(models -> {
                SwingUtilities.invokeLater(() -> {
                    refreshModelsButton.setEnabled(true);
                    if (models != null && !models.isEmpty()) {
                        modelComboBox.removeAllItems();
                        for (String model : models) {
                            modelComboBox.addItem(model);
                        }
                        statusLabel.setText("Загружено моделей: " + models.size());
                        statusLabel.setForeground(Color.GREEN);
                    } else {
                        statusLabel.setText("Ошибка загрузки списка моделей");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(
                            GenAiTester.this,
                            "Не удалось загрузить список моделей. Проверьте API ключ.",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    refreshModelsButton.setEnabled(true);
                    statusLabel.setText("Ошибка загрузки списка моделей");
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(
                        GenAiTester.this,
                        "Ошибка: " + throwable.getMessage(),
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
                return null;
            });
        }
    }
    
    /**
     * Получение списка доступных моделей от Google API.
     *
     * @param apiKey API ключ
     * @return список названий моделей
     * @throws Exception при ошибке запроса
     */
    private List<String> fetchModelsList(String apiKey) throws Exception {
        List<String> models = new ArrayList<>();
        
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Парсинг JSON ответа
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("models")) {
                    JSONArray modelsArray = jsonResponse.getJSONArray("models");
                    for (int i = 0; i < modelsArray.length(); i++) {
                        JSONObject model = modelsArray.getJSONObject(i);
                        if (model.has("name")) {
                            String modelName = model.getString("name");
                            // Извлекаем только имя модели (убираем префикс "models/")
                            if (modelName.startsWith("models/")) {
                                modelName = modelName.substring(7);
                            }
                            models.add(modelName);
                        }
                    }
                }
            } else {
                BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream())
                );
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                throw new Exception("HTTP " + responseCode + ": " + errorResponse.toString());
            }
            
            connection.disconnect();
        } catch (Exception e) {
            throw new Exception("Ошибка получения списка моделей: " + e.getMessage(), e);
        }
        
        return models;
    }

    /**
     * Обработчик кнопки отправки запроса.
     */
    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String apiKey = apiKeyField.getText().trim();
            String model = (String) modelComboBox.getSelectedItem();
            String prompt = promptArea.getText().trim();

            // Валидация
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(
                    GenAiTester.this,
                    "Пожалуйста, введите API ключ",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (prompt.isEmpty()) {
                JOptionPane.showMessageDialog(
                    GenAiTester.this,
                    "Пожалуйста, введите запрос",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Отключение кнопки во время обработки
            sendButton.setEnabled(false);
            statusLabel.setText("Отправка запроса...");
            statusLabel.setForeground(Color.ORANGE);
            responseArea.setText("");

            // Асинхронная отправка запроса
            CompletableFuture.supplyAsync(() -> {
                try {
                    return sendRequest(apiKey, model, prompt);
                } catch (Exception ex) {
                    return "Ошибка: " + ex.getMessage();
                }
            }).thenAccept(result -> {
                SwingUtilities.invokeLater(() -> {
                    responseArea.setText(result);
                    sendButton.setEnabled(true);
                    statusLabel.setText("Запрос выполнен успешно");
                    statusLabel.setForeground(Color.GREEN);
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    responseArea.setText("Ошибка: " + throwable.getMessage());
                    sendButton.setEnabled(true);
                    statusLabel.setText("Ошибка при выполнении запроса");
                    statusLabel.setForeground(Color.RED);
                });
                return null;
            });
        }
    }

    /**
     * Отправка запроса к Google Gen AI API.
     *
     * @param apiKey API ключ
     * @param modelName название модели
     * @param prompt текст запроса
     * @return ответ от API
     * @throws Exception при ошибке API
     */
    private String sendRequest(String apiKey, String modelName, String prompt) 
            throws Exception {
        try {
            // Создание клиента с API ключом
            client = Client.builder()
                .apiKey(apiKey)
                .build();

            // Конфигурация генерации
            GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.7f)
                .topP(0.95f)
                .topK(40.0f)
                .maxOutputTokens(2048)
                .build();

            // Отправка запроса (API принимает String prompt напрямую)
            GenerateContentResponse response = client.models.generateContent(
                modelName, 
                prompt, 
                config
            );

            // Извлечение ответа через метод text()
            String result = response.text();
            
            if (result == null || result.isEmpty()) {
                return "Получен пустой ответ от модели.";
            }

            return result;

        } catch (Exception e) {
            throw new Exception("Ошибка API: " + e.getMessage(), e);
        }
    }

    /**
     * Точка входа в приложение.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        // Установка Look and Feel для лучшего внешнего вида
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Использование дефолтного L&F при ошибке
        }

        SwingUtilities.invokeLater(() -> {
            GenAiTester app = new GenAiTester();
            app.setVisible(true);
        });
    }
}

