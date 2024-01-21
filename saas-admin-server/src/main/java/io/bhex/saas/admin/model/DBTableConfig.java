package io.bhex.saas.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@Table(name = "tb_db_table_config")
public class DBTableConfig {

    @Id
    private Long id;

    private String dbName;
    private String tableName;
    /**
     * 可以用于查询的索引列表每条索引的字段用逗号分隔，多条索引之间用分号分隔
     */
    private String tableIndexs;

}
