@AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);
    public abstract Builder setNumberOfLegs(int value);
    public abstract Animal build();
  }