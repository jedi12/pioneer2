package ru.pioneersystem.pioneer2.view.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.ResourceBundle;

@FacesValidator("notEmptyString")
public class NotEmptyString implements Validator {
    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if  (
                value == null
                || (value instanceof String && ((String) value).trim().equals(""))
                || (value instanceof Integer && ((Integer) value) == 0)
            )
        {
            ResourceBundle bundle = ResourceBundle.getBundle("text", context.getViewRoot().getLocale());
            throw new ValidatorException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, bundle.getString("error"), bundle.getString("validator.value_required")));
        }
    }
}