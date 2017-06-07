package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.TreeNode;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.ServiceException;
import ru.pioneersystem.pioneer2.view.utils.TreeNodeUtil;

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
public class DocumentCreateView implements Serializable {
    private static final long serialVersionUID = 1L;

    private TreeNode partTempTree;
    private TreeNode selectedNode;

    private ResourceBundle bundle;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshTemplateList() {
        try {
            partTempTree = TreeNodeUtil.toTree(partService.getUserPartList(Part.Type.FOR_TEMPLATES), true);
        } catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void onNodeExpand(NodeExpandEvent event) {
        try {
            Part part = (Part) event.getTreeNode().getData();
            List<TreeNode> treeNodes = TreeNodeUtil.toTemplateTreeNodeList(templateService.getTemplateList(part.getId()));
            event.getTreeNode().getChildren().addAll(treeNodes);
        } catch (ServiceException e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
//        createFlag = false;

        if (selectedNode == null || !selectedNode.getType().equals(TreeNodeUtil.DOCUMENT_TYPE)) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {


            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public TreeNode getPartTempTree() {
        return partTempTree;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
}