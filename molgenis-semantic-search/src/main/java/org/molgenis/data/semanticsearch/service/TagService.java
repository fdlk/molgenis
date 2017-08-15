package org.molgenis.data.semanticsearch.service;

import com.google.common.collect.Multimap;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

/**
 * Service that administrates tags on attributes, entities and packages of a particular code system.
 *
 * @param <T> the type of the tag object, for instance {@link OntologyTerm}
 * @param <C> the type of the code system, for instance {@link Ontology}
 */
public interface TagService<T, C> {
  /** Retrieves all tags for an attribute, and groups them by relation */
  Multimap<Relation, T> getTagsForAttribute(EntityType entityType, Attribute attribute);

  /** Retrieves all tags for a package */
  Iterable<SemanticTag<Package, T, C>> getTagsForPackage(Package p);

  /** Retrieves all tags for an entity. */
  Iterable<SemanticTag<EntityType, LabeledResource, LabeledResource>> getTagsForEntity(
      EntityType entityType);

  /**
   * Tags an attribute.
   *
   * @param entityType the entity the tagged attribute sits on
   * @param tag the tag to add
   */
  void addAttributeTag(
      EntityType entityType, SemanticTag<Attribute, T, C> tag);

  /**
   * Removes attribute tag
   *
   * @param entityType the entity the tagged attribute sits on
   * @param tag the tag to remove
   */
  void removeAttributeTag(
      EntityType entityType, SemanticTag<Attribute, T, C> tag);

  /**
   * Tags an entity.
   *
   * @param tag the tag to add
   */
  void addEntityTag(SemanticTag<EntityType, T, C> tag);

  /**
   * Removes an entity tag.
   *
   * @param tag the tag to remove
   */
  void removeEntityTag(SemanticTag<EntityType, T, C> tag);

  /**
   * Removes all tags for a given entity
   *
   * @param entityTypeId the name of the entity for which all tags should be removed
   */
  void removeAllTagsFromEntity(String entityTypeId);
}
