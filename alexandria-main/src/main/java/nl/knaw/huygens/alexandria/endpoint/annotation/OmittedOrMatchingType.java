package nl.knaw.huygens.alexandria.endpoint.annotation;

/*
 * #%L
 * alexandria-main
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
