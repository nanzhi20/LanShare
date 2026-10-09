package com.nanzhi.network.service;

import com.nanzhi.network.model.LanAddress;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LanAddressService {
    public List<LanAddress> findAddresses() {
        List<LanAddress> addresses = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces == null) {
                return addresses;
            }
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp() || network.isLoopback() || network.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> candidates = network.getInetAddresses();
                String label = network.getDisplayName() != null ? network.getDisplayName() : network.getName();
                while (candidates.hasMoreElements()) {
                    InetAddress candidate = candidates.nextElement();
                    if (candidate instanceof Inet4Address && candidate.isSiteLocalAddress()) {
                        addresses.add(new LanAddress(label, candidate.getHostAddress()));
                    }
                }
            }
        } catch (SocketException e) {
            throw new IllegalStateException("读取局域网地址失败", e);
        }
        addresses.sort(Comparator.comparing(LanAddress::interfaceName).thenComparing(LanAddress::ip));
        return addresses;
    }
}
