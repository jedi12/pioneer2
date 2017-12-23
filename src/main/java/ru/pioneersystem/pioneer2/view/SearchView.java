package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.model.SearchFilter;
import ru.pioneersystem.pioneer2.service.SearchService;
import ru.pioneersystem.pioneer2.service.exception.TooManyObjectsException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.*;

@ManagedBean
@ViewScoped
public class SearchView {
    private List<Document> documentList;
    private List<Document> filteredDocumentList;
    private Document selectedDocument;

    private SearchFilter searchFilter;

    private ResourceBundle bundle;

    @ManagedProperty("#{documentView}")
    private DocumentView documentView;

    @ManagedProperty("#{searchService}")
    private SearchService searchService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        searchFilter = searchService.getNewFilter();
    }

    public void findAction() {
        try {
            documentList = searchService.findList(searchFilter);

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

    public SearchFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(SearchFilter searchFilter) {
        this.searchFilter = searchFilter;
    }
}