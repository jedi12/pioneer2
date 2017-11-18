package ru.pioneersystem.pioneer2.view;

import ru.pioneersystem.pioneer2.model.*;
import ru.pioneersystem.pioneer2.service.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean
@ViewScoped
public class TestView implements Serializable {
    private static final long serialVersionUID = 1L;

    private static Random randomGenerator = new Random();
    private static final String[] words = new String[] {"русским ", "беспредельною ", "свойствами ", "наклонностей ",
            "бронзовые ", "закована ", "слепым ", "благородством ", "подшитое ", "обвила ", "телами ", "окрестностях ",
            "невыразимые ", "догадаются ", "брошюрами ", "понравилась ", "приехавшая ", "поражающей ", "масальского ",
            "улыбаться ", "гагина ", "навестившей ", "нетерпеливее ", "стефенсон ", "передняя ", "усыновить ",
            "красному ", "господ ", "духота ", "взрывали ", "билетом ", "училось ", "штрафами ", "приискать ",
            "полынью ", "оживилось ", "рапорт ", "переднему ", "речкой ", "дрожавшим ", "слыхав ", "вкусна ",
            "смыслит ", "туманя ", "необычное ", "скаты ", "колесами ", "раздольнее ", "некого ", "пошевелил ",
            "связав ", "елеонской ", "грациозностью ", "устроилось ", "дувр ", "шуму ", "развилась ", "впал ",
            "отеческие ", "браво ", "чихнул ", "перилы ", "холостой ", "сану ", "маску ", "паркете ", "щитов ",
            "тесной ", "самостоятельное ", "штейнов ", "начинаний ", "упирал ", "расположенном ", "оставлю ",
            "крошечном ", "нарисовала ", "мгновенна ", "узорчатые ", "фрида ", "отъявленнейшими ", "моду ", "баклажки ",
            "удивилась ", "оли ", "тяжелый ", "обрезанные ", "вентиляцию ", "стрекозами ", "пылящую ", "вздрогнувшей ",
            "жестких ", "зажмуря ", "найдем ", "однодневная ", "жаров ", "поддерживали ", "пьянит ", "пугливы",
            "0 ", "1 ", "22 ", "333 ", "4444 ", "55555 ", "666666 ", "7777777 ", "88888888 ", "999999999 ", "0000000000 "
    };

    String createMess;
    String fillMess;

    @ManagedProperty("#{currentUser}")
    private CurrentUser currentUser;

    @ManagedProperty("#{companyService}")
    private CompanyService companyService;

    @ManagedProperty("#{userService}")
    private UserService userService;

    @ManagedProperty("#{groupService}")
    private GroupService groupService;

    @ManagedProperty("#{routeService}")
    private RouteService routeService;

    @ManagedProperty("#{partService}")
    private PartService partService;

    @ManagedProperty("#{choiceListService}")
    private ChoiceListService choiceListService;

    @ManagedProperty("#{templateService}")
    private TemplateService templateService;

    @ManagedProperty("#{documentService}")
    private DocumentService documentService;

    @PostConstruct
    public void init() {

    }

