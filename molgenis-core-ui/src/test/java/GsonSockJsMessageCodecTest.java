import org.molgenis.ui.jobs.GsonSockJsMessageCodec;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GsonSockJsMessageCodecTest
{

	private GsonSockJsMessageCodec codec = new GsonSockJsMessageCodec();

	@Test
	public void testEncodeString()
	{
		Assert.assertEquals(codec.encode("{\"blah\"}"), "".toCharArray());
	}
}
