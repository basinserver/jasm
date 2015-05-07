package sun.net.www.protocol.jasmrsrc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import com.javaprophet.jasm.reflection.AppClassLoaderBridge;

public class Handler extends URLStreamHandler {
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		return new ResourceURLConnection(u);
	}
	
	private static final class ResourceURLConnection extends URLConnection {
		String rsrc = "";
		
		public ResourceURLConnection(URL url) {
			super(url);
			rsrc = url.toString().substring(url.toString().indexOf("jasmrsrc:") + 10);
		}
		
		public void connect() throws IOException {
		}
		
		public InputStream getInputStream() throws IOException {
			int id = Integer.parseInt(rsrc);
			if (!AppClassLoaderBridge.sunrsrc.containsKey(id)) return null;
			try {
				return new ByteArrayInputStream(AppClassLoaderBridge.sunrsrc.get(id));
			}catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
}