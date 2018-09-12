package org.molgenis.oneclickimporter.service;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.EntityManager.CreationMode.NO_POPULATE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.security.PackagePermission.ADD_PACKAGE;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.security.PackageIdentity;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.oneclickimporter.model.Column;
import org.molgenis.oneclickimporter.model.DataCollection;
import org.molgenis.oneclickimporter.service.impl.EntityServiceImpl;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EntityServiceImplTest {
  @Mock private EntityTypeFactory entityTypeFactory;

  @Mock private AttributeFactory attributeFactory;

  @Mock private IdGenerator idGenerator;

  @Mock private DataService dataService;

  @Mock private MetaDataService metaDataService;

  @Mock private EntityManager entityManager;

  @Mock private AttributeTypeService attributeTypeService;

  @Mock private OneClickImporterService oneClickImporterService;

  @Mock private OneClickImporterNamingService oneClickImporterNamingService;

  @Mock private PackageFactory packageFactory;

  @Mock private PermissionSystemService permissionSystemService;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  private EntityService entityService;

  @BeforeClass
  public void beforeClass() {
    initMocks(this);
  }

  @Test
  public void testCreateEntity() {
    String tableName = "super-powers";
    List<Object> userNames = Arrays.asList("Mark", "Mariska", "Bart");
    List<Object> superPowers = Arrays.asList("Arrow functions", "Cookies", "Knots");
    List<Column> columns =
        Arrays.asList(
            Column.create("user name", 0, userNames), Column.create("super power", 1, superPowers));
    DataCollection dataCollection = DataCollection.create(tableName, columns);

    // mock auto id
    String generatedId = "id_0";
    when(idGenerator.generateId()).thenReturn(generatedId);

    // mock attributes
    Attribute idAttr = mock(Attribute.class);
    when(idAttr.setName(anyString())).thenReturn(idAttr);
    when(idAttr.setVisible(anyBoolean())).thenReturn(idAttr);
    when(idAttr.setAuto(anyBoolean())).thenReturn(idAttr);
    when(idAttr.setIdAttribute(anyBoolean())).thenReturn(idAttr);

    Attribute nameAttr = mock(Attribute.class);
    when(nameAttr.getDataType()).thenReturn(STRING);

    Attribute powerAttr = mock(Attribute.class);
    when(powerAttr.getDataType()).thenReturn(STRING);

    when(attributeFactory.create()).thenReturn(idAttr, nameAttr, powerAttr);

    // mock table
    EntityType table = mock(EntityType.class);
    when(entityTypeFactory.create()).thenReturn(table);
    when(table.getId()).thenReturn(generatedId);

    when(table.getAttribute("user_name")).thenReturn(nameAttr);
    when(table.getAttribute("super_power")).thenReturn(powerAttr);

    // mock package
    Package package_ = mock(Package.class);
    when(metaDataService.getPackage("parent_package_")).thenReturn(Optional.of(package_));

    when(dataService.getMeta()).thenReturn(metaDataService);

    // mock rows
    Entity row1 = mock(Entity.class);
    when(row1.getEntityType()).thenReturn(table);

    Entity row2 = mock(Entity.class);
    when(row2.getEntityType()).thenReturn(table);

    Entity row3 = mock(Entity.class);
    when(row3.getEntityType()).thenReturn(table);

    when(entityManager.create(table, NO_POPULATE)).thenReturn(row1, row2, row3);

    when(oneClickImporterNamingService.asValidColumnName("user name")).thenReturn("user_name");
    when(oneClickImporterNamingService.asValidColumnName("super power")).thenReturn("super_power");
    when(oneClickImporterNamingService.getLabelWithPostFix("super-powers"))
        .thenReturn("super-powers");

    when(attributeTypeService.guessAttributeType(any())).thenReturn(STRING);

    doReturn(true)
        .when(userPermissionEvaluator)
        .hasPermission(new PackageIdentity("parent"), ADD_PACKAGE);
    when(metaDataService.getPackages()).thenReturn(Collections.singletonList(package_));
    when(package_.getId()).thenReturn("parent");

    entityService =
        new EntityServiceImpl(
            entityTypeFactory,
            attributeFactory,
            idGenerator,
            dataService,
            metaDataService,
            entityManager,
            attributeTypeService,
            oneClickImporterService,
            oneClickImporterNamingService,
            packageFactory,
            permissionSystemService,
            userPermissionEvaluator);

    EntityType entityType = entityService.createEntityType(dataCollection, "package_");
    assertEquals(entityType.getId(), generatedId);

    verify(table).setPackage(package_);
    verify(table).setId(generatedId);
    verify(table).setLabel(tableName);
    verify(permissionSystemService).giveUserWriteMetaPermissions(table);
  }
}
