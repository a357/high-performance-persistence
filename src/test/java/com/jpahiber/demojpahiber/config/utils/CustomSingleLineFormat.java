package com.jpahiber.demojpahiber.config.utils;

import com.p6spy.engine.common.P6Util;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.apache.logging.log4j.util.Strings;

/**
 * @see !!!!!!!!!!! resources/spy.properties
 *  logMessageFormat=com.jpahiber.demojpahiber.util.CustomSingleLineFormat
 * */
public class CustomSingleLineFormat implements MessageFormattingStrategy {

    public CustomSingleLineFormat() {
    }

    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        var sqlSL = P6Util.singleLine(sql);
        return String.format("%s |%2s | %9s | connection %s | %s", now, elapsed, category, connectionId, Strings.isBlank(sqlSL) ? "" : "sql " + sqlSL);
    }

}
