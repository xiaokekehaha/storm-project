package com.kuxun.kxtopology.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



public class HttpUtils {

	/**
	 * @param url
	 * @return 获得数据
	 * @throws IOException
	 */
	public static String get(String url) throws IOException {
		String responseBody = "";
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpget = new HttpGet(url);
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response)
						throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity)
								: null;
					} else {
						throw new ClientProtocolException(
								"Unexpected response status: " + status);
					}
				}

			};
			responseBody = httpclient.execute(httpget, responseHandler);
		} finally {
			httpclient.close();
		}
		return responseBody;
	}

	public static byte[] downloadFile(String url) throws IOException{
		byte[] bytes = null;
		BufferedOutputStream out = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		try {
			HttpPost httpPost = new HttpPost(url);
			response = httpclient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null
					&& response.getStatusLine().getStatusCode() == 200) {
				bytes = EntityUtils.toByteArray(entity);
				// File file = new File("/home/cuijh/software/aa.jar");
				// out = new BufferedOutputStream(new FileOutputStream(file));
				// out.write(bytes);
				// out.flush();
			}
		} finally {
			try {
				if (response != null)
					response.close();
				if (out != null)
					out.close();
				if (httpclient != null)
					httpclient.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return bytes;
	}
	
	public static String post(String url,String key, String postBody) throws ClientProtocolException, IOException{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair(key, postBody));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse response2 = httpclient.execute(httpPost);
        String response = null;
		try {
		    if(response2.getStatusLine().getStatusCode()==200){
		       HttpEntity entity2 = response2.getEntity();
		       response = EntityUtils.toString(entity2);
		    }
		} finally {
		    response2.close();
		}
		return response;
	}
}
