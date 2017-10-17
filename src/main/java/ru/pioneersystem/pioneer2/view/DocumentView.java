package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.service.exception.LockException;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;
import ru.pioneersystem.pioneer2.view.utils.TreeNodeUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.*;

@ManagedBean
@ViewScoped
public class DocumentView implements Serializable {
    private static final long serialVersionUID = 1L;

    private TreeNode partTempTree;
    private TreeNode partDocTree;
    private TreeNode selectedNode;

    private List<Document> documentList;
    private List<Document> filteredDocumentList;
    private Document selectedDocument;

    private Document currDoc;
    private List<RoutePoint> routePoints;
    private List<User> usersInGroup;

    private Date dateIn;
    private String radioSelect;

    private ResourceBundle bundle;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @ManagedProperty("#{documentService}")
    private DocumentService documentService;

    @ManagedProperty("#{routeProcessService}")
    private RouteProcessService routeProcessService;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @ManagedProperty("#{fileService}")
    private FileService fileService;

    @ManagedProperty("#{currentUser}")
    private CurrentUser currentUser;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    private void initDefault() {
        dateIn = Date.from(LocalDate.now(localeBean.getZoneId()).atStartOfDay(localeBean.getZoneId()).toInstant());
        radioSelect = "date";
        currDoc = new Document();
    }

