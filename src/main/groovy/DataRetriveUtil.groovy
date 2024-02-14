import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatterBuilder
import java.util.Optional

public class DataRetriveUtil extends ExtendM3Utility {

    /**
     * To retrive and validate inputed data
     * @param mi Instance of MI API
     * @param fieldName Name of the field from which the input will be taken to validation
     * @param type Type of inputed value
     * @param defaultValue Value which should be returned in case of wrong data input***
     * @return value in a proper format
     */

    def <T> Optional<T> getInput(String field, Class<T> type, T defaultValue) {
        String value = mi.inData.get(field).trim()
        if (value == null || value.isEmpty()) {
            return Optional.ofNullable(defaultValue)
        }
        T result = null
        if (String.class == type) {
            result = "?".equals(value) ? "" : value
        } else if (Integer.class == type) {
            result = "?".equals(value) ? 0 : Integer.valueOf(value)
        } else if (Long.class == type) {
            result = "?".equals(value) ? 0L : Long.valueOf(value)
        } else if (Double.class == type) {
            result = "?".equals(value) ? 0d : Double.valueOf(value)
        } else if (BigDecimal.class == type) {
            result = "?".equals(value) ? new BigDecimal("0") : new BigDecimal(value)
        } else if (LocalDate.class == type) {
            result = "?".equals(value) ? LocalDate.ofEpochDay(0) : convertDate(value, mi.getDateFormat())
        }
        return Optional.ofNullable(result)
    }

    /**
     * To check and convert date into a proper format
     * @param date Inputed date
     * @param format Format of inputed date
     * @return date value in a proper format
     */

    private LocalDate convertDate(String date, String format) {
        if ("YMD8".equals(format)) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"))
        } else if ("YMD6".equals(format)) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyMMdd"))
        } else if ("MDY6".equals(format)) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("MMddyy"))
        } else if ("DMY6".equals(format)) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("ddMMyy"))
        } else if ("YWD5".equals(format)) {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yywwe"))
        }
        return null
    }
}