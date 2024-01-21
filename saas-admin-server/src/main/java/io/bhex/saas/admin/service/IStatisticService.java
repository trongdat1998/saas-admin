package io.bhex.saas.admin.service;

import java.io.File;

public interface IStatisticService {
    File exportRateUsdtStatisticExcelByDate(Long date);
}
