package nl.knaw.huygens.alexandria.endpoint.command;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ResourceViewId {
  private UUID resourceId;
  private String textViewName;

  public ResourceViewId(UUID resourceId, String textViewName) {
    this.resourceId = resourceId;
    this.textViewName = textViewName;
  }

  public static ResourceViewId fromString(String idString) {
    String[] parts = idString.split(":");
    UUID uuid = UUID.fromString(parts[0]);
    String name = (parts.length > 1) ? parts[1] : null;
    return new ResourceViewId(uuid, name);
  }

  public UUID getResourceId() {
    return resourceId;
  }

  public Optional<String> getTextViewName() {
    return Optional.ofNullable(textViewName);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ResourceViewId //
        && ((ResourceViewId) obj).getTextViewName().equals(getTextViewName()) //
        && ((ResourceViewId) obj).getResourceId().toString().equals(resourceId.toString());
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this, false);
  }

}
