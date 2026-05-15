/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.controller;

import io.renren.model.setting.GeneratorSettings;
import io.renren.model.request.GenerateOptionsRequest;
import io.renren.service.GeneratorSettingsService;
import io.renren.service.SysGeneratorService;
import io.renren.utils.PageUtils;
import io.renren.utils.Query;
import io.renren.utils.R;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 代码生成器
 *
 * @author Mark sunlightcs@gmail.com
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
    @Autowired
    private SysGeneratorService sysGeneratorService;
    @Autowired
    private GeneratorSettingsService generatorSettingsService;

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils pageUtil = sysGeneratorService.queryList(new Query(params));

        return R.ok().put("page", pageUtil);
    }

    /**
     * 生成代码
     */
    @RequestMapping("/code")
    public void code(String tables, GenerateOptionsRequest request, HttpServletResponse response) throws IOException {
        byte[] data = sysGeneratorService.generatorCode(tables.split(","), request);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + tables + ".zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }

    /**
     * 获取生成器设置
     */
    @ResponseBody
    @GetMapping("/settings")
    public R getSettings() {
        GeneratorSettings settings = generatorSettingsService.getSettings();
        return R.ok().put("settings", settings);
    }

    /**
     * 保存生成器设置
     */
    @ResponseBody
    @PostMapping("/settings/save")
    public R saveSettings(@RequestBody GeneratorSettings settings) {
        generatorSettingsService.saveSettings(settings);
        return R.ok();
    }
}
