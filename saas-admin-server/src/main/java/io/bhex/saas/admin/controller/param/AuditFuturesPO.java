package io.bhex.saas.admin.controller.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditFuturesPO {
    @NotNull(message = "audit.curState.required")
    private Integer curState;
    @NotNull(message = "audit.toState.required")
    private Integer toState;
    @NotNull(message = "audit.id.required")
    private Long id;
    private String reason;

    @NotEmpty(message = "symbol.record.symbolId.required")
    private String symbolId;

    private String newSymbolId;

    @NotEmpty(message = "symbol.record.baseToken.required")
    private String indexToken;
    @NotEmpty(message = "symbol.record.baseToken.required")
    private String displayIndexToken;
}
