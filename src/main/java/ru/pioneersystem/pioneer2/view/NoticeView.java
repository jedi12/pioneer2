package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Notice;
import ru.pioneersystem.pioneer2.service.NoticeService;
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
public class NoticeView {
    private List<Notice> noticeList;
    private List<Notice> filteredNoticeList;
    private Notice selectedNotice;

    private String info;

    private Date fromDate;
    private Date toDate;

    private ResourceBundle bundle;

    @ManagedProperty("#{noticeService}")
    private NoticeService noticeService;

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
            noticeList = noticeService.getNoticeList(fromDate, toDate);

            RequestContext.getCurrentInstance().execute("PF('noticesTable').clearFilters()");
        }
        catch (TooManyObjectsException e) {
            noticeList = e.getObjects();

            RequestContext.getCurrentInstance().execute("PF('noticesTable').clearFilters()");

            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void editDialog() {
        if (selectedNotice == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.notice.NotSelected")));
            return;
        }

        try {
            info = noticeService.getNoticeInfo(selectedNotice.getId());

            RequestContext.getCurrentInstance().execute("PF('noticeEditDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public List<Notice> getNoticeList() {
        return noticeList;
    }

    public List<Notice> getFilteredNoticeList() {
        return filteredNoticeList;
    }

    public void setFilteredNoticeList(List<Notice> filteredNoticeList) {
        this.filteredNoticeList = filteredNoticeList;
    }

    public Notice getSelectedNotice() {
        return selectedNotice;
    }

    public void setSelectedNotice(Notice selectedNotice) {
        this.selectedNotice = selectedNotice;
    }

    public String getInfo() {
        return info;
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