package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.TreeNode;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;
import ru.pioneersystem.pioneer2.view.utils.TreeNodeUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class PartView {
    private List<Part> partList;
    private TreeNode partTree;
    private TreeNode selectedNode;
    private List<Part> filteredPart;
    private Part selectedPart;

    private Part currPart;
    private int partType;

    private Map<String, Integer> selectPublishGroup;
    private Map<String, Integer> selectParentPart;

    private List<String> selectGroup;
    private Map<String, Integer> selectGroupDefault;
    private String selectedGroup;

    private List<String> notSelectableTemplateList;
    private int docCountInPubPart;

    private ResourceBundle bundle;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        partType = Part.Type.FOR_TEMPLATES;
        currPart = partService.getNewPart();
    }

    public void refreshList() {
        try {
            partList = partService.getPartList(partType);
            partTree = TreeNodeUtil.toTree(partList, false);

            selectParentPart = toPartMapAndSort(partList);
            selectPublishGroup = groupService.getUserPublishMap();
            selectGroupDefault = groupService.getGroupMap();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        // TODO: 06.05.2017 Необходима проверка на право создавать подраздел в текущем разделе для владельца или администратора (тут или в сервисе)

        currPart = partService.getNewPart();
        if (selectedNode != null) {
            currPart.setParent(((Part) selectedNode.getData()).getId());
        }
        selectGroup = getCurrSelectPart(currPart);

        RequestContext.getCurrentInstance().execute("PF('partEditDialog').show()");
    }

    public void editDialog() {
        // TODO: 06.05.2017 Необходима проверка на право изменить раздел для владельца или администратора (тут или в сервисе)

        try {
            Part selectedPart;
            if (selectedNode != null) {
                selectedPart = (Part) selectedNode.getData();
            } else {
                selectedPart = this.selectedPart;
            }

            currPart = partService.getPart(selectedPart);
            selectGroup = getCurrSelectPart(currPart);

            RequestContext.getCurrentInstance().execute("PF('partEditDialog').show()");
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void saveAction() {
        try {
            partService.savePart(TreeNodeUtil.setTreeLevel(currPart, partList), partType);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('partEditDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedNode == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.part.NotSelected")));
            return;
        }

        try {
            List<Part> partsForDelete = TreeNodeUtil.toList(selectedNode, new ArrayList<>(), 0);
            if (partType == Part.Type.FOR_TEMPLATES) {
                notSelectableTemplateList = partService.getTemplateListContainingInParts(partsForDelete);
            } else if (partType == Part.Type.FOR_DOCUMENTS) {
                docCountInPubPart = partService.getCountPubDocContainingInParts(partsForDelete);
            }
            RequestContext.getCurrentInstance().execute("PF('partDeleteDialog').show()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteAction() {
        try {
            List<Part> parts = TreeNodeUtil.toList(selectedNode, new ArrayList<>(), 0);
            partService.deleteParts(parts, partType);
            refreshList();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }

        RequestContext.getCurrentInstance().execute("PF('partDeleteDialog').hide();");
    }

    public void onDragDrop(TreeDragDropEvent event) {
        try {
            TreeNode dragNode = event.getDragNode();
            TreeNode dropNode = event.getDropNode();

            int pastTreeLevel = ((Part) dragNode.getData()).getTreeLevel();
            int currTreeLevel = ((Part) dragNode.getParent().getData()).getTreeLevel() + 1;
            int treeLevelOffset = currTreeLevel - pastTreeLevel;

            int partParentId = ((Part) dropNode.getData()).getId();
            ((Part) dragNode.getData()).setParent(partParentId);

            List<Part> parts = TreeNodeUtil.toList(dragNode, new ArrayList<>(), treeLevelOffset);
            partService.updateParts(parts, partType);

            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO,
                    bundle.getString("info"), bundle.getString("info.tree.changed")));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
            refreshList();
        }
    }

    public void addGroup() {
        if (selectGroup.isEmpty()) {
            return;
        }

        Part.LinkGroup linkGroup = new Part.LinkGroup();
        linkGroup.setGroupId(selectGroupDefault.get(selectedGroup));
        linkGroup.setGroupName(selectedGroup);
        currPart.getLinkGroups().add(linkGroup);

        selectGroup.remove(selectedGroup);
    }

    public void removeGroup(Part.LinkGroup collectedGroup) {
        selectGroup.add(collectedGroup.getGroupName());
    }

    private List<String> getCurrSelectPart(Part currPart) {
        List<String> currSelectGroup = new ArrayList<>();
        currSelectGroup.addAll(selectGroupDefault.keySet());
        for (Part.LinkGroup linkGroup: currPart.getLinkGroups()) {
            currSelectGroup.remove(linkGroup.getGroupName());
        }
        return currSelectGroup;
    }

    private Map<String, Integer> toPartMapAndSort(List<Part> parts) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(bundle.getString("part.root.name"), 0);
        for (Part part : parts) {
            map.put(part.getName(), part.getId());
        }
        return map;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public List<Part> getPartList() {
        return partList;
    }

    public TreeNode getPartTree() {
        return partTree;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public List<Part> getFilteredPart() {
        return filteredPart;
    }

    public void setFilteredPart(List<Part> filteredPart) {
        this.filteredPart = filteredPart;
    }

    public Part getSelectedPart() {
        return selectedPart;
    }

    public void setSelectedPart(Part selectedPart) {
        this.selectedPart = selectedPart;
    }

    public Part getCurrPart() {
        return currPart;
    }

    public void setCurrPart(Part currPart) {
        this.currPart = currPart;
    }

    public int getPartType() {
        return partType;
    }

    public void setPartType(int partType) {
        this.partType = partType;
    }

    public Map<String, Integer> getSelectPublishGroup() {
        return selectPublishGroup;
    }

    public Map<String, Integer> getSelectParentPart() {
        return selectParentPart;
    }

    public List<String> getSelectGroup() {
        return selectGroup;
    }

    public void setSelectGroup(List<String> selectGroup) {
        this.selectGroup = selectGroup;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<String> getNotSelectableTemplateList() {
        return notSelectableTemplateList;
    }

    public int getDocCountInPubPart() {
        return docCountInPubPart;
    }
}