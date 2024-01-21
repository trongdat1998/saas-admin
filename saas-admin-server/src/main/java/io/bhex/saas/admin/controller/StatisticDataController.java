package io.bhex.saas.admin.controller;

import com.google.common.io.Files;
import io.bhex.broker.common.util.CryptoUtil;
import io.bhex.saas.admin.service.IStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;

@RequestMapping("/api/v1/statistic")
@RestController
@Slf4j
public class StatisticDataController {
    private final IStatisticService statisticService;

    public StatisticDataController(IStatisticService statisticService) {
        this.statisticService = statisticService;
    }


    @RequestMapping(value = "/export/rate")
    public void exportRate(@RequestParam(required = false, defaultValue = "0") long date, HttpServletResponse response)
        throws Exception {
        if (date == 0) {
            date = System.currentTimeMillis();
        }
        File file = statisticService.exportRateUsdtStatisticExcelByDate(date);
        HttpHeaders header = new HttpHeaders();
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usdt_rate_" + date + ".xls");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.setContentType(com.google.common.net.MediaType.MICROSOFT_EXCEL.toString());
        Files.copy(file, response.getOutputStream());
    }

}
