package io.bhex.saas.admin.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
import com.google.protobuf.TextFormat;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.token.TokenDetailInfo;
import io.bhex.base.token.TokenTypeEnum;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.bizlog.ExcludeLogAnnotation;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.bhop.common.util.validation.ValidUtil;
import io.bhex.broker.common.objectstorage.CannedAccessControlList;
import io.bhex.broker.common.objectstorage.ObjectStorage;
import io.bhex.broker.common.objectstorage.ObjectStorageUtil;
import io.bhex.broker.common.util.FileUtil;
import io.bhex.broker.grpc.admin.AddTokenReply;
import io.bhex.broker.grpc.common.AdminSimplyReply;
import io.bhex.saas.admin.config.AwsPublicStorageConfig;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.constants.OpTypeConstant;
import io.bhex.saas.admin.controller.dto.BrokerTokenDTO;
import io.bhex.saas.admin.controller.dto.TokenRecordDTO;
import io.bhex.saas.admin.controller.param.*;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.service.BrokerTokenService;
import io.bhex.saas.admin.service.ImageUtilService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/broker_token")
public class BrokerTokenController {

    @Autowired
    private BrokerTokenService brokerTokenService;
    public static final String IMG_FILE_TYPES = "jpg,png,jpeg,svg";

    @Autowired
    private AwsPublicStorageConfig awsPublicStorageConfig;
    @Autowired
    @Qualifier("objectPublicStorage")
    private ObjectStorage awsPublicObjectStorage;
    @Autowired
    private ImageUtilService imageUtilService;
    @Autowired
    private BhOrgClient bhOrgClient;

    @GetMapping("/detail")
    public ResultModel<TokenRecordDTO> queryTokenDetail(@RequestParam Long brokerId,
                                                   @RequestParam String tokenId) {
        return ResultModel.ok(brokerTokenService.getTokenDetailInfo(brokerId, tokenId));
    }

