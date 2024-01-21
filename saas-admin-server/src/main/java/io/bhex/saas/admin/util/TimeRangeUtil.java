package io.bhex.saas.admin.util;


import io.bhex.saas.admin.controller.param.TimeRange;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;
import java.util.Optional;

/**
 * Time range util
 */
@Slf4j
public class TimeRangeUtil {

    private static final String DAY = "d";

    private static final String WEEK = "w";

    private static final String MONTH = "m";

    public static TimeRange getRange(String timeRange, Long startTime, Long endTime) {
        TimeRange range = TimeRangeUtil.getRange(timeRange);
        if (range == null) {
            return null;
        }

        startTime = Optional.ofNullable(startTime).orElse(0L);
        endTime = Optional.ofNullable(endTime).orElse(0L);

        if (range.getStartTime() != null) {
            if (startTime > 0 && startTime < range.getStartTime()) {
                startTime = range.getStartTime();
            }

            if (startTime == 0) {
                startTime = range.getStartTime();
            }
        }

        if (range.getEndTime() != null) {
            if (endTime > 0 && endTime > range.getEndTime()) {
                endTime = range.getEndTime();
            }

            if (endTime == 0) {
                endTime = range.getEndTime();
            }
        }

        return TimeRange.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    /**
     * Get time range
     *
     * @param timeRange time range，其中：d = 天， w = 周， m = 月。 例子：
     *                  最近一周 = 1w
     *                  一周以前 = -1w
     *                  当日 = 1d
     *                  1周 = 1w
     *                  1个月 = 1m
     *                  3个月 = 3m
     * @return time range
     */
    public static TimeRange getRange(String timeRange) {
        if (StringUtils.isEmpty(timeRange)) {
            return null;
        }
        timeRange = timeRange.toLowerCase();
        if (timeRange.endsWith(DAY)) {
            int number = Integer.parseInt(timeRange.split(DAY)[0]);
            return getTimeRange(number);
        }

        if (timeRange.endsWith(WEEK)) {
            int number = Integer.parseInt(timeRange.split(WEEK)[0]);
            return getTimeRange(number * 7);
        }

        if (timeRange.endsWith(MONTH)) {
            int number = Integer.parseInt(timeRange.split(MONTH)[0]);
            return getTimeRange(number * 30);
        }
        return null;
    }

    /**
     * 最近 days 天
     *
     * @param days day numbers
     * @return time range
     */
    private static TimeRange getTimeRange(int days) {
        if (days < 0) {
            return getTimeRangeBefore(Math.abs(days));
        }
        return TimeRange.builder()
                .startTime(DateUtils.addDays(new Date(), -days).getTime())
                .endTime(System.currentTimeMillis())
                .build();
    }

    /**
     * days 天以前
     *
     * @param days day numbers
     * @return time range
     */
    private static TimeRange getTimeRangeBefore(int days) {
        return TimeRange.builder()
                .startTime(null)
                .endTime(DateUtils.addDays(new Date(), -days).getTime())
                .build();
    }

}