    public void startCreateObjects() {
        try {
            Date beginDate = new Date();

            List<Company> companyList = createCompanies();
            for (int index = 1; index <= companyList.size(); index = index + 1) {
                Company company = companyList.get(index -1);
                currentUser.signIn(company.getUserLogin(), company.getUserPass());
                createCreatorUsers(index);
                createRouteUsers(index);
                createTemplates(index);
            }

            Date endDate = new Date();
            long diff = endDate.getTime() - beginDate.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String elapsedTime = sdf.format(new Date(diff));

            createMess = "Завершено за " + elapsedTime;
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Информация", createMess));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка", e.getMessage()));
        }
    }

    public void startFillDataBase() {
        try {
            Date beginDate = new Date();

            ChoiceList choiceList = new ChoiceList();
            choiceList.setId(1);
            choiceList.setCompanyId(1);
            choiceList = choiceListService.getChoiceList(choiceList);

            Map<Integer, List<Integer>> templateMap = new HashMap<>();
            int oldCompanyId = -1;
            List<Integer> templateListForCompany = null;
            for (Template template: templateService.getTemplateList()) {
                if (template.getCompanyId() != oldCompanyId) {
                    templateListForCompany = new ArrayList<>();
                    templateMap.put(template.getCompanyId(), templateListForCompany);
                    oldCompanyId = template.getCompanyId();
                }

                templateListForCompany.add(template.getId());
            }
            System.out.println("Получен список шаблонов для всех организаций");

            List<User> users = userService.getUserList();
            for (int index = 2; index <= users.size(); index = index + 1) {
                currentUser.signIn(users.get(index).getLogin(), "123");
                if (!currentUser.getUserCreateGroups().isEmpty()) {
                    createDocuments(templateMap.get(users.get(index).getCompanyId()), choiceList);
                }

                List<Menu> menus = currentUser.getUserMenu();
                for (Menu menu: menus) {
                    if (menu.getRoleId() > 9) {
                        currentUser.setCurrPage(menu);
                        confirmDocuments();
                    }
                }


                System.out.println("Созданы документы для пользователя: " + users.get(index).getLogin());
            }

            Date endDate = new Date();
            long diff = endDate.getTime() - beginDate.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String elapsedTime = sdf.format(new Date(diff));

            fillMess = "Завершено за " + elapsedTime;
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_INFO, "Информация", fillMess));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("growl", new FacesMessage(FacesMessage.SEVERITY_FATAL, "Ошибка", e.getMessage()));
        }
    }

    private List<Company> createCompanies() throws Exception {
        List<Company> companies = new ArrayList<>();
        for (int index = 1; index <= 100; index = index + 1) {
            Company company = companyService.getNewCompany();
            company.setName("Компания " + index);
            company.setFullName("Компания " + index + " Полное название");
            company.setAddress("Адрес Компании " + index);
            company.setPhone("555-" + index);
            company.setEmail("company" + index + "@company" + index + ".ru");
            company.setSite("site" + index + ".server.ru");
            company.setComment("Комментарий для Компании " + index);
            company.setMaxUsers(120);
            company.setUserName("Администратор Компании " + index);
            company.setUserLogin("admin0company" + index);
            company.setUserEmail("admin0company" + index + "@company" + index + ".ru");
            company.setGroupName("Группа 0 Администратор Компании " + index);
            company.setUserPass("123");

            companyService.saveCompany(company);
            companies.add(company);
            System.out.println("Создана компания: " + company.getName());
        }
        return companies;
    }

    private void createCreatorUsers(int company) throws Exception {
        for (int index = 1; index <= 100; index = index + 1) {
            User user = userService.getNewUser();
            user.setName("Пользователь " + index + " Компания " + company);
            user.setLogin("user" + index + "company" + company);
            user.setPosition("Бухгалтер" + index);
            user.setEmail("user" + index + "@company" + company + ".ru");
            user.setPhone(company + "-111-111-" + index);
            user.setComment("Комментарий для пользователя " + index);

            userService.saveUser(user);
            userService.setUserPass(user, "123");
            System.out.println("Создан пользователь-создатель документов: " + user.getName());
        }
    }

    private void createRouteUsers(int company) throws Exception {
        Map<String, Group> routeGroups =  groupService.getPointMap();

        for (int index = 1; index <= 3; index = index + 1) {
            User user = userService.getNewUser();
            user.setName("Пользователь-асогласователь " + index + " Компания " + company);
            user.setLogin("coordUser" + index + "company" + company);
            user.setPosition("Главный бухгалтер " + index);
            user.setEmail("coordUser" + index + "@company" + company + ".ru");
            user.setPhone(company + "-222-222-" + index);
            user.setComment("Комментарий для пользователя-асогласователя " + index);

            User.LinkGroup linkGroup = new User.LinkGroup();
            linkGroup.setGroupId(routeGroups.get("Согласователи (Пример)").getId());
            linkGroup.setParticipant(true);

            List<User.LinkGroup> linkGroups = new ArrayList<>();
            linkGroups.add(linkGroup);
            user.setLinkGroups(linkGroups);

            userService.saveUser(user);
            userService.setUserPass(user, "123");
            System.out.println("Создан пользователь-асогласователь: " + user.getName());
        }

        for (int index = 1; index <= 3; index = index + 1) {
            User user = userService.getNewUser();
            user.setName("Пользователь-исполнитель " + index + " Компания " + company);
            user.setLogin("execUser" + index + "company" + company);
            user.setPosition("Инженер " + index);
            user.setEmail("execUser" + index + "@company" + company + ".ru");
            user.setPhone(company + "-333-333-" + index);
            user.setComment("Комментарий для пользователя-исполнителя " + index);

            User.LinkGroup linkGroup = new User.LinkGroup();
            linkGroup.setGroupId(routeGroups.get("Исполнители (Пример)").getId());
            linkGroup.setParticipant(true);

            List<User.LinkGroup> linkGroups = new ArrayList<>();
            linkGroups.add(linkGroup);
            user.setLinkGroups(linkGroups);

            userService.saveUser(user);
            userService.setUserPass(user, "123");
            System.out.println("Создан пользователь-исполнитель: " + user.getName());
        }
    }

    private void createTemplates(int company) throws Exception {
        List<Route> routes = routeService.getRouteList();
        List<Part> parts = partService.getPartList(1);
        List<ChoiceList> choiceLists = choiceListService.getChoiceListList();

        int routeId = routes.get(randomGenerator.nextInt(routes.size())).getId();
        int partId = parts.get(randomGenerator.nextInt(routes.size())).getId();
        int choiceListId = choiceLists.get(randomGenerator.nextInt(routes.size())).getId();
        createTemplate2(routeId, partId, choiceListId, company);

        routeId = routes.get(randomGenerator.nextInt(routes.size())).getId();
        partId = parts.get(randomGenerator.nextInt(routes.size())).getId();
        choiceListId = choiceLists.get(randomGenerator.nextInt(routes.size())).getId();
        createTemplate3(routeId, partId, choiceListId, company);
    }

    private void createTemplate2(int routeId, int partId, int choiceListId, int company) throws Exception {
        Template template = templateService.getNewTemplate();
        template.setName("Тестовый шаблон " + company);
        template.setRouteId(routeId);
        template.setPartId(partId);

        Document.Field choiceListField = new Document.Field();
        choiceListField.setNum(1);
        choiceListField.setTypeId(FieldType.Id.CHOICE_LIST);
        choiceListField.setChoiceListId(choiceListId);
        choiceListField.setName("Выберите вариант");
        template.getFields().add(choiceListField);

        Document.Field checkboxField = new Document.Field();
        checkboxField.setNum(2);
        checkboxField.setTypeId(FieldType.Id.CHECKBOX);
        checkboxField.setName("Согласие на использование материала");
        template.getFields().add(checkboxField);

        Document.Field calendarField = new Document.Field();
        calendarField.setNum(3);
        calendarField.setTypeId(FieldType.Id.CALENDAR);
        calendarField.setName("Дата изготовления товара");
        template.getFields().add(calendarField);

        Document.Field textStringField = new Document.Field();
        textStringField.setNum(4);
        textStringField.setTypeId(FieldType.Id.TEXT_STRING);
        textStringField.setName("Тут можно написать информацию о товаре");
        template.getFields().add(textStringField);

        Document.Field textAreaField = new Document.Field();
        textAreaField.setNum(5);
        textAreaField.setTypeId(FieldType.Id.TEXT_AREA);
        textAreaField.setName("Оставьте свой комментарий");
        template.getFields().add(textAreaField);

        templateService.saveTemplate(template);
        System.out.println("Создан Шаблон 2 для Компании : " + company);
    }

    private void createTemplate3(int routeId, int partId, int choiceListId, int company) throws Exception {
        Template template = templateService.getNewTemplate();
        template.setName("Заявка " + company + " для получения товара");
        template.setRouteId(routeId);
        template.setPartId(partId);

        Document.Field textStringField = new Document.Field();
        textStringField.setNum(1);
        textStringField.setTypeId(FieldType.Id.TEXT_STRING);
        textStringField.setName("Компания " + company + " текстовая строка 1");
        template.getFields().add(textStringField);

        textStringField = new Document.Field();
        textStringField.setNum(2);
        textStringField.setTypeId(FieldType.Id.TEXT_STRING);
        textStringField.setName("Компания " + company + " текстовая строка 2");
        template.getFields().add(textStringField);

        Document.Field checkboxField = new Document.Field();
        checkboxField.setNum(3);
        checkboxField.setTypeId(FieldType.Id.CHECKBOX);
        checkboxField.setName("Компания " + company + " чекбокс");
        template.getFields().add(checkboxField);

        Document.Field textAreaField = new Document.Field();
        textAreaField.setNum(4);
        textAreaField.setTypeId(FieldType.Id.TEXT_AREA);
        textAreaField.setName("Компания " + company + " текстоввое поле 1");
        template.getFields().add(textAreaField);

        Document.Field calendarField = new Document.Field();
        calendarField.setNum(5);
        calendarField.setTypeId(FieldType.Id.CALENDAR);
        calendarField.setName("Компания " + company + " календарь");
        template.getFields().add(calendarField);

        Document.Field choiceListField = new Document.Field();
        choiceListField.setNum(6);
        choiceListField.setTypeId(FieldType.Id.CHOICE_LIST);
        choiceListField.setChoiceListId(choiceListId);
        choiceListField.setName("Компания " + company + " список выбора");
        template.getFields().add(choiceListField);

        textAreaField = new Document.Field();
        textAreaField.setNum(7);
        textAreaField.setTypeId(FieldType.Id.TEXT_AREA);
        textAreaField.setName("Компания " + company + " текстоввое поле 2");
        template.getFields().add(textAreaField);

        Document.Condition condition = new Document.Condition();
        condition.setCondNum(1);
        condition.setFieldNum(6);
        condition.setCond("Равно");
        condition.setValue("Epson FX-890");
        condition.setRouteId(routeId);
        template.getConditions().add(condition);

        condition = new Document.Condition();
        condition.setCondNum(2);
        condition.setFieldNum(4);
        condition.setCond("Равно");
        condition.setValue("1234567890");
        condition.setRouteId(routeId);
        template.getConditions().add(condition);

        templateService.saveTemplate(template);
        System.out.println("Создан Шаблон 3 для Компании : " + company);
    }

    private void createDocuments(List<Integer> templates, ChoiceList choiceList) throws Exception {
        for (int index = 1; index <= 100; index = index + 1) {
            int templateId = templates.get(randomGenerator.nextInt(templates.size()));
            Document document = documentService.getNewDocument(templateId);
            for (Document.Field field: document.getFields()) {
                switch (field.getTypeId()) {
                    case FieldType.Id.TEXT_STRING:
                        field.setValueTextField(getRandomWord(96));
                        break;
                    case FieldType.Id.CHOICE_LIST:
                        List<String> values = choiceList.getValues();
                        field.setValueChoiceList(values.get(randomGenerator.nextInt(values.size())));
                        break;
                    case FieldType.Id.CALENDAR:
                        field.setValueCalendar(new Date(randomGenerator.nextLong()));
                        break;
                    case FieldType.Id.CHECKBOX:
                        field.setValueCheckBox(randomGenerator.nextBoolean());
                        break;
                    case FieldType.Id.TEXT_AREA:
                        field.setValueTextArea(getRandomWord(128));
                        break;
                }
            }

            switch (randomGenerator.nextInt(2)) {
                case 0:
                    documentService.saveDocument(document);
                    break;
                case 1:
                    documentService.saveAndSendDocument(document);
                    break;
            }
        }
    }

    private void confirmDocuments() throws Exception {
        List<Document> documents = documentService.getOnRouteDocumentList();
        for (Document document: documents) {
            switch (randomGenerator.nextInt(2)) {
                case 0:
                    document.setSignerComment("Не возражаю");
                    documentService.acceptDocument(document);
                    break;
                case 1:
                    document.setSignerComment("Категорически против");
                    documentService.rejectDocument(document);
                    break;
            }

        }
    }

    private String getRandomWord(int count) throws Exception {
        StringBuilder sb = new StringBuilder("");
        while (sb.length() < count) {
            sb.append(words[randomGenerator.nextInt(words.length)]);
        }

        return sb.toString().substring(0, count - 1);
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public void setCompanyService(CompanyService companyService) {
        this.companyService = companyService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setRouteService(RouteService routeService) {
        this.routeService = routeService;
    }

    public void setPartService(PartService partService) {
        this.partService = partService;
    }

    public void setChoiceListService(ChoiceListService choiceListService) {
        this.choiceListService = choiceListService;
    }

    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public String getCreateMess() {
        return createMess;
    }

    public String getFillMess() {
        return fillMess;
    }
}