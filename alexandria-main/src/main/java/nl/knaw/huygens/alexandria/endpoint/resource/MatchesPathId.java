package nl.knaw.huygens.alexandria.endpoint.resource;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import nl.knaw.huygens.alexandria.endpoint.UUIDParam;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MatchesPathId.Validator.class)
public @interface MatchesPathId {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathId.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<MatchesPathId, ResourcePrototype> {
    final UriInfo uriInfo;

    @Inject
    public Validator(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    @Override
    public void initialize(MatchesPathId constraintAnnotation) {
      // Log.info("initialize: annotation={}", constraintAnnotation);
    }

    @Override
    public boolean isValid(ResourcePrototype prototype, ConstraintValidatorContext context) {
      if (prototype == null) {
        return true; // Non-null validation, if desired, should be enforced via @NotNull
      }

      final UUIDParam prototypeId = prototype.getId();

      // Either no id was passed in prototype, or it MUST equal the id in URI
      return prototypeId == null || value(uriId()).equals(value(prototypeId));
    }

    private UUID value(PathSegment segment) {
      return UUID.fromString(segment.getPath());
    }

    private UUID value(UUIDParam uuid) {
      return uuid.getValue();
    }

    private PathSegment uriId() {
      return uuidSegment(uriSegments());
    }

    private PathSegment uuidSegment(List<PathSegment> pathSegments) {
      final int uuidSegmentIndex = uriSegments().size() - 1;
      return pathSegments.get(uuidSegmentIndex);
    }

    private List<PathSegment> uriSegments() {
      return uriInfo.getPathSegments();
    }
  }

}
