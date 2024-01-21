package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.DepositReceiptApplyRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author JinYuYuan
 * @description
 * @date 2021-01-15 14:02
 */
@Mapper
@Component
public interface DepositReceiptApplyRecordMapper extends tk.mybatis.mapper.common.Mapper<DepositReceiptApplyRecord> {
    @Select("select * from tb_deposit_receipt_apply_record where org_id = #{orgId} and order_id = #{orderId};")
    DepositReceiptApplyRecord getReceiptApplyByOrderId(@Param("orgId")Long orgId, @Param("orderId")Long orderId);

    @Select("select order_id from tb_deposit_receipt_apply_record where org_id = #{orgId}")
    List<Long> queryReceiptApplyOrderIds(@Param("orgId")Long orgId);

    @Delete("delete from tb_deposit_receipt_apply_record where org_id = #{orgId} and order_id = #{orderId}")
    int deleteReceiptApply(@Param("orgId")Long orgId,@Param("orderId")Long orderId);

    @Select("select order_id from tb_deposit_receipt_apply_record where org_id = #{orgId}")
    List<Long> queryReceiptApplyOrderIdsByOrgId(@Param("orgId")Long orgId);
}
