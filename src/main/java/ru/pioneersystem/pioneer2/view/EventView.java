package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Event;
import ru.pioneersystem.pioneer2.service.EventService;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class EventView {
    private List<Event> eventList;
    private List<Event> filteredEventList;
    private Event selectedEvent;

    private String eventDetail;

    private Date fromDate;
    private Date toDate;

    private ResourceBundle bundle;

    @ManagedProperty("#{eventService}")
    private EventService eventService;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshList() {
        ZonedDateTime currDate = LocalDate.now(localeBean.getZoneId()).atStartOfDay(localeBean.getZoneId());
        fromDate = Date.from(currDate.toInstant());
        toDate = Date.from(currDate.toInstant());

        findAction();
    }

    public void findAction() {
        try {
            eventList = eventService.getEventList(fromDate, toDate);

            RequestContext.getCurrentInstance().execute("PF('eventsTable').clearFilters()");
        }
        catch (TooManyObjectsException e) {
            eventList = e.getObjects();

            RequestContext.getCurrentInstance().execute("PF('eventsTable').clearFilters()");

            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void editDialog() {
        if (selectedEvent == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.event.NotSelected")));
            return;
        }

        try {
            eventDetail = eventService.getEventDetail(selectedEvent.getDate(), selectedEvent.getUserId());

            RequestContext.getCurrentInstance().execute("PF('eventEditDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public List<Event> getFilteredEventList() {
        return filteredEventList;
    }

    public void setFilteredEventList(List<Event> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    public Event getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public String getEventDetail() {
        return eventDetail;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}