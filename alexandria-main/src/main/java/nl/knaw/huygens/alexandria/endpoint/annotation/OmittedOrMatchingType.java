package nl.knaw.huygens.alexandria.endpoint.annotation;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2016 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nl.knaw.huygens.alexandria.endpoint.annotation.AnnotationsEndpoint.annotationNotFoundForId;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.PathParam;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotation;
import nl.knaw.huygens.alexandria.model.AlexandriaAnnotationBody;
import nl.knaw.huygens.alexandria.service.AlexandriaService;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OmittedOrMatchingType.Validator.class)
public @interface OmittedOrMatchingType {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.annotation.OmittedOrMatchingType.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<OmittedOrMatchingType, AnnotationPrototype> {
    private static final boolean OMITTED_TYPE_CONSIDERED_VALID = true;

    private final String currentType;

    @Inject
    public Validator(AlexandriaService service, @PathParam("uuid") UUIDParam uuidParam) {
      currentType = service.readAnnotation(uuidParam.getValue()) //
          .map(AlexandriaAnnotation::getBody) //
          .map(AlexandriaAnnotationBody::getType) //
          .orElseThrow(annotationNotFoundForId(uuidParam));
    }

    @Override
    public void initialize(OmittedOrMatchingType constraintAnnotation) {
    }

    @Override
    public boolean isValid(AnnotationPrototype prototype, ConstraintValidatorContext context) {
      if (prototype == null) {
        return true; // Non-null validation, if desired, should be enforced via @NotNull
      }

      return prototype.getType().map(currentType::equals).orElse(OMITTED_TYPE_CONSIDERED_VALID);
    }
  }
}
