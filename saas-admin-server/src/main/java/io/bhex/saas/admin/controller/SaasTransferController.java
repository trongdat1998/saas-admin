package io.bhex.saas.admin.controller;

import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.token.GetTokensReply;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.param.BalanceDetailDTO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AccountAssetClient;
import io.bhex.bhop.common.grpc.client.BhAccountClient;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.exception.BrokerErrorCode;
import io.bhex.broker.common.exception.BrokerException;
import io.bhex.broker.common.objectstorage.CannedAccessControlList;
import io.bhex.broker.common.objectstorage.ObjectStorage;
import io.bhex.broker.common.util.FileUtil;
import io.bhex.saas.admin.config.AwsPublicStorageConfig;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.SaasTransferRecordDTO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.AddSaasTransferPO;
import io.bhex.saas.admin.controller.param.VerifyListPO;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.service.impl.SaasTransferService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import io.bhex.saas.admin.util.NumberUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/saas_transfer")
@RestController
@Slf4j
public class SaasTransferController extends BaseController {

    public static final String SEQUENCE_KEY = "saas.transfer.seq.";
    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private SaasTransferService saasTransferService;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Autowired
    private BrokerTokenClient exchangeTokenClient;
    @Autowired
    @Qualifier("objectPublicStorage")
    private ObjectStorage awsPublicObjectStorage;
    @Autowired
    private AwsPublicStorageConfig awsPublicStorageConfig;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AccountAssetClient assetClient;
    @Autowired
    private BhAccountClient accountClient;

    private List<String> queryTokens() {
        GetTokensReply reply = exchangeTokenClient.getBhTokensNoCache(1, 5000, 1, null, null);
        List<String> tokens = reply.getTokenDetailsList().stream().map(t -> t.getTokenId()).collect(Collectors.toList());
        return tokens;
    }
    @RequestMapping("/tokens")
    public ResultModel queryTokenList() {
        return ResultModel.ok(queryTokens());
    }



    @RequestMapping(value = {"/create", "/check"}, method = RequestMethod.POST)
    public ResultModel createSaasTransfer(@RequestBody @Valid AddSaasTransferPO po, AdminUserReply adminUser) {
        boolean submit = !StringUtils.isEmpty(po.getVerifyCode());
        if (submit) {
            if (po.getReqId() == null || po.getReqId() == 0) {
                return ResultModel.error("no reqId");
            }
            adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        }

        if (!StringUtils.isEmpty(po.getSequenceId())) {
            String tmplUrl = redisTemplate.opsForValue().get(SEQUENCE_KEY + po.getSequenceId());
            byte[] bytes = awsPublicObjectStorage.downloadObject(tmplUrl);
            XSSFWorkbook workbook;
            try {
                workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                throw new BizException(ErrorCode.ERROR);
            }
            List<TmplData> tmplRecords = convertTmpl(workbook);
            List<AddSaasTransferPO.TransferOutAccount> accounts = tmplRecords.stream().map(a -> {
                AddSaasTransferPO.TransferOutAccount account = new AddSaasTransferPO.TransferOutAccount();
                if (account.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BizException(ErrorCode.ERROR);
                }
                return account;
            }).collect(Collectors.toList());
            po.setTransferOutAccounts(accounts);
        }

        ResultModel resultModel = saasTransferService.addRecord(po, adminUser, submit);
        return resultModel;
    }

//    @RequestMapping(value = {"/create_finance_transfer"}, method = RequestMethod.POST)
//    public ResultModel createFinanceSaasTransfer(AdminUserReply adminUser) {
//
//        AddSaasTransferPO transferPO = new AddSaasTransferPO();
//        transferPO.setTitle("币多多转账");
//        transferPO.setDescription("");
//        transferPO.setMoreBrokers(false);
//
//        transferPO.setTransferInAccount(513007301207112960L);
//
//        long outAccountId = 321771271147271L;
//        List<BalanceDetailDTO> balanceDetails = assetClient.getBalances(outAccountId);
//        if (CollectionUtils.isEmpty(balanceDetails)) {
//            return ResultModel.ok(transferPO);
//        }
//        List<AddSaasTransferPO.TransferOutAccount> transferOutAccounts = new ArrayList<>();
//        for (BalanceDetailDTO balanceDetail : balanceDetails) {
//            if (balanceDetail.getAvailable().compareTo(new BigDecimal("0.00001")) < 0) {
//                continue;
//            }
//            AddSaasTransferPO.TransferOutAccount outAccount = new AddSaasTransferPO.TransferOutAccount();
//            outAccount.setTokenId(balanceDetail.getTokenId());
//            outAccount.setAccountId(outAccountId);
//            outAccount.setAmount(balanceDetail.getAvailable());
//            transferOutAccounts.add(outAccount);
//        }
//        transferPO.setTransferOutAccounts(transferOutAccounts);
//
//        return ResultModel.ok(transferPO);
//    }


