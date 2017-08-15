package org.molgenis.data.annotation.web.meta;

import java.util.LinkedList;
import org.molgenis.data.meta.model.Attribute;

public interface AnnotatorEntityType {
  LinkedList<Attribute> getOrderedAttributes();
}
