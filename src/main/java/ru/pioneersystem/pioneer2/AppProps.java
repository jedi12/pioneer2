package ru.pioneersystem.pioneer2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;

@Component("appProps")
public class AppProps {

    @Value("${fieldTypeFileAllowed: true}")
    public boolean fieldTypeFileAllowed;

    @Value("${sendNotices.enabled: false}")
    public boolean sendNoticesEnabled;

    @Value("${sendNotices.period: 60000}")
    public int sendNoticesPeriod;

    @Value("${sendNotices.fromEmailAddress: mail1@yandex.ru}")
    public String sendNoticesFromEmailAddress;

    @Value("${sendNotices.fromPersonName: Система Пионер 2}")
    public String sendNoticesFromPersonName;

    @Value("${sendNotices.mailInboxName: INBOX}")
    public String sendNoticesMailInboxName;

    @Value("${sendNotices.mailTrashName: Trash}")
    public String sendNoticesMailTrashName;

    @Value("${sendNotices.incomingMessageMaxSize: 1048576}")
    public int sendNoticesIncomingMessageMaxSize;

    @Value("${sendNotices.smtp.host: smtp.yandex.ru}")
    public String sendNoticesSmtpHost;

    @Value("${sendNotices.smtp.port: 465}")
    public String sendNoticesSmtpPort;

    @Value("${sendNotices.smtp.starttls.enable: false}")
    public String sendNoticesSmtpStarttlsEnable;

    @Value("${sendNotices.smtp.ssl.enable: true}")
    public String sendNoticesSmtpSslEnable;

    @Value("${sendNotices.smtp.auth: true}")
    public String sendNoticesSmtpAuth;

    @Value("${sendNotices.smtp.dsn.notify: SUCCESS,FAILURE,DELAY}")
    public String sendNoticesSmtpDsnNotify;

    @Value("${sendNotices.smtp.dsn.ret: HDRS}")
    public String sendNoticesSmtpDsnRet;

    @Value("${sendNotices.smtp.allow8bitmime: true}")
    public String sendNoticesSmtpAllow8bitmime;

    @Value("${sendNotices.imap.host: imap.yandex.ru}")
    public String sendNoticesImapHost;

    @Value("${sendNotices.imap.port: 993}")
    public String sendNoticesImapPort;

    @Value("${sendNotices.imap.starttls.enable: false}")
    public String sendNoticesImapStarttlsEnable;

    @Value("${sendNotices.imap.ssl.enable: true}")
    public String sendNoticesImapSslEnable;

    @Value("${sendNotices.imap.auth: true}")
    public String sendNoticesImapAuth;

    @Value("${sendNotices.mail.username: mail1@yandex.ru}")
    public String sendNoticesMailUsername;

    @Value("${sendNotices.mail.password: pass}")
    public String sendNoticesMailPassword;

    @Value("${sendNotices.mail.debug: false}")
    public String sendNoticesMailDebug;

    @Value("${default.datePattern: dd.MM.yyyy}")
    public String defaultDatePattern;







    @Value("${processMailCommand.enabled: false}")
    public boolean processMailCommandEnabled;

    @Value("${processMailCommand.period: 6000}")
    public int processMailCommandPeriod;

    @Value("${processMailCommand.mail.username: mail1@yandex.ru}")
    public String processMailCommandMailUsername;

    @Value("${processMailCommand.mail.password: pass}")
    public String processMailCommandMailPassword;

    @Value("${processMailCommand.mail.debug: false}")
    public String processMailCommandMailDebug;





    @Value("${createDocumentsFromMail.enabled: false}")
    public boolean createDocumentsFromMailEnabled;

    @Value("${createDocumentsFromMail.period: 7000}")
    public int createDocumentsFromMailPeriod;


    @Value("${serverBackRef: http://localhost:8080/pioneer2/main.xhtml}")
    public String serverBackRef;






    private DateTimeFormatter defaultDateTimeFormatter;

    @PostConstruct
    public void init() {
        this.defaultDateTimeFormatter = DateTimeFormatter.ofPattern(defaultDatePattern);
    }

    public DateTimeFormatter getDefaultDateTimeFormatter() {
        return defaultDateTimeFormatter;
    }

    public boolean isFieldTypeFileAllowed() {
        return fieldTypeFileAllowed;
    }
}
