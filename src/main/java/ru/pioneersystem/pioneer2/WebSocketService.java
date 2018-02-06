package ru.pioneersystem.pioneer2;

import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PostConstruct;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@SessionScope
public class WebSocketService {

    @Inject
    @Push
    private PushContext myChannel;

    @PostConstruct
    public void init() {

    }

    public void sendMessage() {
        myChannel.send("Message has been broadcasted");
    }
}
