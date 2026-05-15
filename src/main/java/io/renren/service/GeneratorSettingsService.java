package io.renren.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.renren.model.setting.GeneratorSettings;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@Service
public class GeneratorSettingsService {

    private static final String SETTINGS_FILE = System.getProperty("user.dir") + File.separator + "generator-settings.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public GeneratorSettings getSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return new GeneratorSettings();
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return OBJECT_MAPPER.readValue(fis, GeneratorSettings.class);
        } catch (IOException e) {
            return new GeneratorSettings();
        }
    }

    public void saveSettings(GeneratorSettings settings) {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(writer, settings);
        } catch (IOException e) {
            throw new RuntimeException("保存设置文件失败", e);
        }
    }
}
