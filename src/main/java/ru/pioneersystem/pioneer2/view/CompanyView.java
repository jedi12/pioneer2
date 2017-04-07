package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.CompanyService;
import ru.pioneersystem.pioneer2.view.utils.LocaleBean;

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
public class CompanyView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Company> companyList;
    private List<Company> filteredCompanyList;
    private Company selectedCompany;

    private boolean createFlag;
    private Company currCompany;

    @ManagedProperty("#{companyService}")
    private CompanyService companyService;

    @ManagedProperty("#{localeBean}")
    private LocaleBean localeBean;

    @PostConstruct
    public void init()  {
        refreshList();
    }

    private void refreshList() {
        try {
            companyList = companyService.getCompanyList(localeBean.getLocale());
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currCompany = new Company();

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedCompany == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currCompany = companyService.getCompany(selectedCompany.getId(), localeBean.getLocale());
            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                companyService.createCompany(currCompany);
            } else {
                companyService.updateCompany(currCompany);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void lockAction() {
        if (selectedCompany == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            companyService.lockCompany(selectedCompany.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.locked");
        }
    }

    public void unlockAction() {
        if (selectedCompany == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            companyService.unlockCompany(selectedCompany.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.unlocked");
        }
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        ResourceBundle bundle = ResourceBundle.getBundle("text", localeBean.getLocale());
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setLocaleBean(LocaleBean localeBean) {
        this.localeBean = localeBean;
    }

    public List<Company> getCompanyList() {
        return companyList;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public List<Company> getFilteredCompanyList() {
        return filteredCompanyList;
    }

    public void setFilteredCompanyList(List<Company> filteredCompanyList) {
        this.filteredCompanyList = filteredCompanyList;
    }

    public Company getSelectedCompany() {
        return selectedCompany;
    }

    public void setSelectedCompany(Company selectedCompany) {
        this.selectedCompany = selectedCompany;
    }

    public Company getCurrCompany() {
        return currCompany;
    }

    public void setCurrCompany(Company currCompany) {
        this.currCompany = currCompany;
    }
}