    public void refreshTemplateList() {
        try {
            partTempTree = TreeNodeUtil.toTree(partService.getUserPartList(Part.Type.FOR_TEMPLATES), true);
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void refreshPubDocList() {
        try {
            partDocTree = TreeNodeUtil.toTree(partService.getUserPartList(Part.Type.FOR_DOCUMENTS), true);
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void refreshMyDocList() {
        try {
            initDefault();
            documentList = documentService.getMyDocumentListOnDate(dateIn);

            selectedDocument = null;
            RequestContext.getCurrentInstance().execute("PF('docTable').clearFilters()");
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void refreshMyDocListFilter() {
        try {
            if (radioSelect.equals("date")) {
                documentList = documentService.getMyDocumentListOnDate(dateIn);
            } else {
                documentList = documentService.getMyWorkingDocumentList();
            }

            selectedDocument = null;
            RequestContext.getCurrentInstance().execute("PF('docTable').clearFilters()");
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void refreshOnRouteDocList() {
        try {
            documentList = documentService.getOnRouteDocumentList();

            selectedDocument = null;
            RequestContext.getCurrentInstance().execute("PF('docTable').clearFilters()");
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void handleDateSelect(SelectEvent event) {
        dateIn = (Date) event.getObject();
        refreshMyDocListFilter();
    }

    public void onTempNodeExpand(NodeExpandEvent event) {
        try {
            Part part = (Part) event.getTreeNode().getData();
            List<TreeNode> treeNodes = TreeNodeUtil.toTemplateTreeNodeList(templateService.getTemplateList(part.getId()));
            event.getTreeNode().getChildren().addAll(treeNodes);
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void onPubDocNodeExpand(NodeExpandEvent event) {
        try {
            Part part = (Part) event.getTreeNode().getData();
            List<TreeNode> treeNodes = TreeNodeUtil.toDocumentTreeNodeList(documentService.getDocumentListByPatrId(part.getId()));
            event.getTreeNode().getChildren().addAll(treeNodes);
        } catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openNewDocDialog() {
        if (selectedNode == null || !selectedNode.getType().equals(TreeNodeUtil.DOCUMENT_TYPE)) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotSelected")));
            return;
        }

        try {
            int templateId = ((Template) selectedNode.getData()).getId();
            currDoc = documentService.getNewDocument(templateId);

            RequestContext.getCurrentInstance().execute("PF('docDialog').show()");
        }
        catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openDocFromNodeDialog() {
        if (selectedNode == null || !selectedNode.getType().equals(TreeNodeUtil.DOCUMENT_TYPE)) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotSelected")));
            return;
        }

        try {
            int documentId = ((Document) selectedNode.getData()).getId();
            currDoc = documentService.getDocument(documentId);

            RequestContext.getCurrentInstance().execute("PF('docDialog').show()");
        }
        catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openDocFromListDialog() {
        if (selectedDocument == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotSelected")));
            return;
        }

        try {
            currDoc = documentService.getDocument(selectedDocument.getId());

            RequestContext.getCurrentInstance().execute("PF('docDialog').show()");
        }
        catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void autoOpenDocument(int currDocId, boolean showRoute) {
        if (currDocId <= 0 || currentUser.getCurrMenuId() <= 0 || documentList == null) {
            return;
        }

        for (Document document: documentList) {
            if (document.getId() == currDocId) {
                selectedDocument = document;
                if (showRoute) {
                    openDocRouteFromListDialog();
                } else {
                    openDocFromListDialog();
                }
                return;
            }
        }
    }

    public void openDocRouteFromListDialog() {
        if (selectedDocument == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotSelected")));
            return;
        }

        try {
            routePoints = routeProcessService.getDocumentRoute(selectedDocument.getId());

            RequestContext.getCurrentInstance().execute("PF('docRouteDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openDocRouteFromNodeDialog() {
        if (selectedNode == null || !selectedNode.getType().equals(TreeNodeUtil.DOCUMENT_TYPE)) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotSelected")));
            return;
        }

        try {
            int documentId = ((Document) selectedNode.getData()).getId();
            routePoints = routeProcessService.getDocumentRoute(documentId);

            RequestContext.getCurrentInstance().execute("PF('docRouteDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void openDocUsersInGroupDialog(int groupId) {
        try {
            usersInGroup = userService.getUsersInGroup(groupId);

            RequestContext.getCurrentInstance().execute("PF('docUsersInGroupDialog').show()");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAction() {
        try {
            documentService.saveDocument(currDoc);

            if (currDoc.isCreateFlag()) {
                currentUser.selectMenu(Menu.Id.MY_DOCS);
                RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
                RequestContext.getCurrentInstance().update(
                        new ArrayList<>(Arrays.asList(new String[] {"leftPanel", "centerPanel"})));
                return;
            }
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (LockException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAndSendAction() {
        try {
            documentService.saveAndSendDocument(currDoc);

            if (currDoc.isCreateFlag()) {
                currentUser.selectMenu(Menu.Id.MY_DOCS);
                RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
                RequestContext.getCurrentInstance().update(
                        new ArrayList<>(Arrays.asList(new String[] {"leftPanel", "centerPanel"})));
                return;
            }
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (LockException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteAction() {
        try {
            documentService.deleteDocument(currDoc);
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void copyAction() {
        try {
            documentService.copyDocument(currDoc);
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void recallAction() {
        try {
            documentService.recallDocument(currDoc);
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (LockException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        }
        catch (ServiceException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void publishAction() {
        if (!currDoc.isNewPart()) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.document.NotPartSelected")));
            return;
        }

        try {
            documentService.publishDocument(currDoc);
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void cancelPublishAction() {
        try {
            documentService.cancelPublishDocument(currDoc);
            refreshMyDocListFilter();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void acceptAction() {
        try {
            documentService.acceptDocument(currDoc);
            refreshOnRouteDocList();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void rejectAction() {
        try {
            documentService.rejectDocument(currDoc);
            refreshOnRouteDocList();

            RequestContext.getCurrentInstance().execute("PF('docDialog').hide();");
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            UploadedFile uploadedFile = event.getFile();
            int fieldRowNum = (int) event.getComponent().getAttributes().get("fieldRowNum");
            for (Document.Field field: currDoc.getFields()) {
                if (field.getNum() == fieldRowNum) {
                    field.setFileName(uploadedFile.getFileName());

                    File file = new File();
                    file.setName(uploadedFile.getFileName());
                    file.setMimeType(uploadedFile.getContentType());
                    file.setLength(uploadedFile.getSize());
                    file.setContent(uploadedFile.getContents());

                    field.setFile(file);
                    break;
                }
            }
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), bundle.getString("error.file.not.uploaded")));
        }
    }

    public void fileDelete(int fieldRowNum) {
        for (Document.Field field: currDoc.getFields()) {
            if (field.getNum() == fieldRowNum) {
                field.setFileName(null);
                field.setFile(null);
            }
        }
    }

    public void showFile(int fieldRowNum) {
        OutputStream responseOutputStream = null;

        try {
            for (Document.Field field: currDoc.getFields()) {
                if (field.getNum() == fieldRowNum) {
                    File file = field.getFile();
                    if (file == null) {
                        file = fileService.getFile(field.getFileId());
                    }

                    FacesContext context = FacesContext.getCurrentInstance();
                    HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                    String fileNameURLEncoded = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");
                    response.setContentType(file.getMimeType());
                    response.setContentLengthLong(file.getLength());
                    response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + fileNameURLEncoded);
                    responseOutputStream = response.getOutputStream();
                    responseOutputStream.write(file.getContent());
                    responseOutputStream.flush();
                    responseOutputStream.close();
                    context.responseComplete();

                    break;
                }
            }
        }
        catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
        finally {
            try {
                if (responseOutputStream != null) {responseOutputStream.close();}
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setRouteProcessService(RouteProcessService routeProcessService) {
        this.routeProcessService = routeProcessService;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public TreeNode getPartTempTree() {
        return partTempTree;
    }

    public TreeNode getPartDocTree() {
        return partDocTree;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
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

    public Document getCurrDoc() {
        return currDoc;
    }

    public void setCurrDoc(Document currDoc) {
        this.currDoc = currDoc;
    }

    public List<RoutePoint> getRoutePoints() {
        return routePoints;
    }

    public List<User> getUsersInGroup() {
        return usersInGroup;
    }

    public Date getDateIn() {
        return dateIn;
    }

    public void setDateIn(Date dateIn) {
        this.dateIn = dateIn;
    }

    public String getRadioSelect() {
        return radioSelect;
    }

    public void setRadioSelect(String radioSelect) {
        this.radioSelect = radioSelect;
    }
}