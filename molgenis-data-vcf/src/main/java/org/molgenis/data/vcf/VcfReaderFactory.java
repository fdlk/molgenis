package org.molgenis.data.vcf;

import com.google.common.base.Supplier;
import java.io.Closeable;
import org.molgenis.vcf.VcfReader;

public interface VcfReaderFactory extends Supplier<VcfReader>, Closeable {}