    @ExcludeLogAnnotation
    @RequestMapping(value = "/icon", method = RequestMethod.POST)
    public ResultModel uploadImage(@RequestParam(name = "uploadFile") MultipartFile uploadImageFile,
                                   @RequestParam(required = false) String label)
        throws Exception {
        String fileType = FileUtil.getFileSuffix(uploadImageFile.getOriginalFilename(), "");
        if (Strings.isNullOrEmpty(fileType) || !IMG_FILE_TYPES.contains(fileType)) {
            throw new BizException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        String suffix = fileType.toLowerCase();
        String fileKey = awsPublicStorageConfig.getPrefix() + ObjectStorageUtil
            .sha256FileName(uploadImageFile.getBytes(), suffix);
        awsPublicObjectStorage.uploadObject(fileKey, ObjectStorageUtil.getFileContentType(suffix,
            MediaType.ANY_IMAGE_TYPE),
            uploadImageFile.getInputStream(), CannedAccessControlList.PublicRead);
        String url = imageUtilService.getImageUrl(fileKey);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("label", label);
        log.info("image url: {}, label: {}", url, label);
        return ResultModel.ok(result);
    }

    @PostMapping("/change_token_apply_broker")
    public ResultModel changeTokenApplyBroker(@RequestBody @Validated ChangeTokenBrokerPO po) {
        brokerTokenService.changeTokenBroker(po.getBrokerId(), po.getTokenId(), po.getToBrokerId());
        return ResultModel.ok();
    }

    @BussinessLogAnnotation
    @PostMapping("/apply_audit")
    public ResultModel auditTokenApplication(@RequestBody @Validated TokenAuditPO auditPO)
        throws MissingServletRequestParameterException {
        ApplyStateEnum curStateEnum = ApplyStateEnum.getByState(auditPO.getCurState());
        ApplyStateEnum toStateEnum = ApplyStateEnum.getByState(auditPO.getToState());
        if (curStateEnum.equals(toStateEnum) && curStateEnum != ApplyStateEnum.ACCEPT) { //已通过的可以修改
            return ResultModel.ok();
        }

        TokenRecordDTO tokenRecordDTO = brokerTokenService.getTokenRecordById(-1L, auditPO.getId());

        TokenDetailInfo bhOldTokenInfo = brokerTokenService.getBhTokenInfo(tokenRecordDTO.getTokenId());
        if (!bhOldTokenInfo.getTokenName().equals("")) {
            if (!auditPO.getTokenId().equalsIgnoreCase(tokenRecordDTO.getTokenId())) {
                return ResultModel.error("can't change tokenId");
            }
            if (!auditPO.getTokenName().equalsIgnoreCase(tokenRecordDTO.getTokenName())) {
                //return ResultModel.error("can't change tokenName");
            }
            if (!auditPO.getTokenFullName().equalsIgnoreCase(tokenRecordDTO.getTokenFullName())) {
                //return ResultModel.error("can't change tokenFullName");
            }
            if (!auditPO.getIsPrivateToken() == tokenRecordDTO.getIsPrivateToken()) {
                //return ResultModel.error("can't change isPrivateToken");
            }
        }

        auditPO.setTokenId(auditPO.getTokenId().toUpperCase());
        auditPO.setTokenName(auditPO.getTokenName().toUpperCase());


        //待审核的币种可以修改tokenId 但是平台不能存在
        if (tokenRecordDTO.getState() == ApplyStateEnum.APPLYING.getState()) {
            if (!auditPO.getTokenId().equals(tokenRecordDTO.getTokenId())) { //修改tokenId了
                TokenDetailInfo bhNewTokenInfo = brokerTokenService.getBhTokenInfo(auditPO.getTokenId());
                if (!bhNewTokenInfo.getTokenName().equals("")) { //不能修改成平台已经存在此币种了
                    return ResultModel.error("can't change tokenId to : " + auditPO.getTokenId() + ", existed in platform!");
                }
            }
        } else {
            auditPO.setNeedTag(tokenRecordDTO.getNeedTag() == 1);
        }

        auditPO.setCurState(curStateEnum.getState());
        auditPO.setToState(toStateEnum.getState());



        TokenTypeEnum tokenTypeEnum = TokenTypeEnum.forNumber(auditPO.getTokenType());
        if (!TokenTypeEnum.CHAIN_TOKEN.equals(tokenTypeEnum) && StringUtils.isEmpty(auditPO.getContractAddress())) {
            return ResultModel.error("token.record.contractAddress.required");
        } else if (TokenTypeEnum.CHAIN_TOKEN.equals(tokenTypeEnum)) {
            auditPO.setContractAddress(StringUtils.EMPTY);
        }
        if (auditPO.getToState() == ApplyStateEnum.ACCEPT.getState()) {
            if (Objects.isNull(auditPO.getConfirmCount())) {
                throw new MissingServletRequestParameterException("confirmCount", "Number");
            }
//            if (Objects.isNull(auditPO.getCanWithdrawConfirmCount())) {
//                throw new MissingServletRequestParameterException("canWithdrawConfirmCount", "Number");
//            }
            if (Objects.isNull(auditPO.getExploreUrl())) {
                throw new MissingServletRequestParameterException("exploreUrl", "String");
            }
            if (Objects.isNull(auditPO.getMinPrecision())) {
                throw new MissingServletRequestParameterException("minPrecision", "Number");
            }
            if (Objects.isNull(auditPO.getFeeToken())) {
                throw new MissingServletRequestParameterException("feeToken", "Number");
            }
        }
//        auditPO.setChainName("");
//        auditPO.setParentTokenId("");
//        auditPO.setChainSequence(0);
        brokerTokenService.auditTokenRecord(auditPO, tokenRecordDTO.getBrokerId());
        return ResultModel.ok();
    }

    @PostMapping("/apply_list")
    public ResultModel<PaginationVO<TokenRecordDTO>> listTokenApplyRecords(@RequestBody @Valid QueryTokenApplicationsPO po) {
        if (StringUtils.isNotEmpty(po.getToken()) && !ValidUtil.isTokenName(po.getToken())) {
            return ResultModel.ok();
        }
        if (StringUtils.isNotEmpty(po.getContractAddress()) && !ValidUtil.isSimpleInput(po.getContractAddress())) {
            return ResultModel.ok();
        }
        ApplyStateEnum stateEnum = ApplyStateEnum.getByState(po.getState());
        return ResultModel.ok(brokerTokenService.applicationList(po.getBrokerId(), po.getCurrent(), po.getPageSize(),
                stateEnum.getState(), po.getToken(), po.getContractAddress()));
    }

    @GetMapping("/application")
    public ResultModel<TokenRecordDTO> queryApplication(@RequestParam long brokerId, @RequestParam String tokenId) {
        return ResultModel.ok(brokerTokenService.getTokenRecord(brokerId, tokenId));
    }

    @RequestMapping(value = "/query_tokens")
    public ResultModel<BrokerTokenDTO> queryTokens(@RequestBody QueryBrokerTokensPO po) {
        if (StringUtils.isNotEmpty(po.getToken()) && !ValidUtil.isTokenName(po.getToken().toUpperCase())) {
            return  ResultModel.ok();
        }
        Long brokerId = po.getBrokerId();
        PaginationVO<BrokerTokenDTO> vo = brokerTokenService.querySaasBrokerTokens(brokerId, po.getCurrent(),
            po.getPageSize(), po.getCategory(), po.getToken());

        return ResultModel.ok(vo);
    }

    //查询本交易所上过的币
    @RequestMapping(value = "/query_my_tokens")
    public ResultModel<BrokerTokenDTO> queryMyTokens(@RequestBody QueryBrokerTokensPO po) {

        PaginationVO<BrokerTokenDTO> vo = brokerTokenService.queryMyBrokerTokens(po.getBrokerId(), po.getCurrent(),
                po.getPageSize(), po.getCategory(), po.getToken());
        return ResultModel.ok(vo);
    }


//    @BussinessLogAnnotation(name = OpTypeConstant.TOKEN_ALLOW_SHOW)
//    @RequestMapping(value = "/init_broker_token", method = RequestMethod.POST)
//    public ResultModel initBrokerToken(@RequestBody @Valid EditBrokerTokenPO po) {
//        BatchAllowTokenPO batchAllowTokenPO = new BatchAllowTokenPO();
//        batchAllowTokenPO.setBrokers(Lists.newArrayList(po.getBrokerId()));
//        batchAllowTokenPO.setTokens(Lists.newArrayList(po.getTokenId()));
//        batchAllowTokenPO.setCategory(po.getCategory());
//        return batchAllowShow(batchAllowTokenPO);
//    }

    @RequestMapping(value = "/batch_allow_show", method = RequestMethod.POST)
    public ResultModel batchAllowShow(@RequestBody @Valid BatchAllowTokenPO po) {
        List<String> tokens = po.getTokens().stream().map(t -> t.toUpperCase()).collect(Collectors.toList());
        for (String tokenId : tokens) {
            for (Long brokerId : po.getBrokers()) {
                TokenDetailInfo tokenDetailInfo = brokerTokenService.getBhTokenInfo(tokenId);
                if (tokenDetailInfo.getPrivateTokenBrokerId() > 0 && tokenDetailInfo.getPrivateTokenBrokerId() != brokerId) {
                    ExchangeReply sourceExchange = bhOrgClient.findExchangeByBrokerId(tokenDetailInfo.getPrivateTokenBrokerId());
                    ExchangeReply targetExchange = bhOrgClient.findExchangeByBrokerId(brokerId);
                    if (sourceExchange.getExchangeId() != targetExchange.getExchangeId()) {
                        log.warn("private token {} {} not the same exchange", tokenDetailInfo.getPrivateTokenBrokerId(), brokerId);
                        return ResultModel.error(tokenId + " is private token, owner:" + tokenDetailInfo.getPrivateTokenBrokerId());
                    }
                }
                AddTokenReply reply = brokerTokenService.initBrokerToken(brokerId, tokenId, po.getCategory());
                log.info("{} {} {}", brokerId, tokenId, TextFormat.shortDebugString(reply));
            }
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/batch_forbid_show", method = RequestMethod.POST)
    public ResultModel batchForbidShow(@RequestBody @Valid BatchAllowTokenPO po) {
//        for (String tokenId : po.getTokens()) {
//            for (Long brokerId : po.getBrokers()) {
//                AdminSimplyReply reply = brokerTokenService.deleteBrokerToken(brokerId, tokenId);
//                if (!reply.getResult()) {
//                    return ResultModel.error(reply.getMessage());
//                }
//            }
//        }
        return ResultModel.ok();
    }

//    @BussinessLogAnnotation(name = OpTypeConstant.TOKEN_FORBID_SHOW)
//    @RequestMapping(value = "/forbid_show", method = RequestMethod.POST)
//    public ResultModel forbidPublish(@RequestBody @Valid EditBrokerTokenPO po) {
//        AdminSimplyReply reply = brokerTokenService.deleteBrokerToken(po.getBrokerId(), po.getTokenId());
//        if (!reply.getResult()) {
//            return ResultModel.error(reply.getMessage());
//        }
//        return ResultModel.ok();
//    }

}