    @AccessAnnotation(authIds = {1805L, 1402L})
    @RequestMapping("/verify")
    public ResultModel verifySaasTransfer(@RequestBody @Valid VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        VerifyFlowError verifyFlowError = saasTransferService.verify(po.getId(), po.getPassed(), po.getReviewComments(), adminUser);
        if (verifyFlowError != VerifyFlowError.OK) {
            return ResultModel.error(verifyFlowError.name());
        }
        return ResultModel.ok();
    }

    
    @RequestMapping("/record_list")
    public ResultModel querySaasTransferList(@RequestBody @Valid VerifyListPO po, AdminUserReply adminUser) {
        List<VerifyFlowRecordDTO> list = verifyFlowService.queryAllRecords(0,
                BizConstant.SAAS_TRANSFER_BIZ_TYPE, Lists.newArrayList(), po.getLastId(), po.getPageSize(), adminUser.getId());
        list.forEach(d -> d.setStatus(d.getStatus() > 10 ? d.getStatus() / 10 : d.getStatus()));
        return ResultModel.ok(list);
    }

    @AccessAnnotation(authIds = {1805L, 1402L})
    @RequestMapping("/detail")
    public ResultModel querySaasTransferDetail(@RequestBody IdPO po) {
        SaasTransferRecordDTO dto = saasTransferService.queryRecord(po.getId());
        if (dto.getStatus() > 10) {
            dto.setStatus(dto.getStatus() / 10);
        }
        return ResultModel.ok(dto);
    }

    @RequestMapping(value = "/download_transfer_record", produces = {"text/plain"})
    public void downloadTransferData(
            @RequestParam(value = "id") long id,
            HttpServletResponse response) throws Exception {

        response.setCharacterEncoding("UTF-8");
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
        XSSFSheet sheet = xssfWorkbook.createSheet();

        List<SaasTransferRecordDTO.TransferOutAccount> records = saasTransferService.getTransferRecords(id);

        List<String> headers = new ArrayList<>();
        headers.add("AID");
        headers.add("token");
        headers.add("amount");
        headers.add("status");

        XSSFRow titlerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            titlerRow.createCell(i).setCellValue(headers.get(i));
        }

        for (SaasTransferRecordDTO.TransferOutAccount record : records) {
            List<String> columns = new ArrayList<>();
            columns.add(record.getAccountId() + "");
            columns.add(record.getTokenId() + "");
            columns.add(record.getAmount().stripTrailingZeros().toPlainString());
            String status = "init";
            if (record.getStatus() == 1) {
                status = "locked";
            } else if (record.getStatus() == 2) {
                status = "success";
            } else if (record.getStatus() == 3 || record.getStatus() == 4) {
                status = "unlocked";
            }
            columns.add(status);

            int lastRowNum = sheet.getLastRowNum();
            XSSFRow dataRow = sheet.createRow(lastRowNum + 1);
            for (int i = 0; i < columns.size(); i++) {
                dataRow.createCell(i).setCellValue(columns.get(i));
            }
        }

        response.setHeader("content-disposition", "attachment;filename="
                + URLEncoder.encode(getOrgId() + "_" + System.currentTimeMillis() + ".xlsx", "UTF-8"));
        response.setContentType(com.google.common.net.MediaType.MICROSOFT_EXCEL.toString());

