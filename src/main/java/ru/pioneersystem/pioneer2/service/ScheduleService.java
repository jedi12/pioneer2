package ru.pioneersystem.pioneer2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.pioneersystem.pioneer2.AppProps;

import java.util.Locale;

@Service("scheduleService")
public class ScheduleService {
    private EventService eventService;
    private Locale systemLocale;
    private MessageSource messageSource;
    private AppProps appProps;
    private SendNoticesMailService sendNoticesMailService;

    @Autowired
    public ScheduleService(EventService eventService, Locale systemLocale, MessageSource messageSource,
                           AppProps appProps, SendNoticesMailService sendNoticesMailService) {
        this.eventService = eventService;
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

//    @Scheduled(initialDelay = 3000, fixedDelayString = "${processMailCommand.period: 60000}")
//    public void processingMailCommands() {
//        if (appProps.processMailCommandEnabled) {
//            //
//        }
//    }

//    @Scheduled(initialDelay = 3000, fixedDelayString = "${createDocumentsFromMail.period: 60000}")
//    public void processingMailCreateDocs() {
//        if (appProps.createDocumentsFromMailEnabled) {
//            //
//        }
//    }
}
