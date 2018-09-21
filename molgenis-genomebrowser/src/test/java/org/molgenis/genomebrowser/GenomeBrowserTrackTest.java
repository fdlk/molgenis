package org.molgenis.genomebrowser;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.ALT;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.CHROM;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.POS;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.REF;
import static org.molgenis.genomebrowser.meta.GenomeBrowserSettings.TrackType.VARIANT;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributes;
import org.molgenis.genomebrowser.meta.GenomeBrowserSettings;
import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.molgenis.web.converter.GsonConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = GsonConfig.class)
public class GenomeBrowserTrackTest extends AbstractMockitoTestNGSpringContextTests {
  @Test
  public void testToTrack() {
    EntityType entity = mock(EntityType.class);
    Entity attrsEntity = mock(Entity.class);
    GenomeBrowserAttributes genomeBrowserAttributes = new GenomeBrowserAttributes(attrsEntity);

    doReturn("position").when(attrsEntity).getString(POS);
    doReturn("chrom").when(attrsEntity).getString(CHROM);
    doReturn("normal").when(attrsEntity).getString(REF);
    doReturn("mutant").when(attrsEntity).getString(ALT);
    GenomeBrowserTrack reference =
        GenomeBrowserTrack.create(
            "ref_id",
            "label",
            "ref_label",
            entity,
            VARIANT,
            null,
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            null,
            null,
            null,
            null);

    EntityType molgenisEntityType =
        when(mock(EntityType.class).getId()).thenReturn("molgenisEntityType").getMock();
    GenomeBrowserTrack track =
        GenomeBrowserTrack.create(
            "id",
            "label",
            "entityLabel",
            molgenisEntityType,
            VARIANT,
            Collections.singletonList(reference),
            GenomeBrowserSettings.MolgenisReferenceMode.NONE,
            genomeBrowserAttributes,
            "alert(\"test\")",
            "attr 1:attr1,reference attribute:REF,position on genome:POS",
            null,
            null);

    String expected =
        "{\"genome_attrs\":{\"ref\":\"normal\",\"pos\":\"position\",\"alt\":\"mutant\",\"chr\":\"chrom\"},\"name\":\"label\",\"label_attr\":\"entityLabel\",\"tier_type\":\"molgenis\",\"uri\":\"/api/v2/molgenisEntityType\",\"actions\":\"alert(\\\"test\\\")\",\"track_type\":\"VARIANT\",\"entity\":\"molgenisEntityType\",\"attrs\":[\"attr 1:attr1\",\"reference attribute:REF\",\"position on genome:POS\"]}";

    assertEquals(track.toTrackJson().toString(), expected);
  }
}
