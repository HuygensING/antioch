package nl.knaw.huygens.alexandria.reference;

import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

@RunWith(ConcordionRunner.class)
public class ReferenceFixture {
  private final List<String> references = Lists.newArrayList();

  public void setUpReference(final String id) {
    references.add(id);
  }

  public String createReference(final String id) {
    if (Strings.isNullOrEmpty(id)) {
      return "400 Bad Request";
    }

    if (references.contains(id)) {
      return "409 Conflict";
    }

    references.add(id);
    return "201 Created";
  }
}
