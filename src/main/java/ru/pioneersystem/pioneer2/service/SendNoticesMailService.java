package ru.pioneersystem.pioneer2.service;

import com.sun.mail.dsn.DeliveryStatus;
import com.sun.mail.dsn.MessageHeaders;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.AppProps;
import ru.pioneersystem.pioneer2.dao.DocumentDao;
import ru.pioneersystem.pioneer2.dao.FileDao;
import ru.pioneersystem.pioneer2.dao.MailProcessDao;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.model.File;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.Part;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.util.*;

@Service("sendNoticesMailService")
public class SendNoticesMailService {
    private static final String EMAIL_SEND_ID = "ESID";
    private MailProcessDao mailProcessDao;
    private EventService eventService;
    private DictionaryService dictionaryService;
    private DocumentDao documentDao;
    private FileDao fileDao;
    private MessageSource messageSource;
    private Locale systemLocale;
    private AppProps appProps;
    private Configuration freemarkerConfiguration;

    private boolean initialized;

    private Session mailSession;
    private Transport mailTransport;
    private Store mailStore;

    @Autowired
    public SendNoticesMailService(MailProcessDao mailProcessDao, EventService eventService,
                                  DictionaryService dictionaryService, DocumentDao documentDao,
                                  FileDao fileDao, MessageSource messageSource, Locale systemLocale,
                                  AppProps appProps, Configuration freemarkerConfiguration) {
        this.mailProcessDao = mailProcessDao;
        this.eventService = eventService;
        this.dictionaryService = dictionaryService;
        this.documentDao = documentDao;
        this.fileDao = fileDao;
        this.messageSource = messageSource;
        this.systemLocale = systemLocale;
        this.appProps = appProps;
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public void reInitialize() {
        initialized = false;
    }

    public boolean isInitialized() {
        if (initialized) {
            return true;
        }

        try {
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", appProps.sendNoticesSmtpHost);
            props.setProperty("mail.smtp.port", appProps.sendNoticesSmtpPort);
            props.setProperty("mail.smtp.starttls", appProps.sendNoticesSmtpStarttlsEnable);
            props.setProperty("mail.smtp.ssl.enable", appProps.sendNoticesSmtpSslEnable);
            props.setProperty("mail.smtp.auth", appProps.sendNoticesSmtpAuth);
            props.setProperty("mail.smtp.dsn.notify", appProps.sendNoticesSmtpDsnNotify);
            props.setProperty("mail.smtp.dsn.ret", appProps.sendNoticesSmtpDsnRet);
            props.setProperty("mail.smtp.allow8bitmime", appProps.sendNoticesSmtpAllow8bitmime);
            props.setProperty("mail.imap.host", appProps.sendNoticesImapHost);
            props.setProperty("mail.imap.port", appProps.sendNoticesImapPort);
            props.setProperty("mail.imap.starttls", appProps.sendNoticesImapStarttlsEnable);
            props.setProperty("mail.imap.ssl.enable", appProps.sendNoticesImapSslEnable);
            props.setProperty("mail.imap.auth", appProps.sendNoticesImapAuth);
            props.setProperty("mail.username", appProps.sendNoticesMailUsername);
            props.setProperty("mail.password", appProps.sendNoticesMailPassword);
            props.setProperty("mail.debug", appProps.sendNoticesMailDebug);

            mailSession = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(appProps.sendNoticesMailUsername, appProps.sendNoticesMailPassword);
                }
            });

            mailTransport = mailSession.getTransport("smtp");
            mailTransport.connect();

            mailStore = mailSession.getStore("imap");
            mailStore.connect();
            //Folder[] folders = mailStore.getDefaultFolder().list("*");
            Folder inboxFolder = mailStore.getFolder(appProps.sendNoticesMailInboxName);
            Folder deleteFolder = mailStore.getFolder(appProps.sendNoticesMailTrashName);
            if (!inboxFolder.exists() || !deleteFolder.exists()) {
                String mess = messageSource.getMessage("error.mailProcess.NotExistsTrashFolder",
                        new Object[]{appProps.sendNoticesMailTrashName}, systemLocale);
                eventService.logMailError(mess, null);
                return false;
            }

