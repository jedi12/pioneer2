package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.ChoiceList;
import ru.pioneersystem.pioneer2.service.ChoiceListService;
import ru.pioneersystem.pioneer2.service.TemplateService;
import ru.pioneersystem.pioneer2.service.exception.RestrictionException;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import java.util.List;
import java.util.ResourceBundle;

@ManagedBean
@ViewScoped
public class ChoiceListView {
    private List<ChoiceList> choiceListList;
    private List<ChoiceList> filteredChoiceList;
    private ChoiceList selectedChoiceList;

    private String addElement;
    private ChoiceList currChoiceList;

    private List<String> templatesContainingChoiceList;

    private ResourceBundle bundle;

    @ManagedProperty("#{choiceListService}")
    private ChoiceListService choiceListService;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshList() {
        try {
            choiceListList = choiceListService.getChoiceListList();

            RequestContext.getCurrentInstance().execute("PF('choiceListsTable').clearFilters()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        addElement = null;
        currChoiceList = choiceListService.getNewChoiceList();

        RequestContext.getCurrentInstance().execute("PF('choiceListEditDialog').show()");
    }

    public void editDialog() {
        addElement = null;

        try {
            currChoiceList = choiceListService.getChoiceList(selectedChoiceList);
            RequestContext.getCurrentInstance().execute("PF('choiceListEditDialog').show()");
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
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
            choiceListService.saveChoiceList(currChoiceList);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('choiceListEditDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteDialog() {
        if (selectedChoiceList == null) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), bundle.getString("error.choiceList.NotSelected")));
            return;
        }

        try {
            templatesContainingChoiceList = templateService.getListContainingChoiceList(selectedChoiceList.getId());
            RequestContext.getCurrentInstance().execute("PF('choiceListDeleteDialog').show()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void deleteAction() {
        try {
            choiceListService.deleteChoiceList(selectedChoiceList);
            refreshList();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }

        RequestContext.getCurrentInstance().execute("PF('choiceListDeleteDialog').hide();");
    }

    public void setChoiceListService(ChoiceListService choiceListService) {
        this.choiceListService = choiceListService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public List<ChoiceList> getChoiceListList() {
        return choiceListList;
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

    public List<String> getTemplatesContainingChoiceList() {
        return templatesContainingChoiceList;
    }
}