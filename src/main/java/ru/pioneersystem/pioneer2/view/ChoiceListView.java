package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.ChoiceListService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class ChoiceListView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ChoiceList> choiceListList;
    private List<ChoiceList> filteredChoiceList;
    private ChoiceList selectedChoiceList;

    private boolean createFlag;
    private String addElement;
    private ChoiceList currChoiceList;

    @ManagedProperty("#{choiceListService}")
    private ChoiceListService choiceListService;

    @ManagedProperty("#{currentUser}")
    private CurrentUser currentUser;

    @PostConstruct
    public void init()  {
        refreshList();
    }

    private void refreshList() {
        try {
            choiceListList = choiceListService.getChoiceListList(currentUser.getUser().getCompanyId());
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        addElement = null;
        currChoiceList = new ChoiceList();
        currChoiceList.setValues(new ArrayList<>());

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;
        addElement = null;

        if (selectedChoiceList == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currChoiceList = choiceListService.getChoiceList(selectedChoiceList.getId());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void addValue() {
        if (addElement == null || addElement.trim().equals("")) {
            return;
        }

        currChoiceList.getValues().add(addElement.trim());
        addElement = null;
    }

    public void saveAction() {
        try {
            if (createFlag) {
                choiceListService.createChoiceList(currChoiceList, currentUser.getUser().getCompanyId());
            } else {
                choiceListService.updateChoiceList(currChoiceList);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedChoiceList == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            choiceListService.deleteChoiceList(selectedChoiceList.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setChoiceListService(ChoiceListService choiceListService) {
        this.choiceListService = choiceListService;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public List<ChoiceList> getChoiceListList() {
        return choiceListList;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public List<ChoiceList> getFilteredChoiceList() {
        return filteredChoiceList;
    }

    public void setFilteredChoiceList(List<ChoiceList> filteredChoiceList) {
        this.filteredChoiceList = filteredChoiceList;
    }

    public ChoiceList getSelectedChoiceList() {
        return selectedChoiceList;
    }

    public void setSelectedChoiceList(ChoiceList selectedChoiceList) {
        this.selectedChoiceList = selectedChoiceList;
    }

    public String getAddElement() {
        return addElement;
    }

    public void setAddElement(String addElement) {
        this.addElement = addElement;
    }

    public ChoiceList getCurrChoiceList() {
        return currChoiceList;
    }

    public void setCurrChoiceList(ChoiceList currChoiceList) {
        this.currChoiceList = currChoiceList;
    }
}