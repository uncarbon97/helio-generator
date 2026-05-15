package io.renren.service;

import com.alibaba.fastjson.JSONObject;
import io.renren.model.setting.GeneratorSettings;
import org.springframework.stereotype.Service;

import javax.imageio.stream.FileCacheImageInputStream;
import java.io.*;

@Service
public class GeneratorSettingsService {

    private static final String SETTINGS_FILE = System.getProperty("user.dir") + File.separator + "generator-settings.json";

    public GeneratorSettings getSettings() {
        File file = new File(SETTINGS_FILE);
        if (!file.exists()) {
            return new GeneratorSettings();
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return JSONObject.parseObject(fis, GeneratorSettings.class);
        } catch (IOException e) {
            return new GeneratorSettings();
        }
    }

    public void saveSettings(GeneratorSettings settings) {
        try (Writer writer = new FileWriter(SETTINGS_FILE)) {
            writer.write(JSONObject.toJSONString(settings, false));
        } catch (IOException e) {
            throw new RuntimeException("保存设置文件失败", e);
        }
    }
}
