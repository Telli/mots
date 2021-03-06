package org.motechproject.mots.web;

import java.util.HashMap;
import java.util.Map;
import org.motechproject.mots.exception.BindingResultException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
public abstract class BaseController {

  protected Map<String, String> getErrors(BindingResult bindingResult) {
    Map<String, String> errors = new HashMap<>();

    for (FieldError error : bindingResult.getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return errors;
  }

  protected void checkBindingResult(BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new BindingResultException(getErrors(bindingResult));
    }
  }
}
