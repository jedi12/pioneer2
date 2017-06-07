package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.TreeNode;
import ru.pioneersystem.pioneer2.model.Group;
import ru.pioneersystem.pioneer2.model.Part;
import ru.pioneersystem.pioneer2.service.GroupService;
import ru.pioneersystem.pioneer2.service.PartService;
import ru.pioneersystem.pioneer2.view.utils.TreeNodeUtil;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class PartView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Part> partList;
    private TreeNode partTree;
    private TreeNode selectedNode;

    private boolean createFlag;
    private Part currPart;
    private int partType;

    private Map<String, Integer> selectPublishGroup;
    private Map<String, Integer> selectParentPart;

    private List<String> selectGroup;
    private Map<String, Integer> selectGroupDefault;
    private String selectedGroup;

    private ResourceBundle bundle;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        partType = Part.Type.FOR_TEMPLATES;
        refreshList();
    }

    public void refreshList() {
        try {
            currPart = new Part();
            partList = partService.getPartList(partType);
            partTree = TreeNodeUtil.toTree(partList, false);

            selectParentPart = toPartMapAndSort(partList);
            selectPublishGroup = groupService.getUserPublishMap();
            selectGroupDefault = groupService.getGroupMap();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;

        // TODO: 06.05.2017 Необходима проверка на право создавать подраздел в текущем разделе для владельца или администратора (тут или в сервисе)

        currPart = new Part();
        if (selectedNode != null) {
            currPart.setParent(((Part) selectedNode.getData()).getId());
        }
        currPart.setLinkGroups(new LinkedList<>());
        selectGroup = getCurrSelectPart(currPart);

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedNode == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        // TODO: 06.05.2017 Необходима проверка на право изменить раздел для владельца или администратора (тут или в сервисе)

        try {
            int partId = ((Part) selectedNode.getData()).getId();
            currPart = partService.getPart(partId);
            selectGroup = getCurrSelectPart(currPart);

            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                partService.createPart(TreeNodeUtil.setTreeLevel(currPart, partList), partType);
            } else {
                partService.updatePart(currPart);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedNode == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        // TODO: 06.05.2017 Необходима проверка на право удалить раздел для владельца или администратора (тут или в сервисе)

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            List<Part> parts = TreeNodeUtil.toList(selectedNode, new ArrayList<>(), 0);
            partService.deleteParts(parts);
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
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
            partService.updateParts(parts);

            showGrowl(FacesMessage.SEVERITY_INFO, "info", "info.tree.changed");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.tree.not.changed");
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
        List<String> currSelectGroup = new LinkedList<>();
        currSelectGroup.addAll(selectGroupDefault.keySet());
        for (Part.LinkGroup linkGroup: currPart.getLinkGroups()) {
            currSelectGroup.remove(linkGroup.getGroupName());
        }
        return currSelectGroup;
    }

    private Map<String, Integer> toPartMapAndSort(List<Part> parts) {
        parts.sort(new Comparator<Part>() {
            @Override
            public int compare(Part part1, Part part2) {
                return part1.getName().compareTo(part2.getName());
            }
        });

        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(bundle.getString("part.root.name"), 0);
        for (Part part : parts) {
            map.put(part.getName(), part.getId());
        }
        return map;
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
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

    public boolean isCreateFlag() {
        return createFlag;
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
}