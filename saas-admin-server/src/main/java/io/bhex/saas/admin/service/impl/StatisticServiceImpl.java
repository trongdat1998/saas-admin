package io.bhex.saas.admin.service.impl;

import io.bhex.ex.quote.RateStatistic;
import io.bhex.ex.quote.service.IQuoteAdminService;
import io.bhex.saas.admin.service.IStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class StatisticServiceImpl implements IStatisticService {

    private static final String USDT = "USDT";
    private final IQuoteAdminService quoteAdminService;
    private static final DateTimeZone GMT = DateTimeZone.forID("Asia/Shanghai");
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public StatisticServiceImpl(IQuoteAdminService quoteAdminService) {
        this.quoteAdminService = quoteAdminService;
    }

    @Override
    public File exportRateUsdtStatisticExcelByDate(Long date) {
        FileOutputStream xlsStream = null;
        try {
            List<RateStatistic> rateStatisticList = quoteAdminService.getRateStatisticByDay(0L, USDT, StringUtils.EMPTY, date);

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("sheet1");

            HSSFRow headRow = sheet.createRow(0);
            headRow.createCell(0).setCellValue("exchange");
            headRow.createCell(1).setCellValue("base");
            headRow.createCell(2).setCellValue("quote");
            headRow.createCell(3).setCellValue("rate");
            headRow.createCell(4).setCellValue("create_time");
            for (int row = 0; row < rateStatisticList.size(); row++) {
                HSSFRow rows = sheet.createRow(row + 1);
                RateStatistic rateStatistic = rateStatisticList.get(row);
                rows.createCell(0).setCellValue(rateStatistic.getExchangeId());
                rows.createCell(1).setCellValue(rateStatistic.getBase());
                rows.createCell(2).setCellValue(rateStatistic.getQuote());
                rows.createCell(3).setCellValue(rateStatistic.getRate());
                rows.createCell(4).setCellValue(new DateTime(rateStatistic.getCreateAt(), GMT).toString(DATE_FORMAT));
            }

            File xlsFile = new File("usdt_rate_" + date + ".xls");
            xlsStream = new FileOutputStream(xlsFile);
            workbook.write(xlsStream);
            return xlsFile;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (xlsStream != null) {
                try {
                    xlsStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
