package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.AppProps;
import ru.pioneersystem.pioneer2.dao.MailProcessDao;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import java.util.Locale;

@Service("mailProcessService")
public class MailProcessService {
    private MailProcessDao mailProcessDao;
    private CurrentUser currentUser;
    private EventService eventService;
    private LocaleBean localeBean;
    private Locale systemLocale;
    private MessageSource messageSource;
    private AppProps appProps;
    private SendNoticesMailService sendNoticesMailService;

    @Autowired
    public MailProcessService(MailProcessDao mailProcessDao, CurrentUser currentUser, EventService eventService,
                              LocaleBean localeBean, Locale systemLocale, MessageSource messageSource,
                              AppProps appProps, SendNoticesMailService sendNoticesMailService) {
        this.mailProcessDao = mailProcessDao;
        this.currentUser = currentUser;
        this.eventService = eventService;
        this.localeBean = localeBean;
        this.systemLocale = systemLocale;
        this.messageSource = messageSource;
        this.appProps = appProps;
        this.sendNoticesMailService = sendNoticesMailService;
    }

    @Scheduled(initialDelay = 3000, fixedDelayString = "${sendNotices.period: 60000}")
    public void processingSendNotices() {
        if (appProps.sendNoticesEnabled) {
            if (sendNoticesMailService.isInitialized()) {
                sendNoticesMailService.startProcessing();
            } else {
                appProps.sendNoticesEnabled = false;
                String mess = messageSource.getMessage("error.mailProcess.Stopped", null, systemLocale);
                eventService.logMailError(mess, null);
            }
        }
    }

    public void processingMailCommands() {

    }

    public void processingMailCreateDocs() {

    }
}
