package com.mosleemapp.app.utils;

import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.dnsoverhttps.DnsOverHttps;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DnsHelper {

    public static Dns createGoogleDns() {
        OkHttpClient bootstrapClient = new OkHttpClient.Builder().build();

        return new DnsOverHttps.Builder().client(bootstrapClient)
                .url(HttpUrl.get("https://dns.google/dns-query"))
                .bootstrapDnsHosts(
                        getInetAddress("8.8.8.8"),
                        getInetAddress("8.8.4.4")
                )
                .build();
    }

    private static InetAddress getInetAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
