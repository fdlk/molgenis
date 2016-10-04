package org.molgenis.data.mapper.service;

import java.util.List;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.mapper.mapping.model.AttributeMapping;
import org.molgenis.data.mapper.mapping.model.EntityMapping;
import org.molgenis.data.mapper.mapping.model.MappingProject;
import org.molgenis.data.mapper.mapping.model.MappingTarget;
import org.molgenis.data.mapper.repository.AttributeMappingRepository;
import org.molgenis.data.mapper.repository.EntityMappingRepository;

public interface MappingService
{
	/**
	 * Creates a new {@link MappingProject}
	 *
	 * @param target
	 *            name of the first target entity
	 */
	MappingProject addMappingProject(String name, MolgenisUser owner, String target);

	/**
	 * Retrieves all {@link MappingProject}s.
	 *
	 * @return list of all {@link MappingProject}s.
	 */
	List<MappingProject> getAllMappingProjects();

	/**
	 * Updates a MappingProject in the repository. All {@link MappingTarget}s, {@link EntityMapping}s and
	 * {@link AttributeMapping}s are updated.
	 *
	 * @param mappingProject
	 *            the {@link MappingProject} to update.
	 */
	void updateMappingProject(MappingProject mappingProject);

	/**
	 * Retrieves a {@link MappingProject} from the repository.
	 *
	 * @param identifier
	 *            ID of the {@link MappingProject}
	 * @return the retrieved MappingProject
	 */
	MappingProject getMappingProject(String identifier);

	/**
	 * Applies all mappings in a {@link MappingTarget} Adds the source attribute by default
	 *
	 * @param mappingTarget
	 *            the MappingTarget whose mappings are applied
	 * @param entityName
	 *            the name of the entity to map to
	 * @return fully qualified name of the generated entity
	 */
	String applyMappings(MappingTarget mappingTarget, String entityName);

	/**
	 * Applies all mappings in a {@link MappingTarget}
	 *
	 * @param mappingTarget
	 *            the MappingTarget whose mappings are applied
	 * @param entityName
	 *            the name of the entity to map to
	 * @param addSourceAttribute
	 *            boolean indicating if the 'source' attribute should be added to the target repository
	 * @return fully qualified name of the generated entity
	 */
	String applyMappings(MappingTarget mappingTarget, String entityName, boolean addSourceAttribute);

	/**
	 * Deletes a {@link MappingProject}
	 *
	 * @param mappingProjectId
	 *            id of the {@link MappingProject} to delete
	 */
	void deleteMappingProject(String mappingProjectId);

	/**
	 * Clones a {@link MappingProject}. Deep copies all related mappings. Automatically generates name for cloned
	 * {@link MappingProject}.
	 *
	 * @param mappingProjectId
	 *            id of the {@link MappingProject} to clone
	 * @return cloned {@link MappingProject}
	 */
	MappingProject cloneMappingProject(String mappingProjectId);

	/**
	 * Clones a {@link MappingProject}. Deep copies all related mappings.
	 *
	 * @param mappingProjectId
	 *            id of the {@link MappingProject} to clone
	 * @param clonedMappingProjectName
	 *            name of the cloned {@link MappingProject}
	 * @return cloned {@link MappingProject}
	 */
	MappingProject cloneMappingProject(String mappingProjectId, String clonedMappingProjectName);

	String generateId(AttributeType dataType, Long count);

	/**
	 * Update an {@link EntityMapping} in the {@link EntityMappingRepository}
	 * 
	 * @param mappingForSource
	 */
	void updateMappingEntity(EntityMapping mappingForSource);

	/**
	 * Cascading delete the {@link EntityMapping} associated with the {@link MappingTarget}
	 * 
	 * @param mappingTarget
	 * @param entityMapping
	 */
	void removeEntityMapping(MappingTarget mappingTarget, EntityMapping entityMapping);

	/**
	 * Update an {@link AttributeMapping} in the {@link AttributeMappingRepository}
	 * 
	 * @param attributeMapping
	 */
	void updateAttributeMapping(AttributeMapping attributeMapping);
}