            initialized = true;
            return true;
        } catch (Exception e) {
            String mess = messageSource.getMessage("error.mailProcess.NotInitialized", null, systemLocale);
            eventService.logMailError(mess, e.getMessage());
            return false;
        } finally {
            try {
                if (mailTransport != null) { mailTransport.close(); }
                if (mailStore != null) { mailStore.close(); }
            } catch (Exception e) {
                eventService.logMailError(e.getMessage(), null);
            }
        }
    }

    public void startProcessing() {
        processingSendNotices();
        processingDeliveryStatuses();
    }

    private void processingSendNotices() {
        try {
            List<Notice> notices = mailProcessDao.getForSendList();
            if (notices.isEmpty()) {
                return;
            }

            mailTransport.connect();

            for (Notice notice : notices) {
                String docStatusName = dictionaryService.getLocalizedStatusName(notice.getDocStatusId(), systemLocale);
                if (docStatusName != null) {
                    notice.setDocStatusName(docStatusName);
                }

                try {
                    String mailBody = null;
                    List<File> mailAttachments = null;
                    switch (notice.getEventId()) {
                        case Event.Type.NOTICE_DOC_RECEIVED:
                            if (appProps.processMailCommandEnabled) {
                                mailBody = createHtmlReceivedNoticeWithDocument(notice);
                                mailAttachments = getDocumentFiles(notice);
                            } else {
                                mailBody = createHtmlReceivedNotice(notice);
                            }

                            break;
                        case Event.Type.NOTICE_DOC_STATUS_CHANGED:
                            mailBody = createHtmlStatusChangedNotice(notice);
                            break;
                    }

                    MimeMessage mimeMessage = createMimeMessage(notice, mailBody, mailAttachments);
                    mailTransport.sendMessage(mimeMessage, InternetAddress.parse(notice.getEmail()));

                    notice.setSendStatusId(Notice.Status.SENDED);
                } catch (ServiceException e) {
                    notice.setSendStatusId(Notice.Status.PREPARED_TO_SENDED);
                    String mess = e.getCause().getMessage();
                    if (mess.length() > 2000) {
                        mess = mess.substring(0, 2000);
                    }
                    notice.setInfo(mess);
                } catch (MessagingException e) {
                    notice.setSendStatusId(Notice.Status.PREPARED_TO_SENDED);
                    String mess = e.getMessage();
                    if (mess.length() > 2000) {
                        mess = mess.substring(0, 2000);
                    }
                    notice.setInfo(mess);
                }

                notice.setAttempt(notice.getAttempt() + 1);
                notice.setChangeDate(new Date());
                mailProcessDao.updateEmailSend(notice);
            }
        } catch (DataAccessException | MessagingException e) {
            String mess = messageSource.getMessage("error.mailProcess.CancelProcessing", null, systemLocale);
            eventService.logMailError(mess, e.getMessage());
        } finally {
            try {
                mailTransport.close();
            } catch (MessagingException e) {
                // Соединение с почтовым сервером будет закрыто даже если ошибка
            }
        }
    }

    private String createHtmlReceivedNoticeWithDocument(Notice notice) {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("notice", notice);

            List<Document.Field> fields = documentDao.getDocFields(notice.getDocId());
            root.put("fields", fields);

            // TODO: 14.09.2017 Чтобы добавить в уведомление маршрут, нужно решить проблему с хранением часовой зоны пользователя
//            List<RoutePoint> routePoints = routeProcessService.getDocumentRoute(notice.getDocId());
//            root.put("routePoints", routePoints);

            root.put("serverBackRef", appProps.serverBackRef);
            root.put("emailForCommandProcessing", appProps.processMailCommandMailUsername);

            // TODO: 15.09.2017 для локализации (выбора) шаблонов уведомлений нужно хранить локаль по умолчанию для каждого пользователя
            Template mailTemplate = freemarkerConfiguration.getTemplate("ru/doc_received_with_doc.html");
//            Writer out = new OutputStreamWriter(new FileOutputStream("D:\\Temp\\Temp\\template.html"), StandardCharsets.UTF_8);
            StringWriter out = new StringWriter();
            mailTemplate.process(root, out);

            return out.getBuffer().toString();
        } catch (TemplateException | IOException | DataAccessException e) {
            String mess = messageSource.getMessage("error.mailProcess.NotCreatedNotice", null, systemLocale);
            eventService.logMailError(mess, e.getMessage(), notice.getId());
        }

        return "Получен документ";
    }

    private String createHtmlReceivedNotice(Notice notice) {
        try {
            Map<String, Object> root = new HashMap<>();
            root.put("notice", notice);

            root.put("serverBackRef", appProps.serverBackRef);

            // TODO: 15.09.2017 для локализации (выбора) шаблонов уведомлений нужно хранить локаль по умолчанию для каждого пользователя
            Template mailTemplate = freemarkerConfiguration.getTemplate("ru/doc_received.html");
//            Writer out = new OutputStreamWriter(new FileOutputStream("D:\\Temp\\Temp\\template.html"), StandardCharsets.UTF_8);
            StringWriter out = new StringWriter();
            mailTemplate.process(root, out);

            return out.getBuffer().toString();
        } catch (TemplateException | IOException e) {
            String mess = messageSource.getMessage("error.mailProcess.NotCreatedNotice", null, systemLocale);
            eventService.logMailError(mess, e.getMessage(), notice.getId());
        }

        return "Получен документ";
    }

    private String createHtmlStatusChangedNotice(Notice notice) {
        try {
            Map<String, Object> root = new HashMap<>();
            notice.setMenuId(Menu.Id.MY_DOCS);
            root.put("notice", notice);

            // TODO: 14.09.2017 Чтобы добавить в уведомление маршрут, нужно решить проблему с хранением часовой зоны пользователя
//            List<RoutePoint> routePoints = routeProcessService.getDocumentRoute(notice.getDocId());
//            root.put("routePoints", routePoints);

            root.put("serverBackRef", appProps.serverBackRef);

            // TODO: 15.09.2017 для локализации (выбора) шаблонов уведомлений нужно хранить локаль по умолчанию для каждого пользователя
            Template mailTemplate = freemarkerConfiguration.getTemplate("ru/doc_status_changed.html");
//            Writer out = new OutputStreamWriter(new FileOutputStream("D:\\Temp\\Temp\\template.html"), StandardCharsets.UTF_8);
            StringWriter out = new StringWriter();
            mailTemplate.process(root, out);

            return out.getBuffer().toString();
        } catch (TemplateException | IOException e) {
            String mess = messageSource.getMessage("error.mailProcess.NotCreatedNotice", null, systemLocale);
            eventService.logMailError(mess, e.getMessage(), notice.getId());
        }

        return "Статус документа изменился";
    }

    private List<File> getDocumentFiles(Notice notice) throws ServiceException {
        try {
            List<File> files = new ArrayList<>();
            List<Document.Field> fields = documentDao.getDocFields(notice.getDocId());
            for (Document.Field field : fields) {
                if (field.getTypeId() == FieldType.Id.FILE) {
                    files.add(fileDao.get(field.getFileId()));
                }
            }
            return files.isEmpty() ? null : files;
        } catch (DataAccessException e) {
            String mess = messageSource.getMessage("error.mailProcess.NotLoadedFilesForNotice", null, systemLocale);
            eventService.logMailError(mess, e.getMessage(), notice.getId());
            throw new ServiceException(mess, e);
        }
    }

    private MimeMessage createMimeMessage(Notice notice, String mailBody, List<File> mailAttachments) throws ServiceException {
        try {
            MimeMessage mimeMessage = new MimeMessage(mailSession);
            //mimeMessage.setReturnOption(SMTPMessage.RETURN_HDRS);
            //mimeMessage.setNotifyOptions(SMTPMessage.NOTIFY_SUCCESS | SMTPMessage.NOTIFY_FAILURE | SMTPMessage.NOTIFY_DELAY);
            mimeMessage.setFrom(new InternetAddress(appProps.sendNoticesFromEmailAddress, appProps.sendNoticesFromPersonName));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notice.getEmail()));
            String subject = "[" + notice.getDocId() + "] " + notice.getDocName();
            mimeMessage.setSubject(subject);
            mimeMessage.setHeader(EMAIL_SEND_ID, String.valueOf(notice.getId()));

            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(mailBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            if (mailAttachments != null) {
                for (File file: mailAttachments) {
                    messageBodyPart = new MimeBodyPart();
                    javax.activation.DataSource source = new ByteArrayDataSource(file.getContent(), file.getMimeType());
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName(file.getName());
                    messageBodyPart.setDisposition(Part.ATTACHMENT);
                    multipart.addBodyPart(messageBodyPart);
                }
            }

            mimeMessage.setContent(multipart);
            return mimeMessage;
        } catch (Exception e) {
            String mess = messageSource.getMessage("error.mailProcess.NotCreatedMimeMessage", null, systemLocale);
            eventService.logMailError(mess, e.getMessage(), notice.getId());
            throw new ServiceException(mess, e);
        }
    }

    private void processingDeliveryStatuses() {
        Folder inboxFolder = null;
        Folder deleteFolder = null;
        Message[] allMessages = null;
        List<Message> badMessages = new ArrayList<>();

        try {
            mailStore.connect();

            deleteFolder = mailStore.getFolder(appProps.sendNoticesMailTrashName);
            inboxFolder = mailStore.getFolder(appProps.sendNoticesMailInboxName);
            inboxFolder.open(Folder.READ_WRITE);
            allMessages = inboxFolder.getMessages();

            FetchProfile fetchProfile = new FetchProfile();
            fetchProfile.add(FetchProfile.Item.SIZE);
            inboxFolder.fetch(allMessages, fetchProfile);

            for (Message allMessage : allMessages) {
                MimeMessage message = (MimeMessage) allMessage;
                String mailAddress = ((InternetAddress) message.getFrom()[0]).getAddress();
                String mailSubject = MimeUtility.decodeText(message.getSubject());
                int messSize = message.getSize();
                if (messSize > appProps.sendNoticesIncomingMessageMaxSize) {
                    badMessages.add(message);
                    String mess = messageSource.getMessage("error.mailProcess.IncomingMessageTooBig",
                            new Object[]{messSize, mailAddress}, systemLocale);
                    eventService.logMailError(mess, null);
                    continue;
                }

                MimeMessage msg = new MimeMessage(message);
                if (!msg.isMimeType("multipart/report")) {
                    badMessages.add(message);
                    String mess = messageSource.getMessage("error.mailProcess.IncomingMessageNotReport", null, systemLocale);
                    eventService.logMailError(mess, null);
                    continue;
                }

                try {
                    Notice notice = new Notice();
                    Multipart multiPart = (Multipart) msg.getContent();
                    for (int j = 0; j < multiPart.getCount(); j++) {
                        BodyPart bodyPart = multiPart.getBodyPart(j);
                        if (bodyPart.isMimeType("text/plain")) {
                            String deliveryMessage = (String) bodyPart.getContent();
                            notice.setInfo(deliveryMessage);
                        }
                        else if (bodyPart.isMimeType("message/rfc822")) {
                            MimeMessage rfc822 = (MimeMessage) bodyPart.getContent();
                            int esId = Integer.parseInt(rfc822.getHeader(EMAIL_SEND_ID)[0]);
                            notice.setId(esId);
                        }
                        else if (bodyPart.isMimeType("text/rfc822-headers")) {
                            MessageHeaders messHeaders = (MessageHeaders) bodyPart.getContent();
                            int esId = Integer.parseInt(messHeaders.getHeader(EMAIL_SEND_ID)[0]);
                            notice.setId(esId);
                        }
                        else if (bodyPart.isMimeType("message/delivery-status")) {
                            DeliveryStatus messStatus = (DeliveryStatus) bodyPart.getContent();
                            InternetHeaders deliveryHeaders = messStatus.getRecipientDSN(0);
                            String deliveryStatus = deliveryHeaders.getHeader("Action")[0];
                            switch (deliveryStatus.toLowerCase()) {
                                case "delayed":
                                    notice.setSendStatusId(Notice.Status.DELAYED);
                                    break;
                                case "failed":
                                    notice.setSendStatusId(Notice.Status.NOT_DELIVERED);
                                    break;
                                case "delivered":
                                case "relayed":
                                case "expanded":
                                    notice.setSendStatusId(Notice.Status.DELIVERED);
                                    notice.setInfo(null);
                                    break;
                            }
                        }
                        else {
                            badMessages.add(message);
                            String mess = messageSource.getMessage("error.mailProcess.UnknownDeliveryFormat",
                                    new Object[]{mailAddress}, systemLocale);
                            eventService.logMailError(mess, null);
                            break;
                        }
                    }

                    notice.setChangeDate(new Date());
                    mailProcessDao.updateDeliveryStatus(notice);
                } catch (Exception e) {
                    badMessages.add(message);
                    String mess = messageSource.getMessage("error.mailProcess.NotDefinedDeliveryStatus",
                            new Object[]{mailAddress}, systemLocale);
                    eventService.logMailError(mess, e.getMessage());
                }
            }

            inboxFolder.copyMessages(badMessages.toArray(new Message[badMessages.size()]), deleteFolder);
            inboxFolder.setFlags(allMessages, new Flags(Flags.Flag.DELETED), true);
        } catch (Exception e) {
            String mess = messageSource.getMessage("error.mailProcess.CancelReportProcessing", null, systemLocale);
            eventService.logMailError(mess, e.getMessage());
        } finally {
            try {
                if (inboxFolder != null) {inboxFolder.close(true);}
                if (mailStore != null) {mailStore.close();}
            } catch (Exception ex) {
                //
            }
        }
    }
}