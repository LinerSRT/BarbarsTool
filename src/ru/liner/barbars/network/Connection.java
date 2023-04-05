package ru.liner.barbars.network;

import ru.liner.barbars.utils.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Connection {
    private static boolean isDebugging;
    private final CookieManager cookieManager;
    private final HashMap<String, String> headerMap;
    private final HashMap<String, String> payloadMap;
    private boolean isPostRequest;

    public Connection() {
        isDebugging = false;
        this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        this.headerMap = new HashMap<>();
        this.payloadMap = new HashMap<>();
        this.isPostRequest = false;
    }

    public String request(String link) throws IOException {
        if (link == null || link.isEmpty())
            throw new RuntimeException(Logger.composeLogout("Cannot request to empty or null link, aborting..."));
        HttpURLConnection connection = (HttpURLConnection) new URL(link).openConnection();
        if (isDebugging)
            System.out.println(Logger.composeLogout("Configuring connection to: " + link));
        String payload = formatQueryParams(payloadMap);
        if (!payload.isEmpty() && isPostRequest) {
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            if (!headerMap.containsKey("Content-Type"))
                addHeader("Content-Type", "text/plain; charset=utf-8");
            removeHeader("Content-Length");
            addHeader("Content-Length", String.valueOf(payload.length()));
            if (isDebugging)
                System.out.println(Logger.composeLogout("Composing POST request"));
        } else {
            removeHeader("Content-Type");
            removeHeader("Content-Length");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            if (isDebugging)
                System.out.println(Logger.composeLogout("Composing GET request"));
        }
        String cookies = getCookies();
        if (!cookies.isEmpty()) {
            connection.setRequestProperty("Cookie", cookies);
            if (isDebugging)
                System.out.println(Logger.composeLogout("Apply cookies: " + cookies));
        }
        if (!headerMap.isEmpty())
            for (Map.Entry<String, String> header : headerMap.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
                if (isDebugging)
                    System.out.println(Logger.composeLogout(String.format("Insert header: %s:%s", header.getKey(), header.getValue())));
            }
        connection.connect();
        if (isDebugging)
            System.out.println(Logger.composeLogout("connecting to " + link));
        if (!payload.isEmpty() && isPostRequest) {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            outputStreamWriter.write(payload, 0, payload.length());
            outputStreamWriter.flush();
            outputStreamWriter.close();
            connection.getOutputStream().close();
        }
        int responseCode = connection.getResponseCode();
        InputStream responseStream = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null)
            response.append(line);
        bufferedReader.close();
        if (isDebugging)
            System.out.println(Logger.composeLogout(String.format("Connection success, [%s] %s", responseCode, response)));
        final Map<String, List<String>> headerFields = connection.getHeaderFields();
        final List<String> cookiesHeader = headerFields.get("Set-Cookie");
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                if (isDebugging)
                    System.out.println(Logger.composeLogout("Storing cookie: " + HttpCookie.parse(cookie).get(0)));
            }
        }
        connection.disconnect();
        clearPayload();
        return response.toString();
    }


    public void addHeader(String key, String value) {
        if ((key == null || value == null) || headerMap.containsKey(key))
            return;
        headerMap.put(key, value);
    }

    public void addHeader(String headerPair) {
        if (headerPair != null && headerPair.contains(":")) {
            String[] keyValue = headerPair.split(":");
            if (keyValue.length == 2)
                addHeader(keyValue[0], keyValue[1]);
        }
    }

    public void removeHeader(String key) {
        if (key == null || !headerMap.containsKey(key))
            return;
        headerMap.remove(key);
    }

    public void clearHeaders() {
        this.headerMap.clear();
    }

    public void addPayload(String key, String value) {
        if ((key == null || value == null) || payloadMap.containsKey(key))
            return;
        payloadMap.put(key, value);
        isPostRequest = true;
    }

    public void addPayload(String payloadString) {
        if (payloadString == null || !payloadString.contains("&"))
            return;
        for (String part : payloadString.split("&")) {
            String[] keyValue = part.split("=");
            addPayload(keyValue[0], keyValue[1]);
        }
    }

    public void clearPayload() {
        this.payloadMap.clear();
        this.isPostRequest = false;
    }

    private String formatQueryParams(Map<String, String> params) {
        return params.entrySet().stream()
                .map(p -> p.getKey() + "=" + p.getValue())
                .reduce((p1, p2) -> p1 + "&" + p2)
                .orElse("");
    }

    private String getCookies() {
        StringBuilder stringBuilder = new StringBuilder();
        if (cookieManager.getCookieStore().getCookies().isEmpty())
            return stringBuilder.toString();
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies())
            stringBuilder.append(cookie).append(";");
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public String getCookie(String key) {
        for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
            if (cookie.getName().equals(key))
                return cookie.getValue();
        }
        if (isDebugging)
            System.out.println(Logger.composeLogout("Cant find cookie " + key));
        return null;
    }

    public static String unescapeString(String string) {
        return string
                .replace("&amp;", "&")
                .replace("%20", " ")
                .replace("%21", "!")
                .replace("%22", "\"")
                .replace("%23", "#")
                .replace("%24", "$")
                .replace("%25", "%")
                .replace("%26", "&")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%2A", "*")
                .replace("%2B", "+")
                .replace("%2C", ",")
                .replace("%2D", "-")
                .replace("%2E", ".")
                .replace("%2F", "/")
                .replace("%30", "0")
                .replace("%31", "1")
                .replace("%32", "2")
                .replace("%33", "3")
                .replace("%34", "4")
                .replace("%35", "5")
                .replace("%36", "6")
                .replace("%37", "7")
                .replace("%38", "8")
                .replace("%39", "9")
                .replace("%3A", ":")
                .replace("%3B", ";")
                .replace("%3C", "<")
                .replace("%3D", "=")
                .replace("%3E", ">")
                .replace("%3F", "?")
                .replace("%40", "@")
                .replace("%41", "A")
                .replace("%42", "B")
                .replace("%43", "C")
                .replace("%44", "D")
                .replace("%45", "E")
                .replace("%46", "F")
                .replace("%47", "G")
                .replace("%48", "H")
                .replace("%49", "I")
                .replace("%4A", "J")
                .replace("%4B", "K")
                .replace("%4C", "L")
                .replace("%4D", "M")
                .replace("%4E", "N")
                .replace("%4F", "O")
                .replace("%50", "P")
                .replace("%51", "Q")
                .replace("%52", "R")
                .replace("%53", "S")
                .replace("%54", "T")
                .replace("%55", "U")
                .replace("%56", "V")
                .replace("%57", "W")
                .replace("%58", "X")
                .replace("%59", "Y")
                .replace("%5A", "Z")
                .replace("%5B", "[")
                .replace("%5C", "\\")
                .replace("%5D", "]")
                .replace("%5E", "^")
                .replace("%5F", "_")
                .replace("%60", "`")
                .replace("%61", "a")
                .replace("%62", "b")
                .replace("%63", "c")
                .replace("%64", "d")
                .replace("%65", "e")
                .replace("%66", "f")
                .replace("%67", "g")
                .replace("%68", "h")
                .replace("%69", "i")
                .replace("%6A", "j")
                .replace("%6B", "k")
                .replace("%6C", "l")
                .replace("%6D", "m")
                .replace("%6E", "n")
                .replace("%6F", "o")
                .replace("%70", "p")
                .replace("%71", "q")
                .replace("%72", "r")
                .replace("%73", "s")
                .replace("%74", "t")
                .replace("%75", "u")
                .replace("%76", "v")
                .replace("%77", "w")
                .replace("%78", "x")
                .replace("%79", "y")
                .replace("%7A", "z")
                .replace("%7B", "{")
                .replace("%7C", "|")
                .replace("%7D", "}")
                .replace("%7E", "~")
                .replace("%7F", " ")
                .replace("%80", "€")
                .replace("%81", "")
                .replace("%82", "‚")
                .replace("%83", "ƒ")
                .replace("%84", "„")
                .replace("%85", "…")
                .replace("%86", "†")
                .replace("%87", "‡")
                .replace("%88", "ˆ")
                .replace("%89", "‰")
                .replace("%8A", "Š")
                .replace("%8B", "‹")
                .replace("%8C", "Œ")
                .replace("%8D", "")
                .replace("%8E", "Ž")
                .replace("%8F", "")
                .replace("%90", "")
                .replace("%91", "‘")
                .replace("%92", "’")
                .replace("%93", "“")
                .replace("%94", "”")
                .replace("%95", "•")
                .replace("%96", "–")
                .replace("%97", "—")
                .replace("%98", "")
                .replace("%99", "™")
                .replace("%9A", "š")
                .replace("%9B", "›")
                .replace("%9C", "œ")
                .replace("%9D", "")
                .replace("%9E", "ž")
                .replace("%9F", "Ÿ")
                .replace("%A0", " ")
                .replace("%A1", "¡")
                .replace("%A2", "¢")
                .replace("%A3", "£")
                .replace("%A4", "¤")
                .replace("%A5", "¥")
                .replace("%A6", "¦")
                .replace("%A7", "§")
                .replace("%A8", "¨")
                .replace("%A9", "©")
                .replace("%AA", "ª")
                .replace("%AB", "«")
                .replace("%AC", "¬")
                .replace("%AD", "­")
                .replace("%AE", "®")
                .replace("%AF", "¯")
                .replace("%B0", "°")
                .replace("%B1", "±")
                .replace("%B2", "²")
                .replace("%B3", "³")
                .replace("%B4", "´")
                .replace("%B5", "µ")
                .replace("%B6", "¶")
                .replace("%B7", "·")
                .replace("%B8", "¸")
                .replace("%B9", "¹")
                .replace("%BA", "º")
                .replace("%BB", "»")
                .replace("%BC", "¼")
                .replace("%BD", "½")
                .replace("%BE", "¾")
                .replace("%BF", "¿")
                .replace("%C0", "À")
                .replace("%C1", "Á")
                .replace("%C2", "Â")
                .replace("%C3", "Ã")
                .replace("%C4", "Ä")
                .replace("%C5", "Å")
                .replace("%C6", "Æ")
                .replace("%C7", "Ç")
                .replace("%C8", "È")
                .replace("%C9", "É")
                .replace("%CA", "Ê")
                .replace("%CB", "Ë")
                .replace("%CC", "Ì")
                .replace("%CD", "Í")
                .replace("%CE", "Î")
                .replace("%CF", "Ï")
                .replace("%D0", "Ð")
                .replace("%D1", "Ñ")
                .replace("%D2", "Ò")
                .replace("%D3", "Ó")
                .replace("%D4", "Ô")
                .replace("%D5", "Õ")
                .replace("%D6", "Ö")
                .replace("%D7", "×")
                .replace("%D8", "Ø")
                .replace("%D9", "Ù")
                .replace("%DA", "Ú")
                .replace("%DB", "Û")
                .replace("%DC", "Ü")
                .replace("%DD", "Ý")
                .replace("%DE", "Þ")
                .replace("%DF", "ß")
                .replace("%E0", "à")
                .replace("%E1", "á")
                .replace("%E2", "â")
                .replace("%E3", "ã")
                .replace("%E4", "ä")
                .replace("%E5", "å")
                .replace("%E6", "æ")
                .replace("%E7", "ç")
                .replace("%E8", "è")
                .replace("%E9", "é")
                .replace("%EA", "ê")
                .replace("%EB", "ë")
                .replace("%EC", "ì")
                .replace("%ED", "í")
                .replace("%EE", "î")
                .replace("%EF", "ï")
                .replace("%F0", "ð")
                .replace("%F1", "ñ")
                .replace("%F2", "ò")
                .replace("%F3", "ó")
                .replace("%F4", "ô")
                .replace("%F5", "õ")
                .replace("%F6", "ö")
                .replace("%F7", "÷")
                .replace("%F8", "ø")
                .replace("%F9", "ù")
                .replace("%FA", "ú")
                .replace("%FB", "û")
                .replace("%FC", "ü")
                .replace("%FD", "ý")
                .replace("%FE", "þ")
                .replace("%FF", "ÿ");
    }

    public static void disableSslVerification() {
        try
        {
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }
}
