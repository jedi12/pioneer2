package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

@ManagedBean
@ViewScoped
public class SearchView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Document> documentList;
    private List<Document> filteredDocumentList;
    private Document selectedDocument;

    private SearchDoc searchDoc;

    private Map<String, Integer> forSearchTemplates;
    private Map<String, Integer> forSearchStatuses;
    private Map<String, Integer> forSearchCreateGroups;

    private ResourceBundle bundle;

    @ManagedProperty("#{documentView}")
    private DocumentView documentView;

    @ManagedProperty("#{searchService}")
    private SearchService searchService;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @ManagedProperty("#{statusService}")
    private StatusService statusService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());

        searchDoc = new SearchDoc();
        ZonedDateTime currDate = LocalDate.now(localeBean.getZoneId()).atStartOfDay(localeBean.getZoneId());
        searchDoc.setFromDate(Date.from(currDate.toInstant()));
        searchDoc.setToDate(Date.from(currDate.toInstant()));

        try {
            forSearchTemplates = templateService.getForSearchTemplateMap();
            forSearchStatuses = statusService.getStatusMap();
            forSearchCreateGroups = groupService.getForSearchGroupMap();
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void findAction() {
        try {
            documentList = searchService.findList(searchDoc);

            selectedDocument = null;
            RequestContext.getCurrentInstance().execute("PF('docTable').clearFilters()");

            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    bundle.getString("info"), bundle.getString("search.message.found") + documentList.size()));
        }
        catch (TooManyObjectsException e) {
            documentList = e.getObjects();

            selectedDocument = null;
            RequestContext.getCurrentInstance().execute("PF('docTable').clearFilters()");

            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openDocDialog() {
        documentView.setSelectedDocument(selectedDocument);
        documentView.openDocFromListDialog();
    }

    public void openDocRouteDialog() {
        documentView.setSelectedDocument(selectedDocument);
        documentView.openDocRouteFromListDialog();
    }

    public void setDocumentView(DocumentView documentView) {
        this.documentView = documentView;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public List<Document> getDocumentList() {
        return documentList;
    }

    public List<Document> getFilteredDocumentList() {
        return filteredDocumentList;
    }

    public void setFilteredDocumentList(List<Document> filteredDocumentList) {
        this.filteredDocumentList = filteredDocumentList;
    }

    public Document getSelectedDocument() {
        return selectedDocument;
    }

    public void setSelectedDocument(Document selectedDocument) {
        this.selectedDocument = selectedDocument;
    }

    public SearchDoc getSearchDoc() {
        return searchDoc;
    }

    public void setSearchDoc(SearchDoc searchDoc) {
        this.searchDoc = searchDoc;
    }

    public Map<String, Integer> getForSearchTemplates() {
        return forSearchTemplates;
    }

    public Map<String, Integer> getForSearchStatuses() {
        return forSearchStatuses;
    }

    public Map<String, Integer> getForSearchCreateGroups() {
        return forSearchCreateGroups;
    }
}