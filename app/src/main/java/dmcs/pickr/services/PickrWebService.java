package dmcs.pickr.services;

/**
 * Created by Ayman on 01/12/2016.
 */


/* JSON API for android appliation */


        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.OutputStreamWriter;
        import java.io.UnsupportedEncodingException;
        import java.text.SimpleDateFormat;
        import java.util.Collection;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.Locale;
        import java.util.Map;
        import java.lang.reflect.Method;
        import java.lang.reflect.Modifier;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import org.json.JSONObject;
        import org.json.JSONArray;

public class PickrWebService {

    private final String urlString = "http://pickrwebservice.somee.com/Handler.ashx";
    //private final String urlString = "192.168.0.103/Handler.ashx";

    private static String convertStreamToUTF8String(InputStream stream) throws IOException {
        String result = "";
        StringBuilder sb = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[4096];
            int readedChars = 0;
            while (readedChars != -1) {
                readedChars = reader.read(buffer);
                if (readedChars > 0)
                    sb.append(buffer, 0, readedChars);
            }
            result = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String load(String contents) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(60000);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream());
        w.write(contents);
        w.flush();
        InputStream istream = conn.getInputStream();
        String result = convertStreamToUTF8String(istream);
        return result;
    }


    private Object mapObject(Object o) {
        Object finalValue = null;
        if (o.getClass() == String.class) {
            finalValue = o;
        }
        else if (Number.class.isInstance(o)) {
            finalValue = String.valueOf(o);
        } else if (Date.class.isInstance(o)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", new Locale("en", "USA"));
            finalValue = sdf.format((Date)o);
        }
        else if (Collection.class.isInstance(o)) {
            Collection<?> col = (Collection<?>) o;
            JSONArray jarray = new JSONArray();
            for (Object item : col) {
                jarray.put(mapObject(item));
            }
            finalValue = jarray;
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            Method[] methods = o.getClass().getMethods();
            for (Method method : methods) {
                if (method.getDeclaringClass() == o.getClass()
                        && method.getModifiers() == Modifier.PUBLIC
                        && method.getName().startsWith("get")) {
                    String key = method.getName().substring(3);
                    try {
                        Object obj = method.invoke(o, null);
                        Object value = mapObject(obj);
                        map.put(key, value);
                        finalValue = new JSONObject(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return finalValue;
    }

    public JSONObject CheckUserExistence(String Email) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","PickrWebService");
        o.put("method", "CheckUserExistence");
        p.put("Email",mapObject(Email));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject GetUser(String Email) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","PickrWebService");
        o.put("method", "GetUser");
        p.put("Email",mapObject(Email));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject UserAuthentication(String Email, String Password) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","PickrWebService");
        o.put("method", "UserAuthentication");
        p.put("Email",mapObject(Email));
        p.put("Password",mapObject(Password));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

    public JSONObject GetRideDetails(int RequestId, String PartnerType) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","PickrWebService");
        o.put("method", "GetRideDetails");
        p.put("RequestId",mapObject(RequestId));
        p.put("PartnerType",mapObject(PartnerType));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }


    public JSONObject UpdateDeviceToken(String Email, String Token) throws Exception {
        JSONObject result = null;
        JSONObject o = new JSONObject();
        JSONObject p = new JSONObject();
        o.put("interface","PickrWebService");
        o.put("method", "UpdateDeviceToken");
        p.put("Email",mapObject(Email));
        p.put("Token",mapObject(Token));
        o.put("parameters", p);
        String s = o.toString();
        String r = load(s);
        result = new JSONObject(r);
        return result;
    }

}


