package org.molgenis.gavin.job;

import static org.molgenis.gavin.job.input.model.LineType.CADD;
import static org.molgenis.gavin.job.input.model.LineType.COMMENT;
import static org.molgenis.gavin.job.input.model.LineType.ERROR;
import static org.molgenis.gavin.job.input.model.LineType.INDEL_NOCADD;
import static org.molgenis.gavin.job.input.model.LineType.SKIPPED;
import static org.molgenis.gavin.job.input.model.LineType.VCF;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.CADDS;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.COMMENTS;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.ERRORS;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.FILENAME;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.INDELS_NOCADD;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.INPUT_FILE_EXTENSION;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.SKIPPEDS;
import static org.molgenis.gavin.job.meta.GavinJobExecutionMetaData.VCFS;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import org.molgenis.data.Entity;
import org.molgenis.data.jobs.model.JobExecution;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.gavin.job.input.model.LineType;

public class GavinJobExecution extends JobExecution {
  private static final long serialVersionUID = 1L;
  private static final String GAVIN = "gavin";

  public GavinJobExecution(Entity entity) {
    super(entity);
    setType(GAVIN);
  }

  public GavinJobExecution(EntityType entityType) {
    super(entityType);
    setType(GAVIN);
  }

  public GavinJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(GAVIN);
  }

  public String getFilename() {
    return getString(FILENAME);
  }

  public void setFilename(String filename) {
    set(FILENAME, filename);
  }

  public String getInputFileExtension() {
    return getString(INPUT_FILE_EXTENSION);
  }

  public void setInputFileExtension(String extension) {
    set(INPUT_FILE_EXTENSION, extension);
  }

  public void setLineTypes(Multiset<LineType> lineTypes) {
    set(COMMENTS, lineTypes.count(COMMENT));
    set(VCFS, lineTypes.count(VCF));
    set(CADDS, lineTypes.count(CADD));
    set(ERRORS, lineTypes.count(ERROR));
    set(INDELS_NOCADD, lineTypes.count(INDEL_NOCADD));
    set(SKIPPEDS, lineTypes.count(SKIPPED));
  }

  public Multiset<LineType> getLineTypes() {
    ImmutableMultiset.Builder<LineType> builder = ImmutableMultiset.builder();
    builder.addCopies(COMMENT, getInt(COMMENTS));
    builder.addCopies(VCF, getInt(VCFS));
    builder.addCopies(CADD, getInt(CADDS));
    builder.addCopies(ERROR, getInt(ERRORS));
    builder.addCopies(INDEL_NOCADD, getInt(INDELS_NOCADD));
    builder.addCopies(SKIPPED, getInt(SKIPPEDS));
    return builder.build();
  }
}
