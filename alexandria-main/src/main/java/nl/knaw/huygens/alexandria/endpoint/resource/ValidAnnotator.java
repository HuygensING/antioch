package nl.knaw.huygens.alexandria.endpoint.resource;

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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import nl.knaw.huygens.Log;
import nl.knaw.huygens.alexandria.api.model.Annotator;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidAnnotator.Validator.class)
public @interface ValidAnnotator {
  String message() default "{nl.knaw.huygens.alexandria.endpoint.resource.MatchesPathCode.message}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<ValidAnnotator, Annotator> {
    final UriInfo uriInfo;

    @Inject
    public Validator(UriInfo uriInfo) {
      this.uriInfo = uriInfo;
    }

    @Override
    public void initialize(ValidAnnotator constraintAnnotation) {
      Log.info("initialize: annotation={}", constraintAnnotation);
    }

    @Override
    public boolean isValid(Annotator annotator, ConstraintValidatorContext context) {
      if (annotator == null) {
        return true; // Non-null validation, if desired, should be enforced via @NotNull
      }

      final String code = annotator.getCode();

      // Either no code was passed in prototype, or it MUST equal the id in URI
      return StringUtils.isEmpty(code) || value(annotatorCode()).equals(code);
    }

    private String value(PathSegment segment) {
      return segment.getPath();
    }


    private PathSegment annotatorCode() {
      return annotatorCodeSegment(uriSegments());
    }

    private PathSegment annotatorCodeSegment(List<PathSegment> pathSegments) {
      final int annotatorCodeSegmentIndex = uriSegments().size() - 1;
      return pathSegments.get(annotatorCodeSegmentIndex);
    }

    private List<PathSegment> uriSegments() {
      return uriInfo.getPathSegments();
    }
  }

}
