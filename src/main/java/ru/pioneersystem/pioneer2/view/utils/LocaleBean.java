package ru.pioneersystem.pioneer2.view.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LocaleBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Locale locale;
    private ZoneId zoneId;
    private String datePattern;
    private String dateTimePattern;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private LocaleResolver localeResolver;

    @Autowired
    public LocaleBean(HttpServletRequest request, HttpServletResponse response, LocaleResolver localeResolver) {
        this.request = request;
        this.response = response;
        this.localeResolver = localeResolver;
    }

    @PostConstruct
    public void init()  {
        locale = localeResolver.resolveLocale(request);

        switch (locale.getCountry()) {
            case "RU":
                datePattern = "dd.MM.yyyy";
                dateTimePattern = "dd.MM.yyyy HH:mm:ss";
                break;

            default:
                datePattern = "dd.MM.yyyy";
                dateTimePattern = "dd.MM.yyyy HH:mm:ss";
                break;
        }

        dateFormatter = DateTimeFormatter.ofPattern(datePattern);
        dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
    }

    public void setLanguage(String language) {
        locale = new Locale(language);
        localeResolver.setLocale(request, response, locale);
    }

    public void setTimezoneOffsetMinutes(int timezoneOffsetMinutes) {
        zoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofTotalSeconds(timezoneOffsetMinutes * 60));
    }

    public Locale getLocale() {
        return locale;
    }

    public String getLanguage() {
        return locale.getLanguage();
    }

    public String getCountry() {
        return locale.getCountry();
    }

    public int getTimezoneOffsetMinutes() {
        return 0;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String getDatePattern() {
        return datePattern;
    }

    public String getDateTimePattern() {
        return dateTimePattern;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }
}