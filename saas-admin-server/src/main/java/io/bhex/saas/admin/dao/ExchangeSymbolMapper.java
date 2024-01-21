package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeSymbol;
import io.bhex.saas.admin.model.ExchangeToken;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
@Component
public interface ExchangeSymbolMapper  extends tk.mybatis.mapper.common.Mapper<ExchangeSymbol> {
    @Select("select * from tb_exchange_symbol where exchange_id = #{exchangeId} and symbol_id = #{symbolId}")
    ExchangeSymbol getByExchangeIdAndSymbol(@Param("exchangeId") Long exchangeId, @Param("symbolId") String symbolId);

    @Select("select * from tb_exchange_symbol where exchange_id = #{exchangeId} and category=#{category}")
    List<ExchangeSymbol> getListByExchangeId(@Param("exchangeId") Long exchangeId, @Param("category") int category);

    @Update("update tb_exchange_symbol set status = #{status} " +
            "where  exchange_id = #{exchangeId} and symbol_id = #{symbolId}")
    int updateStatus(@Param("exchangeId") Long exchangeId,@Param("symbolId") String symbolId,
                     @Param("status") Integer status);

    @SelectProvider(type = Provider.class, method="queryExchangeSymbols")
    List<ExchangeSymbol> queryExchangeSymbols(@Param("exchangeIds") List<Long> exchangeIds, @Param("symbolId") String symbolId,
                                              @Param("myAgentSymbol") Boolean myAgentSymbol, @Param("category") Integer category,
                                            @Param("start") int start, @Param("offset") int offset);

    @SelectProvider(type = Provider.class, method="countExchangeSymbols")
    Integer countExchangeSymbols(@Param("exchangeIds") List<Long> exchangeIds, @Param("symbolId") String symbolId, @Param("myAgentSymbol") Boolean myAgentSymbol, @Param("category") Integer category);

    class Provider{

        public String queryExchangeSymbols(Map<String, Object> parameter) {
            List<Long> exchangeIds = (List<Long>) parameter.get("exchangeIds");
            Boolean myAgentSymbol = (Boolean) parameter.get("myAgentSymbol");
            List<String> strings = exchangeIds.stream().map(exchangeId -> exchangeId+"").collect(Collectors.toList());
            Integer category = (Integer) parameter.get("category");
            String insql = String.join("," , strings);
            Object symbolId = parameter.get("symbolId");
            return new SQL() {
                {
                    SELECT("*").FROM("tb_exchange_symbol");
//                    if(myAgentSymbol){
//                        WHERE("status = 1");
//                    }
                    WHERE("status = 1");
                     WHERE("exchange_id in (" + insql + ")");
                    if(symbolId != null && !symbolId.equals("")){
                        WHERE("symbol_id=#{symbolId}");
                    }
                    if(category != null && !category.equals(0)){
                        WHERE("category=#{category}");
                    }
                }
            }.toString(); // + " limit #{start},#{offset}";
        }

        public String countExchangeSymbols(Map<String, Object> parameter) {
            List<Long> exchangeIds = (List<Long>) parameter.get("exchangeIds");
            List<String> strings = exchangeIds.stream().map(exchangeId -> exchangeId+"").collect(Collectors.toList());
            Integer category = (Integer) parameter.get("category");
            String insql = String.join("," , strings);
            Object symbolId = parameter.get("symbolId");
            return new SQL() {
                {
                    SELECT("count(*)").FROM("tb_exchange_symbol");
                    WHERE("status = 1")
                            .WHERE("exchange_id in (" + insql + ")");
                    if(symbolId != null && !symbolId.equals("")){
                        WHERE("symbol_id=#{symbolId}");
                    }
                    if(category != null && !category.equals("0")){
                        WHERE("category=#{category}");
                    }
                }
            }.toString();
        }
    }

}