        xssfWorkbook.write(response.getOutputStream());
        xssfWorkbook.close();
    }


    @Data
    private static class TmplData {
        private String accountId;

        private String amount;

        private String tokenId;
    }
    @RequestMapping(value = "/file/text", method = RequestMethod.POST)
    public ResultModel uploadText(@RequestParam(name = "uploadFile") MultipartFile uploadImageFile,
                                  @RequestParam(value = "echoStr", required = false, defaultValue = "") String echoStr,
                                  @RequestParam(value = "type", required = true, defaultValue = "1") int type) throws Exception {

        long orgId = getOrgId();
        String fileType = FileUtil.getFileSuffix(uploadImageFile.getOriginalFilename(), "");
        if (fileType.equals("xlsx")) {
            throw new BrokerException(BrokerErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        XSSFWorkbook workbook = new XSSFWorkbook(uploadImageFile.getInputStream());

        Map<String, Object> result = new HashMap<>();
        result.put("tmplOk", true);

        String suffix = fileType.toLowerCase();
        String fileKey = "bhop/airdrop/" + System.nanoTime() + ".xlsx";

        List<TmplData> list = convertTmpl(workbook);
        if (CollectionUtils.isEmpty(list)) {
            result.put("success", false);
            return ResultModel.ok(result);
        }

        List<String> errorUserIds = new ArrayList<>();
        for (TmplData dto : list) {
            if (!NumberUtil.isDigits(dto.getAccountId())) {
                errorUserIds.add(dto.getAccountId());
            }
        }



        XSSFWorkbook xssfWorkbook = new XSSFWorkbook();

        XSSFCellStyle cellStyle = xssfWorkbook.createCellStyle();
        XSSFDataFormat format = xssfWorkbook.createDataFormat();
        cellStyle.setDataFormat(format.getFormat("@"));

        XSSFSheet sheet = xssfWorkbook.createSheet();
        List<String> headers = new ArrayList<>();
        headers.add("AID");
        headers.add("token");
        headers.add("amount");
        headers.add("数据是否有效");

        XSSFRow titlerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            titlerRow.createCell(i).setCellValue(headers.get(i));
        }

        List<String> tokens = queryTokens();
        boolean hasErrorData = false;
        for (int i = 0; i <  list.size(); i++) {
            TmplData dto = list.get(i);
            log.info("i:{} data:{}", i, dto);
            List<String> columns = new ArrayList<>();

            columns.add(dto.getAccountId() + "");
            columns.add(dto.getTokenId().toUpperCase());
            columns.add(dto.getAmount());


            String error = "";

            if (!NumberUtil.isNumber(dto.getAmount()) || new BigDecimal(dto.getAmount()).compareTo(BigDecimal.ZERO) == 0) {
                error += "AmountError";
                hasErrorData = true;
            }

            String tokenId = dto.getTokenId().toUpperCase();
            if (!tokens.contains(tokenId)) {
                error += " TokenError";
                hasErrorData = true;
            }

            if (!hasErrorData && !NumberUtil.isDigits(dto.getAccountId())) {
                Long brokerId = accountClient.getAccountBrokerId(Long.parseLong(dto.getAccountId()));
                if (brokerId == null || brokerId == 0) {
                    error += " AccountError";
                    hasErrorData = true;
                } else {
                    BalanceDetailDTO balanceDetail = assetClient.getBalance(Long.parseLong(dto.getAccountId()), dto.getTokenId());
                    if (balanceDetail == null || balanceDetail.getAvailable().compareTo(new BigDecimal(dto.getAmount())) < 0) {
                        error += " insuffiBalanceError";
                        hasErrorData = true;
                    }
                }
            } else {
                error += " AccountIdError";
                hasErrorData = true;
            }

            if (!StringUtils.isEmpty(error)) {
                columns.add(error);
            } else {
                columns.add("OK");
            }

            int lastRowNum = sheet.getLastRowNum();
            XSSFRow dataRow = sheet.createRow(lastRowNum + 1);
            for (int j = 0; j < columns.size(); j++) {
                XSSFCell cell = dataRow.createCell(j, CellType.STRING);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(columns.get(j));
            }
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            xssfWorkbook.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] content = os.toByteArray();
        InputStream is = new ByteArrayInputStream(content);

        awsPublicObjectStorage.uploadObject(fileKey,  MediaType.MICROSOFT_EXCEL, is, CannedAccessControlList.PublicRead);
        String url = awsPublicStorageConfig.getStaticUrl() + fileKey;
        log.info("url:{}", url);

        long sequenceId = System.currentTimeMillis();
        redisTemplate.opsForValue().set(SEQUENCE_KEY + sequenceId, fileKey, 12, TimeUnit.HOURS);

        if (hasErrorData) {
            result.put("success", false);
            result.put("errorUrl", url);
        } else {
            result.put("success", true);
            result.put("sequenceId", sequenceId + "");
        }
        result.put("echoStr", echoStr);
        return ResultModel.ok(result);
    }

    public List<TmplData> convertTmpl(XSSFWorkbook workbook) {
        List<String> titles = new ArrayList<>();
        XSSFSheet sheetAt = workbook.getSheetAt(0);

        List<TmplData> list = new ArrayList<>();
        for (Row row : sheetAt) {
            if (row.getRowNum() == 0) {
                for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
                    String column = row.getCell(i).toString().trim();
                    titles.add(column);
                }
            } else {
                Cell accountIdCell = row.getCell(titles.indexOf("AID"));
                String accountId = accountIdCell != null ? accountIdCell.toString() : "";

                Cell tokenCell = row.getCell(titles.indexOf("token"));
                String token = tokenCell != null ? tokenCell.toString().trim().replaceAll("\t", "") : "";

                Cell quantityCell = row.getCell(titles.indexOf("amount"));
                String quantity = quantityCell != null ? quantityCell.toString().trim().replaceAll("\t", "") : "";

                if (accountId.trim().equals("") && token.equals("")) {
                    break;
                }

                TmplData tmplData = new TmplData();
                tmplData.setAccountId(accountId);
                tmplData.setTokenId(token);
                tmplData.setAmount(quantity);
                list.add(tmplData);
            }
        }
        return list;
    }
}
