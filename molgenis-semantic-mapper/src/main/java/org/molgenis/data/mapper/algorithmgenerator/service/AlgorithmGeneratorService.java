package org.molgenis.data.mapper.algorithmgenerator.service;

import java.util.List;
import java.util.Map;
import org.molgenis.data.mapper.algorithmgenerator.bean.GeneratedAlgorithm;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;

public interface AlgorithmGeneratorService {
  String generate(
      Attribute targetAttribute,
      List<Attribute> sourceAttributes,
      EntityType targetEntityType,
      EntityType sourceEntityType);

  GeneratedAlgorithm generate(
      Attribute targetAttribute,
      Map<Attribute, ExplainedAttribute> sourceAttributes,
      EntityType targetEntityType,
      EntityType sourceEntityType);
}
