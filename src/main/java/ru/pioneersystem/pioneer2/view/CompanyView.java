package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.Company;
import ru.pioneersystem.pioneer2.service.CompanyService;
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
public class CompanyView {
    private List<Company> companyList;
    private List<Company> filteredCompanyList;
    private Company selectedCompany;

    private Company currCompany;

    private ResourceBundle bundle;

    @ManagedProperty("#{companyService}")
    private CompanyService companyService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }

    public void refreshList() {
        try {
            companyList = companyService.getCompanyList();

            RequestContext.getCurrentInstance().execute("PF('companiesTable').clearFilters()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void newDialog() {
        currCompany = companyService.getNewCompany();

        RequestContext.getCurrentInstance().execute("PF('companyEditDialog').show()");
    }

    public void editDialog() {
        try {
            currCompany = companyService.getCompany(selectedCompany);
            RequestContext.getCurrentInstance().execute("PF('companyEditDialog').show()");
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
            companyService.saveCompany(currCompany);

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('companyEditDialog').hide();");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void lockAction() {
        try {
            companyService.lockCompany(selectedCompany);
            refreshList();
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void unlockAction() {
        try {
            companyService.unlockCompany(selectedCompany);
            refreshList();
        } catch (RestrictionException e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_WARN,
                    bundle.getString("warn"), e.getMessage()));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    bundle.getString("fatal"), e.getMessage()));
        }
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public List<Company> getCompanyList() {
        return companyList;
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