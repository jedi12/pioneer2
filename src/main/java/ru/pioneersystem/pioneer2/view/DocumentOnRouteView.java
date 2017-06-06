package ru.pioneersystem.pioneer2.view;

import ru.pioneersystem.pioneer2.model.Document;
import ru.pioneersystem.pioneer2.service.DocumentService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class DocumentOnRouteView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Document> documentList;
    private List<Document> filteredDocumentList;
    private Document selectedDocument;

    private ResourceBundle bundle;

    @ManagedProperty("#{documentService}")
    private DocumentService documentService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshOnRouteDocumentList() {
        try {
            documentList = documentService.getOnRouteDocumentList();
        } catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
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
}