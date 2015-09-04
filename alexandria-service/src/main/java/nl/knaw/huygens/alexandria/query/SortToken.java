package nl.knaw.huygens.alexandria.query;

class SortToken {
  private QueryField field;
  private boolean ascending = true;

  public QueryField getField() {
    return field;
  }

  public SortToken setField(final QueryField field) {
    this.field = field;
    return this;
  }

  public boolean isAscending() {
    return ascending;
  }

  public SortToken setAscending(final boolean ascending) {
    this.ascending = ascending;
    return this;
  }
}