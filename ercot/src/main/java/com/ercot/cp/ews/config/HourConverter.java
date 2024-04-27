package com.ercot.cp.ews.config;

import com.opencsv.bean.AbstractBeanField;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;

@Log4j2
public class HourConverter extends AbstractBeanField {

    @Override
    protected Integer convert(String time) {
        return Arrays.stream(time.trim().split(":"))
            .findFirst()
            .map(Integer::parseInt)
            .orElse(0);
    }
}