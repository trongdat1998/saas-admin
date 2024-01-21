package io.bhex.saas.admin.controller.param;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class TimeRange {

    private Long startTime;

    private Long endTime;
}
