package neo.droid.commons;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public class SOAPs {

	/**
	 * 调用 Web Service
	 * 
	 * @param wsdlURL
	 *            wsdl 完整 URL
	 * @param namespace
	 *            命名空间
	 * @param method
	 *            调用的方法名称
	 * @param params
	 *            调用参数
	 * @param timeout
	 *            链接最大超时时间
	 * @param isDotNet
	 *            是否是 .Net 服务端
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String CallWebService(String wsdlURL, String namespace,
			String method, Map<String, String> params, int timeout,
			boolean isDotNet) throws IOException, XmlPullParserException {

		SoapObject soapObject = new SoapObject(namespace, method);

		if (null != params && params.size() > 0) {
			String key;
			Set<String> keysSet = params.keySet();
			Iterator<String> iterator = keysSet.iterator();

			while (iterator.hasNext()) {
				key = iterator.next();
				soapObject.addProperty(key, params.get(key));
			}
		}

		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.bodyOut = soapObject;
		envelope.setOutputSoapObject(soapObject);
		envelope.encodingStyle = "UTF-8";

		if (false != isDotNet) {
			envelope.dotNet = true;
		}

		(new MarshalBase64()).register(envelope);

		HttpTransportSE httpTransportSE = new HttpTransportSE(wsdlURL, timeout);
		httpTransportSE.debug = true;

		httpTransportSE.call(namespace + method, envelope);
		if (null != envelope.getResponse()) {
			return envelope.getResponse().toString();
		}

		return "";
	}
}
