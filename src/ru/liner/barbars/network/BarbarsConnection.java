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
public class BarbarsConnection extends Connection{
    public static final String SHADOW_HOST = "https://варвары.рф/";
    public static final String HOST = "https://barbars.ru/";
    @Override
    public String request(String link) throws IOException {
        link = link.replace("варвары.рф", IDN.toASCII("варвары.рф"));
        return super.request(link);
    }
}
