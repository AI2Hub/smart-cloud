package org.smartframework.cloud.utility.test.unit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smartframework.cloud.utility.DateUtil;
import org.smartframework.cloud.utility.constant.DateFormartConst;

import java.util.Date;

public class DateUtilUnitTest {

    public void testNow() {
        Assertions.assertThat(DateUtil.now()).isNotNull();
    }

    @Test
    public void testGetCurrentDate() {
        String currentDateStr = DateUtil.getCurrentDate();
        Assertions.assertThat(currentDateStr).isNotBlank();
        Assertions.assertThat(currentDateStr.length()).isEqualTo(DateFormartConst.DATE.length());
    }

    @Test
    public void testGetCurrentDateTime() {
        String currentDateStr = DateUtil.getCurrentDateTime();
        Assertions.assertThat(currentDateStr).isNotBlank();
        Assertions.assertThat(currentDateStr.length()).isEqualTo(DateFormartConst.DATETIME.length());
    }

    @Test
    public void testGetCurrentDateTimeWithFormat() {
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.YYYY).length())
                .isEqualTo(DateFormartConst.YYYY.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.YYYY_MM).length())
                .isEqualTo(DateFormartConst.YYYY_MM.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.DATE).length())
                .isEqualTo(DateFormartConst.DATE.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.DATE_HH).length())
                .isEqualTo(DateFormartConst.DATE_HH.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.DATE_HH_MM).length())
                .isEqualTo(DateFormartConst.DATE_HH_MM.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.DATETIME).length())
                .isEqualTo(DateFormartConst.DATETIME.length());
        Assertions.assertThat(DateUtil.getCurrentDateTime(DateFormartConst.DATETIME_SSS).length())
                .isEqualTo(DateFormartConst.DATETIME_SSS.length());
    }

    @Test
    public void testFormat() {
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.YYYY).length())
                .isEqualTo(DateFormartConst.YYYY.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.YYYY_MM).length())
                .isEqualTo(DateFormartConst.YYYY_MM.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.DATE).length())
                .isEqualTo(DateFormartConst.DATE.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.DATE_HH).length())
                .isEqualTo(DateFormartConst.DATE_HH.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.DATE_HH_MM).length())
                .isEqualTo(DateFormartConst.DATE_HH_MM.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.DATETIME).length())
                .isEqualTo(DateFormartConst.DATETIME.length());
        Assertions.assertThat(DateUtil.format(new Date(), DateFormartConst.DATETIME_SSS).length())
                .isEqualTo(DateFormartConst.DATETIME_SSS.length());
    }

    @Test
    public void testFormatMillis() {
        long currentMillis = new Date().getTime();
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.YYYY).length())
                .isEqualTo(DateFormartConst.YYYY.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.YYYY_MM).length())
                .isEqualTo(DateFormartConst.YYYY_MM.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.DATE).length())
                .isEqualTo(DateFormartConst.DATE.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.DATE_HH).length())
                .isEqualTo(DateFormartConst.DATE_HH.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.DATE_HH_MM).length())
                .isEqualTo(DateFormartConst.DATE_HH_MM.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.DATETIME).length())
                .isEqualTo(DateFormartConst.DATETIME.length());
        Assertions.assertThat(DateUtil.format(currentMillis, DateFormartConst.DATETIME_SSS).length())
                .isEqualTo(DateFormartConst.DATETIME_SSS.length());
    }

    @Test
    public void testFormatDate() {
        Assertions.assertThat(DateUtil.formatDate(new Date()).length()).isEqualTo(DateFormartConst.DATE.length());
    }

    @Test
    public void testFormatDateMillis() {
        Assertions.assertThat(DateUtil.formatDate(new Date().getTime()).length()).isEqualTo(DateFormartConst.DATE.length());
    }

    @Test
    public void testFormatDateTime() {
        Assertions.assertThat(DateUtil.formatDateTime(new Date()).length())
                .isEqualTo(DateFormartConst.DATETIME.length());
    }

    @Test
    public void testFormatDateTimeMillis() {
        Assertions.assertThat(DateUtil.formatDateTime(new Date().getTime()).length())
                .isEqualTo(DateFormartConst.DATETIME.length());
    }

    @Test
    public void testToDate() {
        Assertions.assertThat(DateUtil.toDate("2019")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01-01")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01-01 11")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01-01 11:11")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01-01 11:11:11")).isNotNull();
        Assertions.assertThat(DateUtil.toDate("2019-01-01 11:11:11.111")).isNotNull();
    }

    @Test
    public void testToCurrentMillis() {
        Assertions.assertThat(DateUtil.toCurrentMillis("2019")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01-01")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01-01 11")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01-01 11:11")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01-01 11:11:11")).isNotNull();
        Assertions.assertThat(DateUtil.toCurrentMillis("2019-01-01 11:11:11.111")).isNotNull();
    }

    @Test
    public void testToDateCurrentMillis() {
        Assertions.assertThat(DateUtil.toDate(new Date().getTime())).isNotNull();
    }

}