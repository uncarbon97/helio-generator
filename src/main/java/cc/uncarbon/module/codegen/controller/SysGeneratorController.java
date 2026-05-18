/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package cc.uncarbon.module.codegen.controller;

import cn.hutool.core.io.IoUtil;
import cc.uncarbon.module.codegen.model.setting.GeneratorSettings;
import cc.uncarbon.module.codegen.model.request.GenerateOptionsRequest;
import cc.uncarbon.module.codegen.service.GeneratorSettingsService;
import cc.uncarbon.module.codegen.service.SysGeneratorService;
import cc.uncarbon.module.codegen.utils.PageUtils;
import cc.uncarbon.module.codegen.utils.Query;
import cc.uncarbon.module.codegen.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 代码生成器
 *
 * @author Mark sunlightcs@gmail.com
 */
@Controller
public class SysGeneratorController {
    @Autowired
    private SysGeneratorService sysGeneratorService;
    @Autowired
    private GeneratorSettingsService generatorSettingsService;

    @GetMapping("/")
    public String index() {
        return "generator.html";
    }

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/sys/generator/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils pageUtil = sysGeneratorService.queryList(new Query(params));

        return R.ok().put("page", pageUtil);
    }

    /**
     * 生成代码
     */
    @RequestMapping("/sys/generator/code")
    public void code(String tables, GenerateOptionsRequest request, HttpServletResponse response) throws IOException {
        byte[] data = sysGeneratorService.generatorCode(tables.split(","), request);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tables + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 获取生成器设置
     */
    @ResponseBody
    @GetMapping("/sys/generator/settings")
    public R getSettings() {
        GeneratorSettings settings = generatorSettingsService.getSettings();
        return R.ok().put("settings", settings);
    }

    /**
     * 保存生成器设置
     */
    @ResponseBody
    @PostMapping("/sys/generator/settings/save")
    public R saveSettings(@RequestBody GeneratorSettings settings) {
        generatorSettingsService.saveSettings(settings);
        return R.ok();
    }
}
