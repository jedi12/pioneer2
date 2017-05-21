package ru.pioneersystem.pioneer2.view;

import org.primefaces.context.RequestContext;
import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ManagedBean
@ViewScoped
public class TemplateView implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Template> templateList;
    private List<Template> filteredTemplate;
    private Template selectedTemplate;

    private boolean createFlag;
    private Template currTemplate;

    private Map<String, Route> selectRouteDefault;
    private List<String> selectRouteList;
    private Map<String, Integer> selectRoute;
    private Map<String, Integer> selectPart;
    private Map<Integer, FieldType> selectFieldTypeDefault;
    private List<SelectItem> selectFieldType;
    private int selectedFieldType;
    private Map<String, ChoiceList> selectChoiceListDefault;
    private List<String> selectChoiceList;
    private String selectedChoiceList;
    private String fieldName;
    private boolean addChoiceListRendered;
    private boolean addFieldNameRendered;
    private int condNum;
    private Map<String, Template.Field> selectFieldNameDefault;
    private List<String> selectFieldName;
    private String selectedFieldName;
    private String condValue;
    private boolean condCheckBoxValue;
    private Date condCalendarValue;
    private List<String> selectCond;
    private String selectedCond;
    private String selectedCondRoute;
    private boolean condValueRendered;
    private boolean condCheckBoxValueRendered;
    private boolean condCalendarValueRendered;

    private ResourceBundle bundle;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @ManagedProperty("#{routeService}")
    private RouteService routeService;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{fieldTypeService}")
    private FieldTypeService fieldTypeService;

    @ManagedProperty("#{choiceListService}")
    private ChoiceListService choiceListService;

    @PostConstruct
    public void init() {
        bundle = ResourceBundle.getBundle("text", FacesContext.getCurrentInstance().getViewRoot().getLocale());
        refreshList();
    }

    private void refreshList() {
        try {
            currTemplate = new Template();
            templateList = templateService.getTemplateList();
            selectRouteDefault = routeService.getRouteMap();
            selectRoute = toRouteMap(selectRouteDefault);
            selectRouteList = toRouteList(selectRouteDefault);
            selectPart = partService.getPartMap(Part.Type.FOR_TEMPLATES);
            selectFieldTypeDefault = fieldTypeService.getFieldTypeMap();
            selectFieldType = toSelectItemList(selectFieldTypeDefault);
            selectChoiceListDefault = choiceListService.getChoiceListMap();
            selectChoiceList = new ArrayList<>(selectChoiceListDefault.keySet());
            selectCond = Template.Condition.Operation.LIST;
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.list.refresh");
        }
    }

    public void newDialog() {
        createFlag = true;
        currTemplate = new Template();
        currTemplate.setFields(new LinkedList<Template.Field>());
        currTemplate.setConditions(new LinkedList<>());

        RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
    }

    public void editDialog() {
        createFlag = false;

        if (selectedTemplate == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        try {
            currTemplate = templateService.getTemplate(selectedTemplate.getId());

            RequestContext.getCurrentInstance().execute("PF('editDialog').show()");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.element.not.loaded");
        }
    }

    public void saveAction() {
        try {
            if (createFlag) {
                templateService.createTemplate(currTemplate);
            } else {
                templateService.updateTemplate(currTemplate);
            }

            refreshList();
            RequestContext.getCurrentInstance().execute("PF('editDialog').hide();");
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.saved");
        }
    }

    public void deleteDialog() {
        if (selectedTemplate == null) {
            showGrowl(FacesMessage.SEVERITY_WARN, "warn", "error.list.element.not.selected");
            return;
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').show()");
    }

    public void deleteAction() {
        try {
            templateService.deleteTemplate(selectedTemplate.getId());
            refreshList();
        }
        catch (Exception e) {
            showGrowl(FacesMessage.SEVERITY_FATAL, "fatal", "error.not.deleted");
        }

        RequestContext.getCurrentInstance().execute("PF('deleteDialog').hide();");
    }

    public void addField() {
        Template.Field field = new Template.Field();
        if (currTemplate.getFields().isEmpty()) {
            field.setNum(1);
        } else {
            field.setNum(currTemplate.getFields().getLast().getNum() + 1);
        }
        field.setName(fieldName);
        field.setTypeId(selectedFieldType);
        if (selectedFieldType == FieldType.Id.LIST) {
            field.setTypeName(selectFieldTypeDefault.get(selectedFieldType).getName() + " (" + selectedChoiceList + ")");
            field.setChoiceListId(selectChoiceListDefault.get(selectedChoiceList).getId());
            field.setChoiceListName(selectedChoiceList);
        } else {
            field.setTypeName(selectFieldTypeDefault.get(selectedFieldType).getName());
        }
        currTemplate.getFields().add(field);

        fieldName = null;
    }

    public void addCond() {
        String stringCondValue;
        switch (selectFieldNameDefault.get(selectedFieldName).getTypeId()) {
            case FieldType.Id.CALENDAR:
                stringCondValue = LocalDateTime.ofInstant(condCalendarValue.toInstant(),
                        ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                break;
            case FieldType.Id.CHECKBOX:
                stringCondValue = condCheckBoxValue ? "1" : "0";
                break;
            default:
                stringCondValue = condValue;
        }

        Template.Condition condition = new Template.Condition();
        condition.setCondNum(condNum);
        condition.setFieldNum(selectFieldNameDefault.get(selectedFieldName).getNum());
        condition.setFieldName(selectedFieldName);
        condition.setCond(selectedCond);
        condition.setValue(stringCondValue);
        condition.setRouteId(selectRouteDefault.get(selectedCondRoute) != null ? selectRouteDefault.get(selectedCondRoute).getId() : 0);
        condition.setRouteName(selectedCondRoute);
        currTemplate.getConditions().add(condition);

        condNum = 0;
        condValue = null;
    }

    public void fieldTypeChanged() {
        fieldName = null;
        addChoiceListRendered = false;
        addFieldNameRendered = false;

        switch (selectedFieldType) {
            case FieldType.Id.LIST:
                addChoiceListRendered = true;
                selectedChoiceList = null;
                break;
            case FieldType.Id.LINE:
            case FieldType.Id.EMPTY_STRING:
                addFieldNameRendered = true;
                break;
        }
    }

    private List<SelectItem> toSelectItemList(Map<Integer, FieldType> fieldTypes) {
        List<SelectItem> selectItems = new LinkedList<>();
        List<SelectItem> inputSelectItem = new LinkedList<>();
        SelectItemGroup inputItemGroup = new SelectItemGroup();
        List<SelectItem> decorateSelectItem = new LinkedList<>();
        SelectItemGroup decorateItemGroup = new SelectItemGroup();

        for (FieldType fieldType : fieldTypes.values()) {
            if (fieldType.getTypeId() == FieldType.Type.INPUT) {
                inputSelectItem.add(new SelectItem(fieldType.getId(), fieldType.getName()));
                inputItemGroup.setLabel(fieldType.getTypeName());
            } else if (fieldType.getTypeId() == FieldType.Type.DECORATE) {
                decorateSelectItem.add(new SelectItem(fieldType.getId(), fieldType.getName()));
                decorateItemGroup.setLabel(fieldType.getTypeName());
            }
        }
        inputItemGroup.setSelectItems(inputSelectItem.toArray(new SelectItem[inputSelectItem.size()]));
        decorateItemGroup.setSelectItems(decorateSelectItem.toArray(new SelectItem[decorateSelectItem.size()]));
        selectItems.add(inputItemGroup);
        selectItems.add(decorateItemGroup);

        return selectItems;
    }

    private Map<String, Integer> toRouteMap(Map<String, Route> routesMap) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put(bundle.getString("route.zero.name"), 0);
        for (Route route : routesMap.values()) {
            map.put(route.getName(), route.getId());
        }
        return map;
    }

    private List<String> toRouteList(Map<String, Route> routesMap) {
        List<String> list = new LinkedList<>();
        list.add(bundle.getString("route.zero.name"));
        for (Route route : routesMap.values()) {
            list.add(route.getName());
        }
        return list;
    }

    public void tabChanged() {
        selectFieldNameDefault = new HashMap<>();
        selectFieldName = new LinkedList<>();
        selectedFieldName = null;

        for (Template.Field field : currTemplate.getFields()) {
            if (field.getTypeId() == FieldType.Id.TEXT_STRING
                    || field.getTypeId() == FieldType.Id.LIST
                    || field.getTypeId() == FieldType.Id.CALENDAR
                    || field.getTypeId() == FieldType.Id.CHECKBOX
                    || field.getTypeId() == FieldType.Id.TEXT_AREA) {
                selectFieldNameDefault.put(field.getName(), field);
                if (!field.getName().trim().equals("")) {
                    selectFieldName.add(field.getName());
                }
            }
        }

        List<Template.Condition> condForDelete = new ArrayList<>();
        for (Template.Condition condition : currTemplate.getConditions()) {
            boolean hasField = false;
            for (Template.Field field : currTemplate.getFields()) {
                if (field.getName().equals(condition.getFieldName())) {
                    hasField = true;
                    break;
                }
            }
            if (!hasField) {
                condForDelete.add(condition);
            }
        }
        currTemplate.getConditions().removeAll(condForDelete);

        selectedFieldName = selectFieldName.isEmpty() ? null : selectFieldName.get(0);
        fieldNameListChanged();
    }

    public void fieldNameListChanged() {
        condCheckBoxValueRendered = false;
        condCalendarValueRendered = false;
        condValueRendered = true;

        if (selectFieldNameDefault.get(selectedFieldName) == null) {
            return;
        }

        switch (selectFieldNameDefault.get(selectedFieldName).getTypeId()) {
            case FieldType.Id.CALENDAR:
                condCheckBoxValueRendered = false;
                condCalendarValueRendered = true;
                condValueRendered = false;
                break;
            case FieldType.Id.CHECKBOX:
                condCheckBoxValueRendered = true;
                condCalendarValueRendered = false;
                condValueRendered = false;
                break;
            default:
        }
    }

    private void showGrowl(FacesMessage.Severity severity, String shortMessage, String longMessage) {
        FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(
                severity, bundle.getString(shortMessage), bundle.getString(longMessage)));
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setFieldTypeService(FieldTypeService fieldTypeService) {
        this.fieldTypeService = fieldTypeService;
    }

    public void setChoiceListService(ChoiceListService choiceListService) {
        this.choiceListService = choiceListService;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public boolean isCreateFlag() {
        return createFlag;
    }

    public Template getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(Template selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public List<Template> getFilteredTemplate() {
        return filteredTemplate;
    }

    public void setFilteredTemplate(List<Template> filteredTemplate) {
        this.filteredTemplate = filteredTemplate;
    }

    public Template getCurrTemplate() {
        return currTemplate;
    }

    public void setCurrTemplate(Template currTemplate) {
        this.currTemplate = currTemplate;
    }

    public List<String> getSelectRouteList() {
        return selectRouteList;
    }

    public Map<String, Integer> getSelectRoute() {
        return selectRoute;
    }

    public Map<String, Integer> getSelectPart() {
        return selectPart;
    }

    public List<SelectItem> getSelectFieldType() {
        return selectFieldType;
    }

    public int getSelectedFieldType() {
        return selectedFieldType;
    }

    public void setSelectedFieldType(int selectedFieldType) {
        this.selectedFieldType = selectedFieldType;
    }

    public List<String> getSelectChoiceList() {
        return selectChoiceList;
    }

    public String getSelectedChoiceList() {
        return selectedChoiceList;
    }

    public void setSelectedChoiceList(String selectedChoiceList) {
        this.selectedChoiceList = selectedChoiceList;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isAddChoiceListRendered() {
        return addChoiceListRendered;
    }

    public boolean isAddFieldNameRendered() {
        return addFieldNameRendered;
    }

    public int getCondNum() {
        return condNum;
    }

    public void setCondNum(int condNum) {
        this.condNum = condNum;
    }

    public String getSelectedFieldName() {
        return selectedFieldName;
    }

    public void setSelectedFieldName(String selectedFieldName) {
        this.selectedFieldName = selectedFieldName;
    }

    public List<String> getSelectFieldName() {
        return selectFieldName;
    }

    public String getCondValue() {
        return condValue;
    }

    public boolean isCondCheckBoxValue() {
        return condCheckBoxValue;
    }

    public void setCondCheckBoxValue(boolean condCheckBoxValue) {
        this.condCheckBoxValue = condCheckBoxValue;
    }

    public Date getCondCalendarValue() {
        return condCalendarValue;
    }

    public void setCondCalendarValue(Date condCalendarValue) {
        this.condCalendarValue = condCalendarValue;
    }

    public void setCondValue(String condValue) {
        this.condValue = condValue;
    }

    public List<String> getSelectCond() {
        return selectCond;
    }

    public String getSelectedCond() {
        return selectedCond;
    }

    public void setSelectedCond(String selectedCond) {
        this.selectedCond = selectedCond;
    }

    public String getSelectedCondRoute() {
        return selectedCondRoute;
    }

    public void setSelectedCondRoute(String selectedCondRoute) {
        this.selectedCondRoute = selectedCondRoute;
    }

    public boolean isCondValueRendered() {
        return condValueRendered;
    }

    public boolean isCondCheckBoxValueRendered() {
        return condCheckBoxValueRendered;
    }

    public boolean isCondCalendarValueRendered() {
        return condCalendarValueRendered;
    }
}