package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
@Component
public interface ExchangeTokenMapper  extends tk.mybatis.mapper.common.Mapper<ExchangeToken> {

    @Select("select * from tb_exchange_token where exchange_id = #{exchangeId} and category=#{category}")
    List<ExchangeToken> getListByExchangeId(@Param("exchangeId") Long exchangeId, @Param("category") int category);

    @Select("select * from tb_exchange_token where exchange_id = #{exchangeId} and category=#{category} " +
            "limit #{offset},#{pageSize}")
    List<ExchangeToken> getListByExchangeIdPage(@Param("exchangeId") Long exchangeId, @Param("category") int category,
                                                @Param("offset") int offset, @Param("pageSize") int pageSize, @Param("tokenName") String tokenName);


    @Select("select count(*) from tb_exchange_token where exchange_id = #{exchangeId} and category=#{category} ")
    Integer countByExchangeId(@Param("exchangeId") Long exchangeId, @Param("category") int category, @Param("tokenName") String tokenName);

    @Select("select * from tb_exchange_token where exchange_id = #{exchangeId} and token_id = #{tokenId}")
    ExchangeToken getByExchangeIdAndToken(@Param("exchangeId") Long exchangeId, @Param("tokenId") String tokenId);


    @Update("update tb_exchange_token set status = #{status} " +
            "where  exchange_id = #{exchangeId} and token_id = #{tokenId}")
    int updateStatus(@Param("exchangeId") Long exchangeId, @Param("tokenId") String tokenId,
                     @Param("status") Integer status);

//    @SelectProvider(type = Provider.class, method="queryExchangeTokens")
//    List<ExchangeToken> queryExchangeTokens(@Param("exchangeIds") List<Long> exchangeIds, @Param("tokenId") String tokenId,
//                                            @Param("start") int start, @Param("offset") int offset);

    @SelectProvider(type = Provider.class, method="queryExchangeTokenIds")
    List<String> queryExchangeTokenIds(@Param("exchangeIds") List<Long> exchangeIds, @Param("tokenId") String tokenId,
                                            @Param("start") int start, @Param("offset") int offset, @Param("category") Integer category);

    @SelectProvider(type = Provider.class, method="countExchangeTokens")
    Integer countExchangeTokens(@Param("exchangeIds") List<Long> exchangeIds, @Param("tokenId") String tokenId, @Param("category") Integer category);

    class Provider{

        public String queryExchangeTokenIds(Map<String, Object> parameter) {
            List<Long> exchangeIds = (List<Long>) parameter.get("exchangeIds");
            List<String> strings = exchangeIds.stream().map(exchangeId -> exchangeId+"").collect(Collectors.toList());
            String insql = String.join("," , strings);
            Object tokenId = parameter.get("tokenId");
            Integer category = (Integer) parameter.get("category");
            return new SQL() {
                {
                    SELECT("distinct(token_id)").FROM("tb_exchange_token");
                    WHERE("status = 1")
                    .WHERE("exchange_id in (" + insql + ")");
                    if(tokenId != null && !tokenId.equals("")){
                        WHERE("token_id=#{tokenId}");
                    }
                    if(category != null && category != 0){
                        WHERE("category=#{category}");
                    }
                }
            }.toString() + " limit #{start},#{offset}";
        }

//        public String queryExchangeTokens(Map<String, Object> parameter) {
//            List<Long> exchangeIds = (List<Long>) parameter.get("exchangeIds");
//            List<String> strings = exchangeIds.stream().map(exchangeId -> exchangeId+"").collect(Collectors.toList());
//            String insql = String.join("," , strings);
//            Object tokenId = parameter.get("tokenId");
//            return new SQL() {
//                {
//                    SELECT("*").FROM("tb_exchange_token");
//                    WHERE("status = 1")
//                            .WHERE("exchange_id in (" + insql + ")");
//                    if(tokenId != null && !tokenId.equals("")){
//                        WHERE("token_id=#{tokenId}");
//                    }
//                }
//            }.toString() + " limit #{start},#{offset}";
//        }

        public String countExchangeTokens(Map<String, Object> parameter) {
            List<Long> exchangeIds = (List<Long>) parameter.get("exchangeIds");
            List<String> strings = exchangeIds.stream().map(exchangeId -> exchangeId+"").collect(Collectors.toList());
            String insql = String.join("," , strings);
            Object tokenId = parameter.get("tokenId");
            Integer category = (Integer) parameter.get("category");
            return new SQL() {
                {
                    SELECT("count(distinct(token_id))").FROM("tb_exchange_token");
                    WHERE("status = 1")
                            .WHERE("exchange_id in (" + insql + ")");
                    if(tokenId != null && !tokenId.equals("")) {
                        WHERE("token_id=#{tokenId}");
                    }
                    if(category != null && category != 0) {
                        WHERE("category=#{category}");
                    }
                }
            }.toString();
        }
    }

}
